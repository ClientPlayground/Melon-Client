package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.DefaultFileRegion;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.FileRegion;
import com.github.steveice10.netty.channel.socket.DuplexChannel;
import com.github.steveice10.netty.channel.unix.FileDescriptor;
import com.github.steveice10.netty.channel.unix.IovArray;
import com.github.steveice10.netty.channel.unix.SocketWritableByteChannel;
import com.github.steveice10.netty.channel.unix.UnixChannelUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.Executor;

public abstract class AbstractKQueueStreamChannel extends AbstractKQueueChannel implements DuplexChannel {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractKQueueStreamChannel.class);
  
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
  
  private static final String EXPECTED_TYPES = " (expected: " + 
    StringUtil.simpleClassName(ByteBuf.class) + ", " + 
    StringUtil.simpleClassName(DefaultFileRegion.class) + ')';
  
  private WritableByteChannel byteChannel;
  
  private final Runnable flushTask = new Runnable() {
      public void run() {
        ((AbstractKQueueChannel.AbstractKQueueUnsafe)AbstractKQueueStreamChannel.this.unsafe()).flush0();
      }
    };
  
  AbstractKQueueStreamChannel(Channel parent, BsdSocket fd, boolean active) {
    super(parent, fd, active);
  }
  
  AbstractKQueueStreamChannel(Channel parent, BsdSocket fd, SocketAddress remote) {
    super(parent, fd, remote);
  }
  
  AbstractKQueueStreamChannel(BsdSocket fd) {
    this((Channel)null, fd, isSoErrorZero(fd));
  }
  
  protected AbstractKQueueChannel.AbstractKQueueUnsafe newUnsafe() {
    return new KQueueStreamUnsafe();
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  private int writeBytes(ChannelOutboundBuffer in, ByteBuf buf) throws Exception {
    int readableBytes = buf.readableBytes();
    if (readableBytes == 0) {
      in.remove();
      return 0;
    } 
    if (buf.hasMemoryAddress() || buf.nioBufferCount() == 1)
      return doWriteBytes(in, buf); 
    ByteBuffer[] nioBuffers = buf.nioBuffers();
    return writeBytesMultiple(in, nioBuffers, nioBuffers.length, readableBytes, 
        config().getMaxBytesPerGatheringWrite());
  }
  
  private void adjustMaxBytesPerGatheringWrite(long attempted, long written, long oldMaxBytesPerGatheringWrite) {
    if (attempted == written) {
      if (attempted << 1L > oldMaxBytesPerGatheringWrite)
        config().setMaxBytesPerGatheringWrite(attempted << 1L); 
    } else if (attempted > 4096L && written < attempted >>> 1L) {
      config().setMaxBytesPerGatheringWrite(attempted >>> 1L);
    } 
  }
  
  private int writeBytesMultiple(ChannelOutboundBuffer in, IovArray array) throws IOException {
    long expectedWrittenBytes = array.size();
    assert expectedWrittenBytes != 0L;
    int cnt = array.count();
    assert cnt != 0;
    long localWrittenBytes = this.socket.writevAddresses(array.memoryAddress(0), cnt);
    if (localWrittenBytes > 0L) {
      adjustMaxBytesPerGatheringWrite(expectedWrittenBytes, localWrittenBytes, array.maxBytes());
      in.removeBytes(localWrittenBytes);
      return 1;
    } 
    return Integer.MAX_VALUE;
  }
  
  private int writeBytesMultiple(ChannelOutboundBuffer in, ByteBuffer[] nioBuffers, int nioBufferCnt, long expectedWrittenBytes, long maxBytesPerGatheringWrite) throws IOException {
    assert expectedWrittenBytes != 0L;
    if (expectedWrittenBytes > maxBytesPerGatheringWrite)
      expectedWrittenBytes = maxBytesPerGatheringWrite; 
    long localWrittenBytes = this.socket.writev(nioBuffers, 0, nioBufferCnt, expectedWrittenBytes);
    if (localWrittenBytes > 0L) {
      adjustMaxBytesPerGatheringWrite(expectedWrittenBytes, localWrittenBytes, maxBytesPerGatheringWrite);
      in.removeBytes(localWrittenBytes);
      return 1;
    } 
    return Integer.MAX_VALUE;
  }
  
  private int writeDefaultFileRegion(ChannelOutboundBuffer in, DefaultFileRegion region) throws Exception {
    long regionCount = region.count();
    if (region.transferred() >= regionCount) {
      in.remove();
      return 0;
    } 
    long offset = region.transferred();
    long flushedAmount = this.socket.sendFile(region, region.position(), offset, regionCount - offset);
    if (flushedAmount > 0L) {
      in.progress(flushedAmount);
      if (region.transferred() >= regionCount)
        in.remove(); 
      return 1;
    } 
    return Integer.MAX_VALUE;
  }
  
  private int writeFileRegion(ChannelOutboundBuffer in, FileRegion region) throws Exception {
    if (region.transferred() >= region.count()) {
      in.remove();
      return 0;
    } 
    if (this.byteChannel == null)
      this.byteChannel = (WritableByteChannel)new KQueueSocketWritableByteChannel(); 
    long flushedAmount = region.transferTo(this.byteChannel, region.transferred());
    if (flushedAmount > 0L) {
      in.progress(flushedAmount);
      if (region.transferred() >= region.count())
        in.remove(); 
      return 1;
    } 
    return Integer.MAX_VALUE;
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    int writeSpinCount = config().getWriteSpinCount();
    do {
      int msgCount = in.size();
      if (msgCount > 1 && in.current() instanceof ByteBuf) {
        writeSpinCount -= doWriteMultiple(in);
      } else {
        if (msgCount == 0) {
          writeFilter(false);
          return;
        } 
        writeSpinCount -= doWriteSingle(in);
      } 
    } while (writeSpinCount > 0);
    if (writeSpinCount == 0) {
      writeFilter(false);
      eventLoop().execute(this.flushTask);
    } else {
      writeFilter(true);
    } 
  }
  
  protected int doWriteSingle(ChannelOutboundBuffer in) throws Exception {
    Object msg = in.current();
    if (msg instanceof ByteBuf)
      return writeBytes(in, (ByteBuf)msg); 
    if (msg instanceof DefaultFileRegion)
      return writeDefaultFileRegion(in, (DefaultFileRegion)msg); 
    if (msg instanceof FileRegion)
      return writeFileRegion(in, (FileRegion)msg); 
    throw new Error();
  }
  
  private int doWriteMultiple(ChannelOutboundBuffer in) throws Exception {
    long maxBytesPerGatheringWrite = config().getMaxBytesPerGatheringWrite();
    if (PlatformDependent.hasUnsafe()) {
      IovArray array = ((KQueueEventLoop)eventLoop()).cleanArray();
      array.maxBytes(maxBytesPerGatheringWrite);
      in.forEachFlushedMessage((ChannelOutboundBuffer.MessageProcessor)array);
      if (array.count() >= 1)
        return writeBytesMultiple(in, array); 
    } else {
      ByteBuffer[] buffers = in.nioBuffers();
      int cnt = in.nioBufferCount();
      if (cnt >= 1)
        return writeBytesMultiple(in, buffers, cnt, in.nioBufferSize(), maxBytesPerGatheringWrite); 
    } 
    in.removeBytes(0L);
    return 0;
  }
  
  protected Object filterOutboundMessage(Object msg) {
    if (msg instanceof ByteBuf) {
      ByteBuf buf = (ByteBuf)msg;
      return UnixChannelUtil.isBufferCopyNeededForWrite(buf) ? newDirectBuffer(buf) : buf;
    } 
    if (msg instanceof FileRegion)
      return msg; 
    throw new UnsupportedOperationException("unsupported message type: " + 
        StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
  }
  
  protected final void doShutdownOutput() throws Exception {
    this.socket.shutdown(false, true);
  }
  
  public boolean isOutputShutdown() {
    return this.socket.isOutputShutdown();
  }
  
  public boolean isInputShutdown() {
    return this.socket.isInputShutdown();
  }
  
  public boolean isShutdown() {
    return this.socket.isShutdown();
  }
  
  public ChannelFuture shutdownOutput() {
    return shutdownOutput(newPromise());
  }
  
  public ChannelFuture shutdownOutput(final ChannelPromise promise) {
    EventLoop loop = eventLoop();
    if (loop.inEventLoop()) {
      ((AbstractChannel.AbstractUnsafe)unsafe()).shutdownOutput(promise);
    } else {
      loop.execute(new Runnable() {
            public void run() {
              ((AbstractChannel.AbstractUnsafe)AbstractKQueueStreamChannel.this.unsafe()).shutdownOutput(promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture shutdownInput() {
    return shutdownInput(newPromise());
  }
  
  public ChannelFuture shutdownInput(final ChannelPromise promise) {
    EventLoop loop = eventLoop();
    if (loop.inEventLoop()) {
      shutdownInput0(promise);
    } else {
      loop.execute(new Runnable() {
            public void run() {
              AbstractKQueueStreamChannel.this.shutdownInput0(promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  private void shutdownInput0(ChannelPromise promise) {
    try {
      this.socket.shutdown(true, false);
    } catch (Throwable cause) {
      promise.setFailure(cause);
      return;
    } 
    promise.setSuccess();
  }
  
  public ChannelFuture shutdown() {
    return shutdown(newPromise());
  }
  
  public ChannelFuture shutdown(final ChannelPromise promise) {
    ChannelFuture shutdownOutputFuture = shutdownOutput();
    if (shutdownOutputFuture.isDone()) {
      shutdownOutputDone(shutdownOutputFuture, promise);
    } else {
      shutdownOutputFuture.addListener((GenericFutureListener)new ChannelFutureListener() {
            public void operationComplete(ChannelFuture shutdownOutputFuture) throws Exception {
              AbstractKQueueStreamChannel.this.shutdownOutputDone(shutdownOutputFuture, promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  private void shutdownOutputDone(final ChannelFuture shutdownOutputFuture, final ChannelPromise promise) {
    ChannelFuture shutdownInputFuture = shutdownInput();
    if (shutdownInputFuture.isDone()) {
      shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
    } else {
      shutdownInputFuture.addListener((GenericFutureListener)new ChannelFutureListener() {
            public void operationComplete(ChannelFuture shutdownInputFuture) throws Exception {
              AbstractKQueueStreamChannel.shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
            }
          });
    } 
  }
  
  private static void shutdownDone(ChannelFuture shutdownOutputFuture, ChannelFuture shutdownInputFuture, ChannelPromise promise) {
    Throwable shutdownOutputCause = shutdownOutputFuture.cause();
    Throwable shutdownInputCause = shutdownInputFuture.cause();
    if (shutdownOutputCause != null) {
      if (shutdownInputCause != null)
        logger.debug("Exception suppressed because a previous exception occurred.", shutdownInputCause); 
      promise.setFailure(shutdownOutputCause);
    } else if (shutdownInputCause != null) {
      promise.setFailure(shutdownInputCause);
    } else {
      promise.setSuccess();
    } 
  }
  
  class KQueueStreamUnsafe extends AbstractKQueueChannel.AbstractKQueueUnsafe {
    protected Executor prepareToClose() {
      return super.prepareToClose();
    }
    
    void readReady(KQueueRecvByteAllocatorHandle allocHandle) {
      KQueueChannelConfig kQueueChannelConfig = AbstractKQueueStreamChannel.this.config();
      if (AbstractKQueueStreamChannel.this.shouldBreakReadReady((ChannelConfig)kQueueChannelConfig)) {
        clearReadFilter0();
        return;
      } 
      ChannelPipeline pipeline = AbstractKQueueStreamChannel.this.pipeline();
      ByteBufAllocator allocator = kQueueChannelConfig.getAllocator();
      allocHandle.reset((ChannelConfig)kQueueChannelConfig);
      readReadyBefore();
      ByteBuf byteBuf = null;
      boolean close = false;
      try {
        do {
          byteBuf = allocHandle.allocate(allocator);
          allocHandle.lastBytesRead(AbstractKQueueStreamChannel.this.doReadBytes(byteBuf));
          if (allocHandle.lastBytesRead() <= 0) {
            byteBuf.release();
            byteBuf = null;
            close = (allocHandle.lastBytesRead() < 0);
            if (close)
              this.readPending = false; 
            break;
          } 
          allocHandle.incMessagesRead(1);
          this.readPending = false;
          pipeline.fireChannelRead(byteBuf);
          byteBuf = null;
          if (AbstractKQueueStreamChannel.this.shouldBreakReadReady((ChannelConfig)kQueueChannelConfig))
            break; 
        } while (allocHandle.continueReading());
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
        if (close)
          shutdownInput(false); 
      } catch (Throwable t) {
        handleReadException(pipeline, byteBuf, t, close, allocHandle);
      } finally {
        readReadyFinally((ChannelConfig)kQueueChannelConfig);
      } 
    }
    
    private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close, KQueueRecvByteAllocatorHandle allocHandle) {
      if (byteBuf != null)
        if (byteBuf.isReadable()) {
          this.readPending = false;
          pipeline.fireChannelRead(byteBuf);
        } else {
          byteBuf.release();
        }  
      if (!failConnectPromise(cause)) {
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
        pipeline.fireExceptionCaught(cause);
        if (close || cause instanceof IOException)
          shutdownInput(false); 
      } 
    }
  }
  
  private final class KQueueSocketWritableByteChannel extends SocketWritableByteChannel {
    KQueueSocketWritableByteChannel() {
      super((FileDescriptor)AbstractKQueueStreamChannel.this.socket);
    }
    
    protected ByteBufAllocator alloc() {
      return AbstractKQueueStreamChannel.this.alloc();
    }
  }
}

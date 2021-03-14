package com.github.steveice10.netty.channel.epoll;

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
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.socket.DuplexChannel;
import com.github.steveice10.netty.channel.unix.FileDescriptor;
import com.github.steveice10.netty.channel.unix.IovArray;
import com.github.steveice10.netty.channel.unix.SocketWritableByteChannel;
import com.github.steveice10.netty.channel.unix.UnixChannelUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.WritableByteChannel;
import java.util.Queue;
import java.util.concurrent.Executor;

public abstract class AbstractEpollStreamChannel extends AbstractEpollChannel implements DuplexChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
  
  private static final String EXPECTED_TYPES = " (expected: " + 
    StringUtil.simpleClassName(ByteBuf.class) + ", " + 
    StringUtil.simpleClassName(DefaultFileRegion.class) + ')';
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractEpollStreamChannel.class);
  
  private static final ClosedChannelException CLEAR_SPLICE_QUEUE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractEpollStreamChannel.class, "clearSpliceQueue()");
  
  private static final ClosedChannelException SPLICE_TO_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractEpollStreamChannel.class, "spliceTo(...)");
  
  private static final ClosedChannelException FAIL_SPLICE_IF_CLOSED_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractEpollStreamChannel.class, "failSpliceIfClosed(...)");
  
  private final Runnable flushTask = new Runnable() {
      public void run() {
        ((AbstractEpollChannel.AbstractEpollUnsafe)AbstractEpollStreamChannel.this.unsafe()).flush0();
      }
    };
  
  private Queue<SpliceInTask> spliceQueue;
  
  private FileDescriptor pipeIn;
  
  private FileDescriptor pipeOut;
  
  private WritableByteChannel byteChannel;
  
  protected AbstractEpollStreamChannel(Channel parent, int fd) {
    this(parent, new LinuxSocket(fd));
  }
  
  protected AbstractEpollStreamChannel(int fd) {
    this(new LinuxSocket(fd));
  }
  
  AbstractEpollStreamChannel(LinuxSocket fd) {
    this(fd, isSoErrorZero(fd));
  }
  
  AbstractEpollStreamChannel(Channel parent, LinuxSocket fd) {
    super(parent, fd, Native.EPOLLIN, true);
    this.flags |= Native.EPOLLRDHUP;
  }
  
  AbstractEpollStreamChannel(Channel parent, LinuxSocket fd, SocketAddress remote) {
    super(parent, fd, Native.EPOLLIN, remote);
    this.flags |= Native.EPOLLRDHUP;
  }
  
  protected AbstractEpollStreamChannel(LinuxSocket fd, boolean active) {
    super((Channel)null, fd, Native.EPOLLIN, active);
    this.flags |= Native.EPOLLRDHUP;
  }
  
  protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe() {
    return new EpollStreamUnsafe();
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  public final ChannelFuture spliceTo(AbstractEpollStreamChannel ch, int len) {
    return spliceTo(ch, len, newPromise());
  }
  
  public final ChannelFuture spliceTo(AbstractEpollStreamChannel ch, int len, ChannelPromise promise) {
    if (ch.eventLoop() != eventLoop())
      throw new IllegalArgumentException("EventLoops are not the same."); 
    if (len < 0)
      throw new IllegalArgumentException("len: " + len + " (expected: >= 0)"); 
    if (ch.config().getEpollMode() != EpollMode.LEVEL_TRIGGERED || 
      config().getEpollMode() != EpollMode.LEVEL_TRIGGERED)
      throw new IllegalStateException("spliceTo() supported only when using " + EpollMode.LEVEL_TRIGGERED); 
    ObjectUtil.checkNotNull(promise, "promise");
    if (!isOpen()) {
      promise.tryFailure(SPLICE_TO_CLOSED_CHANNEL_EXCEPTION);
    } else {
      addToSpliceQueue(new SpliceInChannelTask(ch, len, promise));
      failSpliceIfClosed(promise);
    } 
    return (ChannelFuture)promise;
  }
  
  public final ChannelFuture spliceTo(FileDescriptor ch, int offset, int len) {
    return spliceTo(ch, offset, len, newPromise());
  }
  
  public final ChannelFuture spliceTo(FileDescriptor ch, int offset, int len, ChannelPromise promise) {
    if (len < 0)
      throw new IllegalArgumentException("len: " + len + " (expected: >= 0)"); 
    if (offset < 0)
      throw new IllegalArgumentException("offset must be >= 0 but was " + offset); 
    if (config().getEpollMode() != EpollMode.LEVEL_TRIGGERED)
      throw new IllegalStateException("spliceTo() supported only when using " + EpollMode.LEVEL_TRIGGERED); 
    ObjectUtil.checkNotNull(promise, "promise");
    if (!isOpen()) {
      promise.tryFailure(SPLICE_TO_CLOSED_CHANNEL_EXCEPTION);
    } else {
      addToSpliceQueue(new SpliceFdTask(ch, offset, len, promise));
      failSpliceIfClosed(promise);
    } 
    return (ChannelFuture)promise;
  }
  
  private void failSpliceIfClosed(ChannelPromise promise) {
    if (!isOpen())
      if (promise.tryFailure(FAIL_SPLICE_IF_CLOSED_CLOSED_CHANNEL_EXCEPTION))
        eventLoop().execute(new Runnable() {
              public void run() {
                AbstractEpollStreamChannel.this.clearSpliceQueue();
              }
            });  
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
      this.byteChannel = (WritableByteChannel)new EpollSocketWritableByteChannel(); 
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
          clearFlag(Native.EPOLLOUT);
          return;
        } 
        writeSpinCount -= doWriteSingle(in);
      } 
    } while (writeSpinCount > 0);
    if (writeSpinCount == 0) {
      clearFlag(Native.EPOLLOUT);
      eventLoop().execute(this.flushTask);
    } else {
      setFlag(Native.EPOLLOUT);
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
    if (msg instanceof SpliceOutTask) {
      if (!((SpliceOutTask)msg).spliceOut())
        return Integer.MAX_VALUE; 
      in.remove();
      return 1;
    } 
    throw new Error();
  }
  
  private int doWriteMultiple(ChannelOutboundBuffer in) throws Exception {
    long maxBytesPerGatheringWrite = config().getMaxBytesPerGatheringWrite();
    if (PlatformDependent.hasUnsafe()) {
      IovArray array = ((EpollEventLoop)eventLoop()).cleanArray();
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
    if (msg instanceof FileRegion || msg instanceof SpliceOutTask)
      return msg; 
    throw new UnsupportedOperationException("unsupported message type: " + 
        StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
  }
  
  protected final void doShutdownOutput() throws Exception {
    this.socket.shutdown(false, true);
  }
  
  private void shutdownInput0(ChannelPromise promise) {
    try {
      this.socket.shutdown(true, false);
      promise.setSuccess();
    } catch (Throwable cause) {
      promise.setFailure(cause);
    } 
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
              ((AbstractChannel.AbstractUnsafe)AbstractEpollStreamChannel.this.unsafe()).shutdownOutput(promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture shutdownInput() {
    return shutdownInput(newPromise());
  }
  
  public ChannelFuture shutdownInput(final ChannelPromise promise) {
    Executor closeExecutor = ((EpollStreamUnsafe)unsafe()).prepareToClose();
    if (closeExecutor != null) {
      closeExecutor.execute(new Runnable() {
            public void run() {
              AbstractEpollStreamChannel.this.shutdownInput0(promise);
            }
          });
    } else {
      EventLoop loop = eventLoop();
      if (loop.inEventLoop()) {
        shutdownInput0(promise);
      } else {
        loop.execute(new Runnable() {
              public void run() {
                AbstractEpollStreamChannel.this.shutdownInput0(promise);
              }
            });
      } 
    } 
    return (ChannelFuture)promise;
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
              AbstractEpollStreamChannel.this.shutdownOutputDone(shutdownOutputFuture, promise);
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
              AbstractEpollStreamChannel.shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
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
  
  protected void doClose() throws Exception {
    try {
      super.doClose();
    } finally {
      safeClosePipe(this.pipeIn);
      safeClosePipe(this.pipeOut);
      clearSpliceQueue();
    } 
  }
  
  private void clearSpliceQueue() {
    if (this.spliceQueue == null)
      return; 
    while (true) {
      SpliceInTask task = this.spliceQueue.poll();
      if (task == null)
        break; 
      task.promise.tryFailure(CLEAR_SPLICE_QUEUE_CLOSED_CHANNEL_EXCEPTION);
    } 
  }
  
  private static void safeClosePipe(FileDescriptor fd) {
    if (fd != null)
      try {
        fd.close();
      } catch (IOException e) {
        if (logger.isWarnEnabled())
          logger.warn("Error while closing a pipe", e); 
      }  
  }
  
  class EpollStreamUnsafe extends AbstractEpollChannel.AbstractEpollUnsafe {
    protected Executor prepareToClose() {
      return super.prepareToClose();
    }
    
    private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close, EpollRecvByteAllocatorHandle allocHandle) {
      if (byteBuf != null)
        if (byteBuf.isReadable()) {
          this.readPending = false;
          pipeline.fireChannelRead(byteBuf);
        } else {
          byteBuf.release();
        }  
      allocHandle.readComplete();
      pipeline.fireChannelReadComplete();
      pipeline.fireExceptionCaught(cause);
      if (close || cause instanceof IOException)
        shutdownInput(false); 
    }
    
    EpollRecvByteAllocatorHandle newEpollHandle(RecvByteBufAllocator.ExtendedHandle handle) {
      return new EpollRecvByteAllocatorStreamingHandle(handle);
    }
    
    void epollInReady() {
      EpollChannelConfig epollChannelConfig = AbstractEpollStreamChannel.this.config();
      if (AbstractEpollStreamChannel.this.shouldBreakEpollInReady((ChannelConfig)epollChannelConfig)) {
        clearEpollIn0();
        return;
      } 
      EpollRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
      allocHandle.edgeTriggered(AbstractEpollStreamChannel.this.isFlagSet(Native.EPOLLET));
      ChannelPipeline pipeline = AbstractEpollStreamChannel.this.pipeline();
      ByteBufAllocator allocator = epollChannelConfig.getAllocator();
      allocHandle.reset((ChannelConfig)epollChannelConfig);
      epollInBefore();
      ByteBuf byteBuf = null;
      boolean close = false;
      try {
        label37: do {
          if (AbstractEpollStreamChannel.this.spliceQueue != null) {
            AbstractEpollStreamChannel.SpliceInTask spliceTask = AbstractEpollStreamChannel.this.spliceQueue.peek();
            if (spliceTask != null) {
              if (spliceTask.spliceIn((RecvByteBufAllocator.Handle)allocHandle)) {
                if (AbstractEpollStreamChannel.this.isActive())
                  AbstractEpollStreamChannel.this.spliceQueue.remove(); 
              } else {
                break label37;
              } 
              continue;
            } 
          } 
          byteBuf = allocHandle.allocate(allocator);
          allocHandle.lastBytesRead(AbstractEpollStreamChannel.this.doReadBytes(byteBuf));
          if (allocHandle.lastBytesRead() <= 0) {
            byteBuf.release();
            byteBuf = null;
            close = (allocHandle.lastBytesRead() < 0);
            if (close)
              this.readPending = false; 
            break label37;
          } 
          allocHandle.incMessagesRead(1);
          this.readPending = false;
          pipeline.fireChannelRead(byteBuf);
          byteBuf = null;
          if (AbstractEpollStreamChannel.this.shouldBreakEpollInReady((ChannelConfig)epollChannelConfig))
            break label37; 
        } while (allocHandle.continueReading());
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
        if (close)
          shutdownInput(false); 
      } catch (Throwable t) {
        handleReadException(pipeline, byteBuf, t, close, allocHandle);
      } finally {
        epollInFinally((ChannelConfig)epollChannelConfig);
      } 
    }
  }
  
  private void addToSpliceQueue(final SpliceInTask task) {
    EventLoop eventLoop = eventLoop();
    if (eventLoop.inEventLoop()) {
      addToSpliceQueue0(task);
    } else {
      eventLoop.execute(new Runnable() {
            public void run() {
              AbstractEpollStreamChannel.this.addToSpliceQueue0(task);
            }
          });
    } 
  }
  
  private void addToSpliceQueue0(SpliceInTask task) {
    if (this.spliceQueue == null)
      this.spliceQueue = PlatformDependent.newMpscQueue(); 
    this.spliceQueue.add(task);
  }
  
  protected abstract class SpliceInTask {
    final ChannelPromise promise;
    
    int len;
    
    protected SpliceInTask(int len, ChannelPromise promise) {
      this.promise = promise;
      this.len = len;
    }
    
    abstract boolean spliceIn(RecvByteBufAllocator.Handle param1Handle);
    
    protected final int spliceIn(FileDescriptor pipeOut, RecvByteBufAllocator.Handle handle) throws IOException {
      int length = Math.min(handle.guess(), this.len);
      int splicedIn = 0;
      while (true) {
        int localSplicedIn = Native.splice(AbstractEpollStreamChannel.this.socket.intValue(), -1L, pipeOut.intValue(), -1L, length);
        if (localSplicedIn == 0)
          break; 
        splicedIn += localSplicedIn;
        length -= localSplicedIn;
      } 
      return splicedIn;
    }
  }
  
  private final class SpliceInChannelTask extends SpliceInTask implements ChannelFutureListener {
    private final AbstractEpollStreamChannel ch;
    
    SpliceInChannelTask(AbstractEpollStreamChannel ch, int len, ChannelPromise promise) {
      super(len, promise);
      this.ch = ch;
    }
    
    public void operationComplete(ChannelFuture future) throws Exception {
      if (!future.isSuccess())
        this.promise.setFailure(future.cause()); 
    }
    
    public boolean spliceIn(RecvByteBufAllocator.Handle handle) {
      assert this.ch.eventLoop().inEventLoop();
      if (this.len == 0) {
        this.promise.setSuccess();
        return true;
      } 
      try {
        FileDescriptor pipeOut = this.ch.pipeOut;
        if (pipeOut == null) {
          FileDescriptor[] pipe = FileDescriptor.pipe();
          this.ch.pipeIn = pipe[0];
          pipeOut = this.ch.pipeOut = pipe[1];
        } 
        int splicedIn = spliceIn(pipeOut, handle);
        if (splicedIn > 0) {
          ChannelPromise splicePromise;
          if (this.len != Integer.MAX_VALUE)
            this.len -= splicedIn; 
          if (this.len == 0) {
            splicePromise = this.promise;
          } else {
            splicePromise = this.ch.newPromise().addListener((GenericFutureListener)this);
          } 
          boolean autoRead = AbstractEpollStreamChannel.this.config().isAutoRead();
          this.ch.unsafe().write(new AbstractEpollStreamChannel.SpliceOutTask(this.ch, splicedIn, autoRead), splicePromise);
          this.ch.unsafe().flush();
          if (autoRead && !splicePromise.isDone())
            AbstractEpollStreamChannel.this.config().setAutoRead(false); 
        } 
        return (this.len == 0);
      } catch (Throwable cause) {
        this.promise.setFailure(cause);
        return true;
      } 
    }
  }
  
  private final class SpliceOutTask {
    private final AbstractEpollStreamChannel ch;
    
    private final boolean autoRead;
    
    private int len;
    
    SpliceOutTask(AbstractEpollStreamChannel ch, int len, boolean autoRead) {
      this.ch = ch;
      this.len = len;
      this.autoRead = autoRead;
    }
    
    public boolean spliceOut() throws Exception {
      assert this.ch.eventLoop().inEventLoop();
      try {
        int splicedOut = Native.splice(this.ch.pipeIn.intValue(), -1L, this.ch.socket.intValue(), -1L, this.len);
        this.len -= splicedOut;
        if (this.len == 0) {
          if (this.autoRead)
            AbstractEpollStreamChannel.this.config().setAutoRead(true); 
          return true;
        } 
        return false;
      } catch (IOException e) {
        if (this.autoRead)
          AbstractEpollStreamChannel.this.config().setAutoRead(true); 
        throw e;
      } 
    }
  }
  
  private final class SpliceFdTask extends SpliceInTask {
    private final FileDescriptor fd;
    
    private final ChannelPromise promise;
    
    private final int offset;
    
    SpliceFdTask(FileDescriptor fd, int offset, int len, ChannelPromise promise) {
      super(len, promise);
      this.fd = fd;
      this.promise = promise;
      this.offset = offset;
    }
    
    public boolean spliceIn(RecvByteBufAllocator.Handle handle) {
      assert AbstractEpollStreamChannel.this.eventLoop().inEventLoop();
      if (this.len == 0) {
        this.promise.setSuccess();
        return true;
      } 
      try {
        FileDescriptor[] pipe = FileDescriptor.pipe();
        FileDescriptor pipeIn = pipe[0];
        FileDescriptor pipeOut = pipe[1];
        try {
          int splicedIn = spliceIn(pipeOut, handle);
          if (splicedIn > 0) {
            if (this.len != Integer.MAX_VALUE)
              this.len -= splicedIn; 
            while (true) {
              int splicedOut = Native.splice(pipeIn.intValue(), -1L, this.fd.intValue(), this.offset, splicedIn);
              splicedIn -= splicedOut;
              if (splicedIn <= 0) {
                if (this.len == 0) {
                  this.promise.setSuccess();
                  splicedOut = 1;
                  return splicedOut;
                } 
                break;
              } 
            } 
          } 
          return false;
        } finally {
          AbstractEpollStreamChannel.safeClosePipe(pipeIn);
          AbstractEpollStreamChannel.safeClosePipe(pipeOut);
        } 
      } catch (Throwable cause) {
        this.promise.setFailure(cause);
        return true;
      } 
    }
  }
  
  private final class EpollSocketWritableByteChannel extends SocketWritableByteChannel {
    EpollSocketWritableByteChannel() {
      super((FileDescriptor)AbstractEpollStreamChannel.this.socket);
    }
    
    protected ByteBufAllocator alloc() {
      return AbstractEpollStreamChannel.this.alloc();
    }
  }
}

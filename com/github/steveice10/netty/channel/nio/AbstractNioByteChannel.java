package com.github.steveice10.netty.channel.nio;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.FileRegion;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.socket.ChannelInputShutdownEvent;
import com.github.steveice10.netty.channel.socket.ChannelInputShutdownReadComplete;
import com.github.steveice10.netty.channel.socket.SocketChannelConfig;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public abstract class AbstractNioByteChannel extends AbstractNioChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
  
  private static final String EXPECTED_TYPES = " (expected: " + 
    StringUtil.simpleClassName(ByteBuf.class) + ", " + 
    StringUtil.simpleClassName(FileRegion.class) + ')';
  
  private final Runnable flushTask = new Runnable() {
      public void run() {
        ((AbstractNioChannel.AbstractNioUnsafe)AbstractNioByteChannel.this.unsafe()).flush0();
      }
    };
  
  private boolean inputClosedSeenErrorOnRead;
  
  protected AbstractNioByteChannel(Channel parent, SelectableChannel ch) {
    super(parent, ch, 1);
  }
  
  protected boolean isInputShutdown0() {
    return false;
  }
  
  protected AbstractNioChannel.AbstractNioUnsafe newUnsafe() {
    return new NioByteUnsafe();
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  final boolean shouldBreakReadReady(ChannelConfig config) {
    return (isInputShutdown0() && (this.inputClosedSeenErrorOnRead || !isAllowHalfClosure(config)));
  }
  
  private static boolean isAllowHalfClosure(ChannelConfig config) {
    return (config instanceof SocketChannelConfig && ((SocketChannelConfig)config)
      .isAllowHalfClosure());
  }
  
  protected class NioByteUnsafe extends AbstractNioChannel.AbstractNioUnsafe {
    private void closeOnRead(ChannelPipeline pipeline) {
      if (!AbstractNioByteChannel.this.isInputShutdown0()) {
        if (AbstractNioByteChannel.isAllowHalfClosure(AbstractNioByteChannel.this.config())) {
          AbstractNioByteChannel.this.shutdownInput();
          pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
        } else {
          close(voidPromise());
        } 
      } else {
        AbstractNioByteChannel.this.inputClosedSeenErrorOnRead = true;
        pipeline.fireUserEventTriggered(ChannelInputShutdownReadComplete.INSTANCE);
      } 
    }
    
    private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close, RecvByteBufAllocator.Handle allocHandle) {
      if (byteBuf != null)
        if (byteBuf.isReadable()) {
          AbstractNioByteChannel.this.readPending = false;
          pipeline.fireChannelRead(byteBuf);
        } else {
          byteBuf.release();
        }  
      allocHandle.readComplete();
      pipeline.fireChannelReadComplete();
      pipeline.fireExceptionCaught(cause);
      if (close || cause instanceof java.io.IOException)
        closeOnRead(pipeline); 
    }
    
    public final void read() {
      ChannelConfig config = AbstractNioByteChannel.this.config();
      if (AbstractNioByteChannel.this.shouldBreakReadReady(config)) {
        AbstractNioByteChannel.this.clearReadPending();
        return;
      } 
      ChannelPipeline pipeline = AbstractNioByteChannel.this.pipeline();
      ByteBufAllocator allocator = config.getAllocator();
      RecvByteBufAllocator.Handle allocHandle = recvBufAllocHandle();
      allocHandle.reset(config);
      ByteBuf byteBuf = null;
      boolean close = false;
      try {
        do {
          byteBuf = allocHandle.allocate(allocator);
          allocHandle.lastBytesRead(AbstractNioByteChannel.this.doReadBytes(byteBuf));
          if (allocHandle.lastBytesRead() <= 0) {
            byteBuf.release();
            byteBuf = null;
            close = (allocHandle.lastBytesRead() < 0);
            if (close)
              AbstractNioByteChannel.this.readPending = false; 
            break;
          } 
          allocHandle.incMessagesRead(1);
          AbstractNioByteChannel.this.readPending = false;
          pipeline.fireChannelRead(byteBuf);
          byteBuf = null;
        } while (allocHandle.continueReading());
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
        if (close)
          closeOnRead(pipeline); 
      } catch (Throwable t) {
        handleReadException(pipeline, byteBuf, t, close, allocHandle);
      } finally {
        if (!AbstractNioByteChannel.this.readPending && !config.isAutoRead())
          removeReadOp(); 
      } 
    }
  }
  
  protected final int doWrite0(ChannelOutboundBuffer in) throws Exception {
    Object msg = in.current();
    if (msg == null)
      return 0; 
    return doWriteInternal(in, in.current());
  }
  
  private int doWriteInternal(ChannelOutboundBuffer in, Object msg) throws Exception {
    if (msg instanceof ByteBuf) {
      ByteBuf buf = (ByteBuf)msg;
      if (!buf.isReadable()) {
        in.remove();
        return 0;
      } 
      int localFlushedAmount = doWriteBytes(buf);
      if (localFlushedAmount > 0) {
        in.progress(localFlushedAmount);
        if (!buf.isReadable())
          in.remove(); 
        return 1;
      } 
    } else if (msg instanceof FileRegion) {
      FileRegion region = (FileRegion)msg;
      if (region.transferred() >= region.count()) {
        in.remove();
        return 0;
      } 
      long localFlushedAmount = doWriteFileRegion(region);
      if (localFlushedAmount > 0L) {
        in.progress(localFlushedAmount);
        if (region.transferred() >= region.count())
          in.remove(); 
        return 1;
      } 
    } else {
      throw new Error();
    } 
    return Integer.MAX_VALUE;
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    int writeSpinCount = config().getWriteSpinCount();
    do {
      Object msg = in.current();
      if (msg == null) {
        clearOpWrite();
        return;
      } 
      writeSpinCount -= doWriteInternal(in, msg);
    } while (writeSpinCount > 0);
    incompleteWrite((writeSpinCount < 0));
  }
  
  protected final Object filterOutboundMessage(Object msg) {
    if (msg instanceof ByteBuf) {
      ByteBuf buf = (ByteBuf)msg;
      if (buf.isDirect())
        return msg; 
      return newDirectBuffer(buf);
    } 
    if (msg instanceof FileRegion)
      return msg; 
    throw new UnsupportedOperationException("unsupported message type: " + 
        StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
  }
  
  protected final void incompleteWrite(boolean setOpWrite) {
    if (setOpWrite) {
      setOpWrite();
    } else {
      clearOpWrite();
      eventLoop().execute(this.flushTask);
    } 
  }
  
  protected final void setOpWrite() {
    SelectionKey key = selectionKey();
    if (!key.isValid())
      return; 
    int interestOps = key.interestOps();
    if ((interestOps & 0x4) == 0)
      key.interestOps(interestOps | 0x4); 
  }
  
  protected final void clearOpWrite() {
    SelectionKey key = selectionKey();
    if (!key.isValid())
      return; 
    int interestOps = key.interestOps();
    if ((interestOps & 0x4) != 0)
      key.interestOps(interestOps & 0xFFFFFFFB); 
  }
  
  protected abstract ChannelFuture shutdownInput();
  
  protected abstract long doWriteFileRegion(FileRegion paramFileRegion) throws Exception;
  
  protected abstract int doReadBytes(ByteBuf paramByteBuf) throws Exception;
  
  protected abstract int doWriteBytes(ByteBuf paramByteBuf) throws Exception;
}

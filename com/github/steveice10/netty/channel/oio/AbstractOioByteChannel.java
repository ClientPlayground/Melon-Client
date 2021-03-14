package com.github.steveice10.netty.channel.oio;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.FileRegion;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.socket.ChannelInputShutdownEvent;
import com.github.steveice10.netty.channel.socket.ChannelInputShutdownReadComplete;
import com.github.steveice10.netty.util.internal.StringUtil;

public abstract class AbstractOioByteChannel extends AbstractOioChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false);
  
  private static final String EXPECTED_TYPES = " (expected: " + 
    StringUtil.simpleClassName(ByteBuf.class) + ", " + 
    StringUtil.simpleClassName(FileRegion.class) + ')';
  
  protected AbstractOioByteChannel(Channel parent) {
    super(parent);
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  protected abstract boolean isInputShutdown();
  
  protected abstract ChannelFuture shutdownInput();
  
  private void closeOnRead(ChannelPipeline pipeline) {
    if (isOpen()) {
      if (Boolean.TRUE.equals(config().getOption(ChannelOption.ALLOW_HALF_CLOSURE))) {
        shutdownInput();
        pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
      } else {
        unsafe().close(unsafe().voidPromise());
      } 
      pipeline.fireUserEventTriggered(ChannelInputShutdownReadComplete.INSTANCE);
    } 
  }
  
  private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close, RecvByteBufAllocator.Handle allocHandle) {
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
    if (close || cause instanceof java.io.IOException)
      closeOnRead(pipeline); 
  }
  
  protected void doRead() {
    ChannelConfig config = config();
    if (isInputShutdown() || !this.readPending)
      return; 
    this.readPending = false;
    ChannelPipeline pipeline = pipeline();
    ByteBufAllocator allocator = config.getAllocator();
    RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
    allocHandle.reset(config);
    ByteBuf byteBuf = null;
    boolean close = false;
    boolean readData = false;
    try {
      byteBuf = allocHandle.allocate(allocator);
      do {
        allocHandle.lastBytesRead(doReadBytes(byteBuf));
        if (allocHandle.lastBytesRead() <= 0) {
          if (!byteBuf.isReadable()) {
            byteBuf.release();
            byteBuf = null;
            close = (allocHandle.lastBytesRead() < 0);
            if (close)
              this.readPending = false; 
          } 
          break;
        } 
        readData = true;
        int available = available();
        if (available <= 0)
          break; 
        if (byteBuf.isWritable())
          continue; 
        int capacity = byteBuf.capacity();
        int maxCapacity = byteBuf.maxCapacity();
        if (capacity == maxCapacity) {
          allocHandle.incMessagesRead(1);
          this.readPending = false;
          pipeline.fireChannelRead(byteBuf);
          byteBuf = allocHandle.allocate(allocator);
        } else {
          int writerIndex = byteBuf.writerIndex();
          if (writerIndex + available > maxCapacity) {
            byteBuf.capacity(maxCapacity);
          } else {
            byteBuf.ensureWritable(available);
          } 
        } 
      } while (allocHandle.continueReading());
      if (byteBuf != null) {
        if (byteBuf.isReadable()) {
          this.readPending = false;
          pipeline.fireChannelRead(byteBuf);
        } else {
          byteBuf.release();
        } 
        byteBuf = null;
      } 
      if (readData) {
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
      } 
      if (close)
        closeOnRead(pipeline); 
    } catch (Throwable t) {
      handleReadException(pipeline, byteBuf, t, close, allocHandle);
    } finally {
      if (this.readPending || config.isAutoRead() || (!readData && isActive()))
        read(); 
    } 
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    while (true) {
      Object msg = in.current();
      if (msg == null)
        break; 
      if (msg instanceof ByteBuf) {
        ByteBuf buf = (ByteBuf)msg;
        int readableBytes = buf.readableBytes();
        while (readableBytes > 0) {
          doWriteBytes(buf);
          int newReadableBytes = buf.readableBytes();
          in.progress((readableBytes - newReadableBytes));
          readableBytes = newReadableBytes;
        } 
        in.remove();
        continue;
      } 
      if (msg instanceof FileRegion) {
        FileRegion region = (FileRegion)msg;
        long transferred = region.transferred();
        doWriteFileRegion(region);
        in.progress(region.transferred() - transferred);
        in.remove();
        continue;
      } 
      in.remove(new UnsupportedOperationException("unsupported message type: " + 
            StringUtil.simpleClassName(msg)));
    } 
  }
  
  protected final Object filterOutboundMessage(Object msg) throws Exception {
    if (msg instanceof ByteBuf || msg instanceof FileRegion)
      return msg; 
    throw new UnsupportedOperationException("unsupported message type: " + 
        StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
  }
  
  protected abstract int available();
  
  protected abstract int doReadBytes(ByteBuf paramByteBuf) throws Exception;
  
  protected abstract void doWriteBytes(ByteBuf paramByteBuf) throws Exception;
  
  protected abstract void doWriteFileRegion(FileRegion paramFileRegion) throws Exception;
}

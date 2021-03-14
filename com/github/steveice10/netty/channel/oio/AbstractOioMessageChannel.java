package com.github.steveice10.netty.channel.oio;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOioMessageChannel extends AbstractOioChannel {
  private final List<Object> readBuf = new ArrayList();
  
  protected AbstractOioMessageChannel(Channel parent) {
    super(parent);
  }
  
  protected void doRead() {
    if (!this.readPending)
      return; 
    this.readPending = false;
    ChannelConfig config = config();
    ChannelPipeline pipeline = pipeline();
    RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
    allocHandle.reset(config);
    boolean closed = false;
    Throwable exception = null;
    try {
      do {
        int localRead = doReadMessages(this.readBuf);
        if (localRead == 0)
          break; 
        if (localRead < 0) {
          closed = true;
          break;
        } 
        allocHandle.incMessagesRead(localRead);
      } while (allocHandle.continueReading());
    } catch (Throwable t) {
      exception = t;
    } 
    boolean readData = false;
    int size = this.readBuf.size();
    if (size > 0) {
      readData = true;
      for (int i = 0; i < size; i++) {
        this.readPending = false;
        pipeline.fireChannelRead(this.readBuf.get(i));
      } 
      this.readBuf.clear();
      allocHandle.readComplete();
      pipeline.fireChannelReadComplete();
    } 
    if (exception != null) {
      if (exception instanceof java.io.IOException)
        closed = true; 
      pipeline.fireExceptionCaught(exception);
    } 
    if (closed) {
      if (isOpen())
        unsafe().close(unsafe().voidPromise()); 
    } else if (this.readPending || config.isAutoRead() || (!readData && isActive())) {
      read();
    } 
  }
  
  protected abstract int doReadMessages(List<Object> paramList) throws Exception;
}

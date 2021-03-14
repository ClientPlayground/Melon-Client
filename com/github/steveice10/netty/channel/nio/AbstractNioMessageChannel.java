package com.github.steveice10.netty.channel.nio;

import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNioMessageChannel extends AbstractNioChannel {
  boolean inputShutdown;
  
  protected AbstractNioMessageChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    super(parent, ch, readInterestOp);
  }
  
  protected AbstractNioChannel.AbstractNioUnsafe newUnsafe() {
    return new NioMessageUnsafe();
  }
  
  protected void doBeginRead() throws Exception {
    if (this.inputShutdown)
      return; 
    super.doBeginRead();
  }
  
  private final class NioMessageUnsafe extends AbstractNioChannel.AbstractNioUnsafe {
    private final List<Object> readBuf;
    
    private NioMessageUnsafe() {
      this.readBuf = new ArrayList();
    }
    
    public void read() {
      assert AbstractNioMessageChannel.this.eventLoop().inEventLoop();
      ChannelConfig config = AbstractNioMessageChannel.this.config();
      ChannelPipeline pipeline = AbstractNioMessageChannel.this.pipeline();
      RecvByteBufAllocator.Handle allocHandle = AbstractNioMessageChannel.this.unsafe().recvBufAllocHandle();
      allocHandle.reset(config);
      boolean closed = false;
      Throwable exception = null;
      try {
        while (true) {
          try {
            int localRead = AbstractNioMessageChannel.this.doReadMessages(this.readBuf);
            if (localRead == 0)
              break; 
            if (localRead < 0) {
              closed = true;
              break;
            } 
            allocHandle.incMessagesRead(localRead);
            if (!allocHandle.continueReading())
              break; 
          } catch (Throwable t) {
            exception = t;
            break;
          } 
        } 
        int size = this.readBuf.size();
        for (int i = 0; i < size; i++) {
          AbstractNioMessageChannel.this.readPending = false;
          pipeline.fireChannelRead(this.readBuf.get(i));
        } 
        this.readBuf.clear();
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
        if (exception != null) {
          closed = AbstractNioMessageChannel.this.closeOnReadError(exception);
          pipeline.fireExceptionCaught(exception);
        } 
        if (closed) {
          AbstractNioMessageChannel.this.inputShutdown = true;
          if (AbstractNioMessageChannel.this.isOpen())
            close(voidPromise()); 
        } 
      } finally {
        if (!AbstractNioMessageChannel.this.readPending && !config.isAutoRead())
          removeReadOp(); 
      } 
    }
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    SelectionKey key = selectionKey();
    int interestOps = key.interestOps();
    while (true) {
      Object msg = in.current();
      if (msg == null) {
        if ((interestOps & 0x4) != 0)
          key.interestOps(interestOps & 0xFFFFFFFB); 
        break;
      } 
      try {
        boolean done = false;
        for (int i = config().getWriteSpinCount() - 1; i >= 0; i--) {
          if (doWriteMessage(msg, in)) {
            done = true;
            break;
          } 
        } 
        if (done) {
          in.remove();
          continue;
        } 
        if ((interestOps & 0x4) == 0)
          key.interestOps(interestOps | 0x4); 
      } catch (Exception e) {
        if (continueOnWriteError()) {
          in.remove(e);
          continue;
        } 
        throw e;
      } 
      break;
    } 
  }
  
  protected boolean continueOnWriteError() {
    return false;
  }
  
  protected boolean closeOnReadError(Throwable cause) {
    if (!isActive())
      return true; 
    if (cause instanceof java.net.PortUnreachableException)
      return false; 
    if (cause instanceof java.io.IOException)
      return !(this instanceof com.github.steveice10.netty.channel.ServerChannel); 
    return true;
  }
  
  protected abstract int doReadMessages(List<Object> paramList) throws Exception;
  
  protected abstract boolean doWriteMessage(Object paramObject, ChannelOutboundBuffer paramChannelOutboundBuffer) throws Exception;
}

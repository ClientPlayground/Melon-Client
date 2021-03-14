package com.github.steveice10.netty.channel.oio;

import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.EventLoop;
import java.net.SocketAddress;

public abstract class AbstractOioChannel extends AbstractChannel {
  protected static final int SO_TIMEOUT = 1000;
  
  boolean readPending;
  
  private final Runnable readTask = new Runnable() {
      public void run() {
        AbstractOioChannel.this.doRead();
      }
    };
  
  private final Runnable clearReadPendingRunnable = new Runnable() {
      public void run() {
        AbstractOioChannel.this.readPending = false;
      }
    };
  
  protected AbstractOioChannel(Channel parent) {
    super(parent);
  }
  
  protected AbstractChannel.AbstractUnsafe newUnsafe() {
    return new DefaultOioUnsafe();
  }
  
  private final class DefaultOioUnsafe extends AbstractChannel.AbstractUnsafe {
    private DefaultOioUnsafe() {
      super(AbstractOioChannel.this);
    }
    
    public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
      if (!promise.setUncancellable() || !ensureOpen(promise))
        return; 
      try {
        boolean wasActive = AbstractOioChannel.this.isActive();
        AbstractOioChannel.this.doConnect(remoteAddress, localAddress);
        boolean active = AbstractOioChannel.this.isActive();
        safeSetSuccess(promise);
        if (!wasActive && active)
          AbstractOioChannel.this.pipeline().fireChannelActive(); 
      } catch (Throwable t) {
        safeSetFailure(promise, annotateConnectException(t, remoteAddress));
        closeIfClosed();
      } 
    }
  }
  
  protected boolean isCompatible(EventLoop loop) {
    return loop instanceof com.github.steveice10.netty.channel.ThreadPerChannelEventLoop;
  }
  
  protected abstract void doConnect(SocketAddress paramSocketAddress1, SocketAddress paramSocketAddress2) throws Exception;
  
  protected void doBeginRead() throws Exception {
    if (this.readPending)
      return; 
    this.readPending = true;
    eventLoop().execute(this.readTask);
  }
  
  protected abstract void doRead();
  
  @Deprecated
  protected boolean isReadPending() {
    return this.readPending;
  }
  
  @Deprecated
  protected void setReadPending(final boolean readPending) {
    if (isRegistered()) {
      EventLoop eventLoop = eventLoop();
      if (eventLoop.inEventLoop()) {
        this.readPending = readPending;
      } else {
        eventLoop.execute(new Runnable() {
              public void run() {
                AbstractOioChannel.this.readPending = readPending;
              }
            });
      } 
    } else {
      this.readPending = readPending;
    } 
  }
  
  protected final void clearReadPending() {
    if (isRegistered()) {
      EventLoop eventLoop = eventLoop();
      if (eventLoop.inEventLoop()) {
        this.readPending = false;
      } else {
        eventLoop.execute(this.clearReadPendingRunnable);
      } 
    } else {
      this.readPending = false;
    } 
  }
}

package com.github.steveice10.netty.bootstrap;

import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.DefaultChannelConfig;
import com.github.steveice10.netty.channel.EventLoop;
import java.net.SocketAddress;

final class FailedChannel extends AbstractChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false);
  
  private final ChannelConfig config = (ChannelConfig)new DefaultChannelConfig((Channel)this);
  
  FailedChannel() {
    super(null);
  }
  
  protected AbstractChannel.AbstractUnsafe newUnsafe() {
    return new FailedChannelUnsafe();
  }
  
  protected boolean isCompatible(EventLoop loop) {
    return false;
  }
  
  protected SocketAddress localAddress0() {
    return null;
  }
  
  protected SocketAddress remoteAddress0() {
    return null;
  }
  
  protected void doBind(SocketAddress localAddress) {
    throw new UnsupportedOperationException();
  }
  
  protected void doDisconnect() {
    throw new UnsupportedOperationException();
  }
  
  protected void doClose() {
    throw new UnsupportedOperationException();
  }
  
  protected void doBeginRead() {
    throw new UnsupportedOperationException();
  }
  
  protected void doWrite(ChannelOutboundBuffer in) {
    throw new UnsupportedOperationException();
  }
  
  public ChannelConfig config() {
    return this.config;
  }
  
  public boolean isOpen() {
    return false;
  }
  
  public boolean isActive() {
    return false;
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  private final class FailedChannelUnsafe extends AbstractChannel.AbstractUnsafe {
    private FailedChannelUnsafe() {
      super(FailedChannel.this);
    }
    
    public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
      promise.setFailure(new UnsupportedOperationException());
    }
  }
}

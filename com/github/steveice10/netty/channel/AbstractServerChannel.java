package com.github.steveice10.netty.channel;

import java.net.SocketAddress;

public abstract class AbstractServerChannel extends AbstractChannel implements ServerChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
  
  protected AbstractServerChannel() {
    super(null);
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  public SocketAddress remoteAddress() {
    return null;
  }
  
  protected SocketAddress remoteAddress0() {
    return null;
  }
  
  protected void doDisconnect() throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected AbstractChannel.AbstractUnsafe newUnsafe() {
    return new DefaultServerUnsafe();
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected final Object filterOutboundMessage(Object msg) {
    throw new UnsupportedOperationException();
  }
  
  private final class DefaultServerUnsafe extends AbstractChannel.AbstractUnsafe {
    private DefaultServerUnsafe() {}
    
    public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
      safeSetFailure(promise, new UnsupportedOperationException());
    }
  }
}

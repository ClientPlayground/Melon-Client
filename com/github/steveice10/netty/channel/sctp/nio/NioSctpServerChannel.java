package com.github.steveice10.netty.channel.sctp.nio;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.nio.AbstractNioMessageChannel;
import com.github.steveice10.netty.channel.sctp.DefaultSctpServerChannelConfig;
import com.github.steveice10.netty.channel.sctp.SctpServerChannel;
import com.github.steveice10.netty.channel.sctp.SctpServerChannelConfig;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NioSctpServerChannel extends AbstractNioMessageChannel implements SctpServerChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
  
  private final SctpServerChannelConfig config;
  
  private static SctpServerChannel newSocket() {
    try {
      return SctpServerChannel.open();
    } catch (IOException e) {
      throw new ChannelException("Failed to open a server socket.", e);
    } 
  }
  
  public NioSctpServerChannel() {
    super(null, newSocket(), 16);
    this.config = (SctpServerChannelConfig)new NioSctpServerChannelConfig(this, javaChannel());
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  public Set<InetSocketAddress> allLocalAddresses() {
    try {
      Set<SocketAddress> allLocalAddresses = javaChannel().getAllLocalAddresses();
      Set<InetSocketAddress> addresses = new LinkedHashSet<InetSocketAddress>(allLocalAddresses.size());
      for (SocketAddress socketAddress : allLocalAddresses)
        addresses.add((InetSocketAddress)socketAddress); 
      return addresses;
    } catch (Throwable ignored) {
      return Collections.emptySet();
    } 
  }
  
  public SctpServerChannelConfig config() {
    return this.config;
  }
  
  public boolean isActive() {
    return (isOpen() && !allLocalAddresses().isEmpty());
  }
  
  public InetSocketAddress remoteAddress() {
    return null;
  }
  
  public InetSocketAddress localAddress() {
    return (InetSocketAddress)super.localAddress();
  }
  
  protected SctpServerChannel javaChannel() {
    return (SctpServerChannel)super.javaChannel();
  }
  
  protected SocketAddress localAddress0() {
    try {
      Iterator<SocketAddress> i = javaChannel().getAllLocalAddresses().iterator();
      if (i.hasNext())
        return i.next(); 
    } catch (IOException iOException) {}
    return null;
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    javaChannel().bind(localAddress, this.config.getBacklog());
  }
  
  protected void doClose() throws Exception {
    javaChannel().close();
  }
  
  protected int doReadMessages(List<Object> buf) throws Exception {
    SctpChannel ch = javaChannel().accept();
    if (ch == null)
      return 0; 
    buf.add(new NioSctpChannel((Channel)this, ch));
    return 1;
  }
  
  public ChannelFuture bindAddress(InetAddress localAddress) {
    return bindAddress(localAddress, newPromise());
  }
  
  public ChannelFuture bindAddress(final InetAddress localAddress, final ChannelPromise promise) {
    if (eventLoop().inEventLoop()) {
      try {
        javaChannel().bindAddress(localAddress);
        promise.setSuccess();
      } catch (Throwable t) {
        promise.setFailure(t);
      } 
    } else {
      eventLoop().execute(new Runnable() {
            public void run() {
              NioSctpServerChannel.this.bindAddress(localAddress, promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture unbindAddress(InetAddress localAddress) {
    return unbindAddress(localAddress, newPromise());
  }
  
  public ChannelFuture unbindAddress(final InetAddress localAddress, final ChannelPromise promise) {
    if (eventLoop().inEventLoop()) {
      try {
        javaChannel().unbindAddress(localAddress);
        promise.setSuccess();
      } catch (Throwable t) {
        promise.setFailure(t);
      } 
    } else {
      eventLoop().execute(new Runnable() {
            public void run() {
              NioSctpServerChannel.this.unbindAddress(localAddress, promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected void doFinishConnect() throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected SocketAddress remoteAddress0() {
    return null;
  }
  
  protected void doDisconnect() throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected Object filterOutboundMessage(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  private final class NioSctpServerChannelConfig extends DefaultSctpServerChannelConfig {
    private NioSctpServerChannelConfig(NioSctpServerChannel channel, SctpServerChannel javaChannel) {
      super(channel, javaChannel);
    }
    
    protected void autoReadCleared() {
      NioSctpServerChannel.this.clearReadPending();
    }
  }
}

package com.github.steveice10.netty.channel.socket.oio;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.oio.AbstractOioMessageChannel;
import com.github.steveice10.netty.channel.socket.ServerSocketChannel;
import com.github.steveice10.netty.channel.socket.ServerSocketChannelConfig;
import com.github.steveice10.netty.util.internal.SocketUtils;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OioServerSocketChannel extends AbstractOioMessageChannel implements ServerSocketChannel {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioServerSocketChannel.class);
  
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 1);
  
  final ServerSocket socket;
  
  private static ServerSocket newServerSocket() {
    try {
      return new ServerSocket();
    } catch (IOException e) {
      throw new ChannelException("failed to create a server socket", e);
    } 
  }
  
  final Lock shutdownLock = new ReentrantLock();
  
  private final OioServerSocketChannelConfig config;
  
  public OioServerSocketChannel() {
    this(newServerSocket());
  }
  
  public OioServerSocketChannel(ServerSocket socket) {
    super(null);
    if (socket == null)
      throw new NullPointerException("socket"); 
    boolean success = false;
    try {
      socket.setSoTimeout(1000);
      success = true;
    } catch (IOException e) {
      throw new ChannelException("Failed to set the server socket timeout.", e);
    } finally {
      if (!success)
        try {
          socket.close();
        } catch (IOException e) {
          if (logger.isWarnEnabled())
            logger.warn("Failed to close a partially initialized socket.", e); 
        }  
    } 
    this.socket = socket;
    this.config = new DefaultOioServerSocketChannelConfig(this, socket);
  }
  
  public InetSocketAddress localAddress() {
    return (InetSocketAddress)super.localAddress();
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  public OioServerSocketChannelConfig config() {
    return this.config;
  }
  
  public InetSocketAddress remoteAddress() {
    return null;
  }
  
  public boolean isOpen() {
    return !this.socket.isClosed();
  }
  
  public boolean isActive() {
    return (isOpen() && this.socket.isBound());
  }
  
  protected SocketAddress localAddress0() {
    return SocketUtils.localSocketAddress(this.socket);
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    this.socket.bind(localAddress, this.config.getBacklog());
  }
  
  protected void doClose() throws Exception {
    this.socket.close();
  }
  
  protected int doReadMessages(List<Object> buf) throws Exception {
    if (this.socket.isClosed())
      return -1; 
    try {
      Socket s = this.socket.accept();
      try {
        buf.add(new OioSocketChannel((Channel)this, s));
        return 1;
      } catch (Throwable t) {
        logger.warn("Failed to create a new channel from an accepted socket.", t);
        try {
          s.close();
        } catch (Throwable t2) {
          logger.warn("Failed to close a socket.", t2);
        } 
      } 
    } catch (SocketTimeoutException socketTimeoutException) {}
    return 0;
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected Object filterOutboundMessage(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected SocketAddress remoteAddress0() {
    return null;
  }
  
  protected void doDisconnect() throws Exception {
    throw new UnsupportedOperationException();
  }
  
  @Deprecated
  protected void setReadPending(boolean readPending) {
    super.setReadPending(readPending);
  }
  
  final void clearReadPending0() {
    clearReadPending();
  }
}

package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.socket.ServerSocketChannel;
import com.github.steveice10.netty.channel.socket.SocketChannel;
import com.github.steveice10.netty.channel.socket.SocketChannelConfig;
import com.github.steveice10.netty.util.concurrent.GlobalEventExecutor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;

public final class KQueueSocketChannel extends AbstractKQueueStreamChannel implements SocketChannel {
  private final KQueueSocketChannelConfig config;
  
  public KQueueSocketChannel() {
    super((Channel)null, BsdSocket.newSocketStream(), false);
    this.config = new KQueueSocketChannelConfig(this);
  }
  
  public KQueueSocketChannel(int fd) {
    super(new BsdSocket(fd));
    this.config = new KQueueSocketChannelConfig(this);
  }
  
  KQueueSocketChannel(Channel parent, BsdSocket fd, InetSocketAddress remoteAddress) {
    super(parent, fd, remoteAddress);
    this.config = new KQueueSocketChannelConfig(this);
  }
  
  public InetSocketAddress remoteAddress() {
    return (InetSocketAddress)super.remoteAddress();
  }
  
  public InetSocketAddress localAddress() {
    return (InetSocketAddress)super.localAddress();
  }
  
  public KQueueSocketChannelConfig config() {
    return this.config;
  }
  
  public ServerSocketChannel parent() {
    return (ServerSocketChannel)super.parent();
  }
  
  protected AbstractKQueueChannel.AbstractKQueueUnsafe newUnsafe() {
    return new KQueueSocketChannelUnsafe();
  }
  
  private final class KQueueSocketChannelUnsafe extends AbstractKQueueStreamChannel.KQueueStreamUnsafe {
    private KQueueSocketChannelUnsafe() {}
    
    protected Executor prepareToClose() {
      try {
        if (KQueueSocketChannel.this.isOpen() && KQueueSocketChannel.this.config().getSoLinger() > 0) {
          ((KQueueEventLoop)KQueueSocketChannel.this.eventLoop()).remove(KQueueSocketChannel.this);
          return (Executor)GlobalEventExecutor.INSTANCE;
        } 
      } catch (Throwable throwable) {}
      return null;
    }
  }
}

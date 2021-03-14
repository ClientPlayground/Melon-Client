package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.socket.ServerSocketChannel;
import com.github.steveice10.netty.channel.socket.ServerSocketChannelConfig;
import com.github.steveice10.netty.channel.unix.NativeInetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class KQueueServerSocketChannel extends AbstractKQueueServerChannel implements ServerSocketChannel {
  private final KQueueServerSocketChannelConfig config;
  
  public KQueueServerSocketChannel() {
    super(BsdSocket.newSocketStream(), false);
    this.config = new KQueueServerSocketChannelConfig(this);
  }
  
  public KQueueServerSocketChannel(int fd) {
    this(new BsdSocket(fd));
  }
  
  KQueueServerSocketChannel(BsdSocket fd) {
    super(fd);
    this.config = new KQueueServerSocketChannelConfig(this);
  }
  
  KQueueServerSocketChannel(BsdSocket fd, boolean active) {
    super(fd, active);
    this.config = new KQueueServerSocketChannelConfig(this);
  }
  
  protected boolean isCompatible(EventLoop loop) {
    return loop instanceof KQueueEventLoop;
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    super.doBind(localAddress);
    this.socket.listen(this.config.getBacklog());
    this.active = true;
  }
  
  public InetSocketAddress remoteAddress() {
    return (InetSocketAddress)super.remoteAddress();
  }
  
  public InetSocketAddress localAddress() {
    return (InetSocketAddress)super.localAddress();
  }
  
  public KQueueServerSocketChannelConfig config() {
    return this.config;
  }
  
  protected Channel newChildChannel(int fd, byte[] address, int offset, int len) throws Exception {
    return (Channel)new KQueueSocketChannel((Channel)this, new BsdSocket(fd), NativeInetAddress.address(address, offset, len));
  }
}

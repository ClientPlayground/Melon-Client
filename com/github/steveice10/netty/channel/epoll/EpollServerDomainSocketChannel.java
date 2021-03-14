package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.unix.DomainSocketAddress;
import com.github.steveice10.netty.channel.unix.FileDescriptor;
import com.github.steveice10.netty.channel.unix.ServerDomainSocketChannel;
import com.github.steveice10.netty.channel.unix.Socket;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.File;
import java.net.SocketAddress;

public final class EpollServerDomainSocketChannel extends AbstractEpollServerChannel implements ServerDomainSocketChannel {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(EpollServerDomainSocketChannel.class);
  
  private final EpollServerChannelConfig config = new EpollServerChannelConfig(this);
  
  private volatile DomainSocketAddress local;
  
  public EpollServerDomainSocketChannel() {
    super(LinuxSocket.newSocketDomain(), false);
  }
  
  public EpollServerDomainSocketChannel(int fd) {
    super(fd);
  }
  
  EpollServerDomainSocketChannel(LinuxSocket fd) {
    super(fd);
  }
  
  EpollServerDomainSocketChannel(LinuxSocket fd, boolean active) {
    super(fd, active);
  }
  
  protected Channel newChildChannel(int fd, byte[] addr, int offset, int len) throws Exception {
    return (Channel)new EpollDomainSocketChannel((Channel)this, (FileDescriptor)new Socket(fd));
  }
  
  protected DomainSocketAddress localAddress0() {
    return this.local;
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    this.socket.bind(localAddress);
    this.socket.listen(this.config.getBacklog());
    this.local = (DomainSocketAddress)localAddress;
    this.active = true;
  }
  
  protected void doClose() throws Exception {
    try {
      super.doClose();
    } finally {
      DomainSocketAddress local = this.local;
      if (local != null) {
        File socketFile = new File(local.path());
        boolean success = socketFile.delete();
        if (!success && logger.isDebugEnabled())
          logger.debug("Failed to delete a domain socket file: {}", local.path()); 
      } 
    } 
  }
  
  public EpollServerChannelConfig config() {
    return this.config;
  }
  
  public DomainSocketAddress remoteAddress() {
    return (DomainSocketAddress)super.remoteAddress();
  }
  
  public DomainSocketAddress localAddress() {
    return (DomainSocketAddress)super.localAddress();
  }
}

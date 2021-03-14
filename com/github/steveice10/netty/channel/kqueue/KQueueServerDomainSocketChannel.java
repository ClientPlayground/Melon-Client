package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.unix.DomainSocketAddress;
import com.github.steveice10.netty.channel.unix.ServerDomainSocketChannel;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.File;
import java.net.SocketAddress;

public final class KQueueServerDomainSocketChannel extends AbstractKQueueServerChannel implements ServerDomainSocketChannel {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(KQueueServerDomainSocketChannel.class);
  
  private final KQueueServerChannelConfig config = new KQueueServerChannelConfig(this);
  
  private volatile DomainSocketAddress local;
  
  public KQueueServerDomainSocketChannel() {
    super(BsdSocket.newSocketDomain(), false);
  }
  
  public KQueueServerDomainSocketChannel(int fd) {
    this(new BsdSocket(fd), false);
  }
  
  KQueueServerDomainSocketChannel(BsdSocket socket, boolean active) {
    super(socket, active);
  }
  
  protected Channel newChildChannel(int fd, byte[] addr, int offset, int len) throws Exception {
    return (Channel)new KQueueDomainSocketChannel((Channel)this, new BsdSocket(fd));
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
  
  public KQueueServerChannelConfig config() {
    return this.config;
  }
  
  public DomainSocketAddress remoteAddress() {
    return (DomainSocketAddress)super.remoteAddress();
  }
  
  public DomainSocketAddress localAddress() {
    return (DomainSocketAddress)super.localAddress();
  }
}

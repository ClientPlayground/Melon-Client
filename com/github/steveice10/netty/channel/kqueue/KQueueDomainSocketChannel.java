package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.unix.DomainSocketAddress;
import com.github.steveice10.netty.channel.unix.DomainSocketChannel;
import com.github.steveice10.netty.channel.unix.DomainSocketChannelConfig;
import com.github.steveice10.netty.channel.unix.DomainSocketReadMode;
import com.github.steveice10.netty.channel.unix.FileDescriptor;
import com.github.steveice10.netty.channel.unix.PeerCredentials;
import java.io.IOException;
import java.net.SocketAddress;

public final class KQueueDomainSocketChannel extends AbstractKQueueStreamChannel implements DomainSocketChannel {
  private final KQueueDomainSocketChannelConfig config = new KQueueDomainSocketChannelConfig(this);
  
  private volatile DomainSocketAddress local;
  
  private volatile DomainSocketAddress remote;
  
  public KQueueDomainSocketChannel() {
    super((Channel)null, BsdSocket.newSocketDomain(), false);
  }
  
  public KQueueDomainSocketChannel(int fd) {
    this((Channel)null, new BsdSocket(fd));
  }
  
  KQueueDomainSocketChannel(Channel parent, BsdSocket fd) {
    super(parent, fd, true);
  }
  
  protected AbstractKQueueChannel.AbstractKQueueUnsafe newUnsafe() {
    return new KQueueDomainUnsafe();
  }
  
  protected DomainSocketAddress localAddress0() {
    return this.local;
  }
  
  protected DomainSocketAddress remoteAddress0() {
    return this.remote;
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    this.socket.bind(localAddress);
    this.local = (DomainSocketAddress)localAddress;
  }
  
  public KQueueDomainSocketChannelConfig config() {
    return this.config;
  }
  
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    if (super.doConnect(remoteAddress, localAddress)) {
      this.local = (DomainSocketAddress)localAddress;
      this.remote = (DomainSocketAddress)remoteAddress;
      return true;
    } 
    return false;
  }
  
  public DomainSocketAddress remoteAddress() {
    return (DomainSocketAddress)super.remoteAddress();
  }
  
  public DomainSocketAddress localAddress() {
    return (DomainSocketAddress)super.localAddress();
  }
  
  protected int doWriteSingle(ChannelOutboundBuffer in) throws Exception {
    Object msg = in.current();
    if (msg instanceof FileDescriptor && this.socket.sendFd(((FileDescriptor)msg).intValue()) > 0) {
      in.remove();
      return 1;
    } 
    return super.doWriteSingle(in);
  }
  
  protected Object filterOutboundMessage(Object msg) {
    if (msg instanceof FileDescriptor)
      return msg; 
    return super.filterOutboundMessage(msg);
  }
  
  public PeerCredentials peerCredentials() throws IOException {
    return this.socket.getPeerCredentials();
  }
  
  private final class KQueueDomainUnsafe extends AbstractKQueueStreamChannel.KQueueStreamUnsafe {
    private KQueueDomainUnsafe() {}
    
    void readReady(KQueueRecvByteAllocatorHandle allocHandle) {
      switch (KQueueDomainSocketChannel.this.config().getReadMode()) {
        case BYTES:
          super.readReady(allocHandle);
          return;
        case FILE_DESCRIPTORS:
          readReadyFd();
          return;
      } 
      throw new Error();
    }
    
    private void readReadyFd() {
      if (KQueueDomainSocketChannel.this.socket.isInputShutdown()) {
        clearReadFilter0();
        return;
      } 
      KQueueDomainSocketChannelConfig kQueueDomainSocketChannelConfig = KQueueDomainSocketChannel.this.config();
      KQueueRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
      ChannelPipeline pipeline = KQueueDomainSocketChannel.this.pipeline();
      allocHandle.reset((ChannelConfig)kQueueDomainSocketChannelConfig);
      readReadyBefore();
      try {
        do {
          int recvFd = KQueueDomainSocketChannel.this.socket.recvFd();
          switch (recvFd) {
            case 0:
              allocHandle.lastBytesRead(0);
              break;
            case -1:
              allocHandle.lastBytesRead(-1);
              close(voidPromise());
              return;
          } 
          allocHandle.lastBytesRead(1);
          allocHandle.incMessagesRead(1);
          this.readPending = false;
          pipeline.fireChannelRead(new FileDescriptor(recvFd));
        } while (allocHandle.continueReading());
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
      } catch (Throwable t) {
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
        pipeline.fireExceptionCaught(t);
      } finally {
        readReadyFinally((ChannelConfig)kQueueDomainSocketChannelConfig);
      } 
    }
  }
}

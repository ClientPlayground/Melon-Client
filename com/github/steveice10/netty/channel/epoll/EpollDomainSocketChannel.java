package com.github.steveice10.netty.channel.epoll;

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

public final class EpollDomainSocketChannel extends AbstractEpollStreamChannel implements DomainSocketChannel {
  private final EpollDomainSocketChannelConfig config = new EpollDomainSocketChannelConfig(this);
  
  private volatile DomainSocketAddress local;
  
  private volatile DomainSocketAddress remote;
  
  public EpollDomainSocketChannel() {
    super(LinuxSocket.newSocketDomain(), false);
  }
  
  EpollDomainSocketChannel(Channel parent, FileDescriptor fd) {
    super(parent, new LinuxSocket(fd.intValue()));
  }
  
  public EpollDomainSocketChannel(int fd) {
    super(fd);
  }
  
  public EpollDomainSocketChannel(Channel parent, LinuxSocket fd) {
    super(parent, fd);
  }
  
  public EpollDomainSocketChannel(int fd, boolean active) {
    super(new LinuxSocket(fd), active);
  }
  
  protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe() {
    return new EpollDomainUnsafe();
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
  
  public EpollDomainSocketChannelConfig config() {
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
  
  private final class EpollDomainUnsafe extends AbstractEpollStreamChannel.EpollStreamUnsafe {
    private EpollDomainUnsafe() {}
    
    void epollInReady() {
      switch (EpollDomainSocketChannel.this.config().getReadMode()) {
        case BYTES:
          super.epollInReady();
          return;
        case FILE_DESCRIPTORS:
          epollInReadFd();
          return;
      } 
      throw new Error();
    }
    
    private void epollInReadFd() {
      if (EpollDomainSocketChannel.this.socket.isInputShutdown()) {
        clearEpollIn0();
        return;
      } 
      EpollDomainSocketChannelConfig epollDomainSocketChannelConfig = EpollDomainSocketChannel.this.config();
      EpollRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
      allocHandle.edgeTriggered(EpollDomainSocketChannel.this.isFlagSet(Native.EPOLLET));
      ChannelPipeline pipeline = EpollDomainSocketChannel.this.pipeline();
      allocHandle.reset((ChannelConfig)epollDomainSocketChannelConfig);
      epollInBefore();
      try {
        do {
          allocHandle.lastBytesRead(EpollDomainSocketChannel.this.socket.recvFd());
          switch (allocHandle.lastBytesRead()) {
            case 0:
              break;
            case -1:
              close(voidPromise());
              return;
          } 
          allocHandle.incMessagesRead(1);
          this.readPending = false;
          pipeline.fireChannelRead(new FileDescriptor(allocHandle.lastBytesRead()));
        } while (allocHandle.continueReading());
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
      } catch (Throwable t) {
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
        pipeline.fireExceptionCaught(t);
      } finally {
        epollInFinally((ChannelConfig)epollDomainSocketChannelConfig);
      } 
    }
  }
}

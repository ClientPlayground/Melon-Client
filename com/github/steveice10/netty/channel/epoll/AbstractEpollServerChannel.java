package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.ServerChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class AbstractEpollServerChannel extends AbstractEpollChannel implements ServerChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
  
  protected AbstractEpollServerChannel(int fd) {
    this(new LinuxSocket(fd), false);
  }
  
  AbstractEpollServerChannel(LinuxSocket fd) {
    this(fd, isSoErrorZero(fd));
  }
  
  AbstractEpollServerChannel(LinuxSocket fd, boolean active) {
    super((Channel)null, fd, Native.EPOLLIN, active);
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  protected boolean isCompatible(EventLoop loop) {
    return loop instanceof EpollEventLoop;
  }
  
  protected InetSocketAddress remoteAddress0() {
    return null;
  }
  
  protected AbstractEpollChannel.AbstractEpollUnsafe newUnsafe() {
    return new EpollServerSocketUnsafe();
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected Object filterOutboundMessage(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  final class EpollServerSocketUnsafe extends AbstractEpollChannel.AbstractEpollUnsafe {
    private final byte[] acceptedAddress;
    
    EpollServerSocketUnsafe() {
      this.acceptedAddress = new byte[26];
    }
    
    public void connect(SocketAddress socketAddress, SocketAddress socketAddress2, ChannelPromise channelPromise) {
      channelPromise.setFailure(new UnsupportedOperationException());
    }
    
    void epollInReady() {
      assert AbstractEpollServerChannel.this.eventLoop().inEventLoop();
      EpollChannelConfig epollChannelConfig = AbstractEpollServerChannel.this.config();
      if (AbstractEpollServerChannel.this.shouldBreakEpollInReady((ChannelConfig)epollChannelConfig)) {
        clearEpollIn0();
        return;
      } 
      EpollRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
      allocHandle.edgeTriggered(AbstractEpollServerChannel.this.isFlagSet(Native.EPOLLET));
      ChannelPipeline pipeline = AbstractEpollServerChannel.this.pipeline();
      allocHandle.reset((ChannelConfig)epollChannelConfig);
      allocHandle.attemptedBytesRead(1);
      epollInBefore();
      Throwable exception = null;
      try {
        while (true) {
          try {
            allocHandle.lastBytesRead(AbstractEpollServerChannel.this.socket.accept(this.acceptedAddress));
            if (allocHandle.lastBytesRead() == -1)
              break; 
            allocHandle.incMessagesRead(1);
            this.readPending = false;
            pipeline.fireChannelRead(AbstractEpollServerChannel.this.newChildChannel(allocHandle.lastBytesRead(), this.acceptedAddress, 1, this.acceptedAddress[0]));
            if (!allocHandle.continueReading())
              break; 
          } catch (Throwable t) {
            exception = t;
            break;
          } 
        } 
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
        if (exception != null)
          pipeline.fireExceptionCaught(exception); 
      } finally {
        epollInFinally((ChannelConfig)epollChannelConfig);
      } 
    }
  }
  
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  abstract Channel newChildChannel(int paramInt1, byte[] paramArrayOfbyte, int paramInt2, int paramInt3) throws Exception;
}

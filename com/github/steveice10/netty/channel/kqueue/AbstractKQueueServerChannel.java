package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.ServerChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class AbstractKQueueServerChannel extends AbstractKQueueChannel implements ServerChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
  
  AbstractKQueueServerChannel(BsdSocket fd) {
    this(fd, isSoErrorZero(fd));
  }
  
  AbstractKQueueServerChannel(BsdSocket fd, boolean active) {
    super((Channel)null, fd, active);
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  protected boolean isCompatible(EventLoop loop) {
    return loop instanceof KQueueEventLoop;
  }
  
  protected InetSocketAddress remoteAddress0() {
    return null;
  }
  
  protected AbstractKQueueChannel.AbstractKQueueUnsafe newUnsafe() {
    return new KQueueServerSocketUnsafe();
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected Object filterOutboundMessage(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  abstract Channel newChildChannel(int paramInt1, byte[] paramArrayOfbyte, int paramInt2, int paramInt3) throws Exception;
  
  final class KQueueServerSocketUnsafe extends AbstractKQueueChannel.AbstractKQueueUnsafe {
    private final byte[] acceptedAddress;
    
    KQueueServerSocketUnsafe() {
      this.acceptedAddress = new byte[26];
    }
    
    void readReady(KQueueRecvByteAllocatorHandle allocHandle) {
      assert AbstractKQueueServerChannel.this.eventLoop().inEventLoop();
      KQueueChannelConfig kQueueChannelConfig = AbstractKQueueServerChannel.this.config();
      if (AbstractKQueueServerChannel.this.shouldBreakReadReady((ChannelConfig)kQueueChannelConfig)) {
        clearReadFilter0();
        return;
      } 
      ChannelPipeline pipeline = AbstractKQueueServerChannel.this.pipeline();
      allocHandle.reset((ChannelConfig)kQueueChannelConfig);
      allocHandle.attemptedBytesRead(1);
      readReadyBefore();
      Throwable exception = null;
      try {
        while (true) {
          try {
            int acceptFd = AbstractKQueueServerChannel.this.socket.accept(this.acceptedAddress);
            if (acceptFd == -1) {
              allocHandle.lastBytesRead(-1);
              break;
            } 
            allocHandle.lastBytesRead(1);
            allocHandle.incMessagesRead(1);
            this.readPending = false;
            pipeline.fireChannelRead(AbstractKQueueServerChannel.this.newChildChannel(acceptFd, this.acceptedAddress, 1, this.acceptedAddress[0]));
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
        readReadyFinally((ChannelConfig)kQueueChannelConfig);
      } 
    }
  }
}

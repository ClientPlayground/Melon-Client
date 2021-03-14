package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.channel.socket.ServerSocketChannelConfig;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

public final class EpollServerSocketChannelConfig extends EpollServerChannelConfig implements ServerSocketChannelConfig {
  EpollServerSocketChannelConfig(EpollServerSocketChannel channel) {
    super(channel);
    setReuseAddress(true);
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super.getOptions(), new ChannelOption[] { EpollChannelOption.SO_REUSEPORT, EpollChannelOption.IP_FREEBIND, EpollChannelOption.IP_TRANSPARENT, EpollChannelOption.TCP_DEFER_ACCEPT });
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == EpollChannelOption.SO_REUSEPORT)
      return (T)Boolean.valueOf(isReusePort()); 
    if (option == EpollChannelOption.IP_FREEBIND)
      return (T)Boolean.valueOf(isFreeBind()); 
    if (option == EpollChannelOption.IP_TRANSPARENT)
      return (T)Boolean.valueOf(isIpTransparent()); 
    if (option == EpollChannelOption.TCP_DEFER_ACCEPT)
      return (T)Integer.valueOf(getTcpDeferAccept()); 
    return super.getOption(option);
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == EpollChannelOption.SO_REUSEPORT) {
      setReusePort(((Boolean)value).booleanValue());
    } else if (option == EpollChannelOption.IP_FREEBIND) {
      setFreeBind(((Boolean)value).booleanValue());
    } else if (option == EpollChannelOption.IP_TRANSPARENT) {
      setIpTransparent(((Boolean)value).booleanValue());
    } else if (option == EpollChannelOption.TCP_MD5SIG) {
      Map<InetAddress, byte[]> m = (Map<InetAddress, byte[]>)value;
      setTcpMd5Sig(m);
    } else if (option == EpollChannelOption.TCP_DEFER_ACCEPT) {
      setTcpDeferAccept(((Integer)value).intValue());
    } else {
      return super.setOption(option, value);
    } 
    return true;
  }
  
  public EpollServerSocketChannelConfig setReuseAddress(boolean reuseAddress) {
    super.setReuseAddress(reuseAddress);
    return this;
  }
  
  public EpollServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize) {
    super.setReceiveBufferSize(receiveBufferSize);
    return this;
  }
  
  public EpollServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    return this;
  }
  
  public EpollServerSocketChannelConfig setBacklog(int backlog) {
    super.setBacklog(backlog);
    return this;
  }
  
  public EpollServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  @Deprecated
  public EpollServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public EpollServerSocketChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public EpollServerSocketChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public EpollServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public EpollServerSocketChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  @Deprecated
  public EpollServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  @Deprecated
  public EpollServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  public EpollServerSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public EpollServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
  
  public EpollServerSocketChannelConfig setTcpMd5Sig(Map<InetAddress, byte[]> keys) {
    try {
      ((EpollServerSocketChannel)this.channel).setTcpMd5Sig(keys);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isReusePort() {
    try {
      return this.channel.socket.isReusePort();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollServerSocketChannelConfig setReusePort(boolean reusePort) {
    try {
      this.channel.socket.setReusePort(reusePort);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isFreeBind() {
    try {
      return this.channel.socket.isIpFreeBind();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollServerSocketChannelConfig setFreeBind(boolean freeBind) {
    try {
      this.channel.socket.setIpFreeBind(freeBind);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isIpTransparent() {
    try {
      return this.channel.socket.isIpTransparent();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollServerSocketChannelConfig setIpTransparent(boolean transparent) {
    try {
      this.channel.socket.setIpTransparent(transparent);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollServerSocketChannelConfig setTcpDeferAccept(int deferAccept) {
    try {
      this.channel.socket.setTcpDeferAccept(deferAccept);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public int getTcpDeferAccept() {
    try {
      return this.channel.socket.getTcpDeferAccept();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
}

package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.channel.socket.SocketChannelConfig;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.io.IOException;
import java.util.Map;

public final class KQueueSocketChannelConfig extends KQueueChannelConfig implements SocketChannelConfig {
  private final KQueueSocketChannel channel;
  
  private volatile boolean allowHalfClosure;
  
  KQueueSocketChannelConfig(KQueueSocketChannel channel) {
    super(channel);
    this.channel = channel;
    if (PlatformDependent.canEnableTcpNoDelayByDefault())
      setTcpNoDelay(true); 
    calculateMaxBytesPerGatheringWrite();
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super
        .getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.TCP_NODELAY, ChannelOption.SO_KEEPALIVE, ChannelOption.SO_REUSEADDR, ChannelOption.SO_LINGER, ChannelOption.IP_TOS, ChannelOption.ALLOW_HALF_CLOSURE, KQueueChannelOption.SO_SNDLOWAT, KQueueChannelOption.TCP_NOPUSH });
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == ChannelOption.SO_RCVBUF)
      return (T)Integer.valueOf(getReceiveBufferSize()); 
    if (option == ChannelOption.SO_SNDBUF)
      return (T)Integer.valueOf(getSendBufferSize()); 
    if (option == ChannelOption.TCP_NODELAY)
      return (T)Boolean.valueOf(isTcpNoDelay()); 
    if (option == ChannelOption.SO_KEEPALIVE)
      return (T)Boolean.valueOf(isKeepAlive()); 
    if (option == ChannelOption.SO_REUSEADDR)
      return (T)Boolean.valueOf(isReuseAddress()); 
    if (option == ChannelOption.SO_LINGER)
      return (T)Integer.valueOf(getSoLinger()); 
    if (option == ChannelOption.IP_TOS)
      return (T)Integer.valueOf(getTrafficClass()); 
    if (option == ChannelOption.ALLOW_HALF_CLOSURE)
      return (T)Boolean.valueOf(isAllowHalfClosure()); 
    if (option == KQueueChannelOption.SO_SNDLOWAT)
      return (T)Integer.valueOf(getSndLowAt()); 
    if (option == KQueueChannelOption.TCP_NOPUSH)
      return (T)Boolean.valueOf(isTcpNoPush()); 
    return super.getOption(option);
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == ChannelOption.SO_RCVBUF) {
      setReceiveBufferSize(((Integer)value).intValue());
    } else if (option == ChannelOption.SO_SNDBUF) {
      setSendBufferSize(((Integer)value).intValue());
    } else if (option == ChannelOption.TCP_NODELAY) {
      setTcpNoDelay(((Boolean)value).booleanValue());
    } else if (option == ChannelOption.SO_KEEPALIVE) {
      setKeepAlive(((Boolean)value).booleanValue());
    } else if (option == ChannelOption.SO_REUSEADDR) {
      setReuseAddress(((Boolean)value).booleanValue());
    } else if (option == ChannelOption.SO_LINGER) {
      setSoLinger(((Integer)value).intValue());
    } else if (option == ChannelOption.IP_TOS) {
      setTrafficClass(((Integer)value).intValue());
    } else if (option == ChannelOption.ALLOW_HALF_CLOSURE) {
      setAllowHalfClosure(((Boolean)value).booleanValue());
    } else if (option == KQueueChannelOption.SO_SNDLOWAT) {
      setSndLowAt(((Integer)value).intValue());
    } else if (option == KQueueChannelOption.TCP_NOPUSH) {
      setTcpNoPush(((Boolean)value).booleanValue());
    } else {
      return super.setOption(option, value);
    } 
    return true;
  }
  
  public int getReceiveBufferSize() {
    try {
      return this.channel.socket.getReceiveBufferSize();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public int getSendBufferSize() {
    try {
      return this.channel.socket.getSendBufferSize();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public int getSoLinger() {
    try {
      return this.channel.socket.getSoLinger();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public int getTrafficClass() {
    try {
      return this.channel.socket.getTrafficClass();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isKeepAlive() {
    try {
      return this.channel.socket.isKeepAlive();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isReuseAddress() {
    try {
      return this.channel.socket.isReuseAddress();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isTcpNoDelay() {
    try {
      return this.channel.socket.isTcpNoDelay();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public int getSndLowAt() {
    try {
      return this.channel.socket.getSndLowAt();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public void setSndLowAt(int sndLowAt) {
    try {
      this.channel.socket.setSndLowAt(sndLowAt);
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isTcpNoPush() {
    try {
      return this.channel.socket.isTcpNoPush();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public void setTcpNoPush(boolean tcpNoPush) {
    try {
      this.channel.socket.setTcpNoPush(tcpNoPush);
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public KQueueSocketChannelConfig setKeepAlive(boolean keepAlive) {
    try {
      this.channel.socket.setKeepAlive(keepAlive);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public KQueueSocketChannelConfig setReceiveBufferSize(int receiveBufferSize) {
    try {
      this.channel.socket.setReceiveBufferSize(receiveBufferSize);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public KQueueSocketChannelConfig setReuseAddress(boolean reuseAddress) {
    try {
      this.channel.socket.setReuseAddress(reuseAddress);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public KQueueSocketChannelConfig setSendBufferSize(int sendBufferSize) {
    try {
      this.channel.socket.setSendBufferSize(sendBufferSize);
      calculateMaxBytesPerGatheringWrite();
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public KQueueSocketChannelConfig setSoLinger(int soLinger) {
    try {
      this.channel.socket.setSoLinger(soLinger);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public KQueueSocketChannelConfig setTcpNoDelay(boolean tcpNoDelay) {
    try {
      this.channel.socket.setTcpNoDelay(tcpNoDelay);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public KQueueSocketChannelConfig setTrafficClass(int trafficClass) {
    try {
      this.channel.socket.setTrafficClass(trafficClass);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isAllowHalfClosure() {
    return this.allowHalfClosure;
  }
  
  public KQueueSocketChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess) {
    super.setRcvAllocTransportProvidesGuess(transportProvidesGuess);
    return this;
  }
  
  public KQueueSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    return this;
  }
  
  public KQueueSocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure) {
    this.allowHalfClosure = allowHalfClosure;
    return this;
  }
  
  public KQueueSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  @Deprecated
  public KQueueSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public KQueueSocketChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public KQueueSocketChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public KQueueSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public KQueueSocketChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  public KQueueSocketChannelConfig setAutoClose(boolean autoClose) {
    super.setAutoClose(autoClose);
    return this;
  }
  
  @Deprecated
  public KQueueSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  @Deprecated
  public KQueueSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  public KQueueSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public KQueueSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
  
  private void calculateMaxBytesPerGatheringWrite() {
    int newSendBufferSize = getSendBufferSize() << 1;
    if (newSendBufferSize > 0)
      setMaxBytesPerGatheringWrite((getSendBufferSize() << 1)); 
  }
}

package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.channel.socket.ServerSocketChannelConfig;
import com.github.steveice10.netty.util.NetUtil;
import java.io.IOException;
import java.util.Map;

public class KQueueServerChannelConfig extends KQueueChannelConfig implements ServerSocketChannelConfig {
  protected final AbstractKQueueChannel channel;
  
  private volatile int backlog = NetUtil.SOMAXCONN;
  
  KQueueServerChannelConfig(AbstractKQueueChannel channel) {
    super(channel);
    this.channel = channel;
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_REUSEADDR, ChannelOption.SO_BACKLOG });
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == ChannelOption.SO_RCVBUF)
      return (T)Integer.valueOf(getReceiveBufferSize()); 
    if (option == ChannelOption.SO_REUSEADDR)
      return (T)Boolean.valueOf(isReuseAddress()); 
    if (option == ChannelOption.SO_BACKLOG)
      return (T)Integer.valueOf(getBacklog()); 
    return super.getOption(option);
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == ChannelOption.SO_RCVBUF) {
      setReceiveBufferSize(((Integer)value).intValue());
    } else if (option == ChannelOption.SO_REUSEADDR) {
      setReuseAddress(((Boolean)value).booleanValue());
    } else if (option == ChannelOption.SO_BACKLOG) {
      setBacklog(((Integer)value).intValue());
    } else {
      return super.setOption(option, value);
    } 
    return true;
  }
  
  public boolean isReuseAddress() {
    try {
      return this.channel.socket.isReuseAddress();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public KQueueServerChannelConfig setReuseAddress(boolean reuseAddress) {
    try {
      this.channel.socket.setReuseAddress(reuseAddress);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public int getReceiveBufferSize() {
    try {
      return this.channel.socket.getReceiveBufferSize();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public KQueueServerChannelConfig setReceiveBufferSize(int receiveBufferSize) {
    try {
      this.channel.socket.setReceiveBufferSize(receiveBufferSize);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public int getBacklog() {
    return this.backlog;
  }
  
  public KQueueServerChannelConfig setBacklog(int backlog) {
    if (backlog < 0)
      throw new IllegalArgumentException("backlog: " + backlog); 
    this.backlog = backlog;
    return this;
  }
  
  public KQueueServerChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess) {
    super.setRcvAllocTransportProvidesGuess(transportProvidesGuess);
    return this;
  }
  
  public KQueueServerChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    return this;
  }
  
  public KQueueServerChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  @Deprecated
  public KQueueServerChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public KQueueServerChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public KQueueServerChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public KQueueServerChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public KQueueServerChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  @Deprecated
  public KQueueServerChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  @Deprecated
  public KQueueServerChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  public KQueueServerChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public KQueueServerChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
}

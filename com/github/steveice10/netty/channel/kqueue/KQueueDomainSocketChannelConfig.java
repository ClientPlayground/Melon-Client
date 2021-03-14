package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.channel.unix.DomainSocketChannelConfig;
import com.github.steveice10.netty.channel.unix.DomainSocketReadMode;
import com.github.steveice10.netty.channel.unix.UnixChannelOption;
import java.util.Map;

public final class KQueueDomainSocketChannelConfig extends KQueueChannelConfig implements DomainSocketChannelConfig {
  private volatile DomainSocketReadMode mode = DomainSocketReadMode.BYTES;
  
  KQueueDomainSocketChannelConfig(AbstractKQueueChannel channel) {
    super(channel);
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super.getOptions(), new ChannelOption[] { UnixChannelOption.DOMAIN_SOCKET_READ_MODE });
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == UnixChannelOption.DOMAIN_SOCKET_READ_MODE)
      return (T)getReadMode(); 
    return super.getOption(option);
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == UnixChannelOption.DOMAIN_SOCKET_READ_MODE) {
      setReadMode((DomainSocketReadMode)value);
    } else {
      return super.setOption(option, value);
    } 
    return true;
  }
  
  public KQueueDomainSocketChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess) {
    super.setRcvAllocTransportProvidesGuess(transportProvidesGuess);
    return this;
  }
  
  @Deprecated
  public KQueueDomainSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public KQueueDomainSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  public KQueueDomainSocketChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public KQueueDomainSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public KQueueDomainSocketChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public KQueueDomainSocketChannelConfig setAutoClose(boolean autoClose) {
    super.setAutoClose(autoClose);
    return this;
  }
  
  public KQueueDomainSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
  
  @Deprecated
  public KQueueDomainSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  @Deprecated
  public KQueueDomainSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  public KQueueDomainSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public KQueueDomainSocketChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  public KQueueDomainSocketChannelConfig setReadMode(DomainSocketReadMode mode) {
    if (mode == null)
      throw new NullPointerException("mode"); 
    this.mode = mode;
    return this;
  }
  
  public DomainSocketReadMode getReadMode() {
    return this.mode;
  }
}
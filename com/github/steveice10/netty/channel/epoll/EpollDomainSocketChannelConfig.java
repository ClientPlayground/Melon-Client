package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.channel.unix.DomainSocketChannelConfig;
import com.github.steveice10.netty.channel.unix.DomainSocketReadMode;
import java.util.Map;

public final class EpollDomainSocketChannelConfig extends EpollChannelConfig implements DomainSocketChannelConfig {
  private volatile DomainSocketReadMode mode = DomainSocketReadMode.BYTES;
  
  EpollDomainSocketChannelConfig(AbstractEpollChannel channel) {
    super(channel);
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super.getOptions(), new ChannelOption[] { EpollChannelOption.DOMAIN_SOCKET_READ_MODE });
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == EpollChannelOption.DOMAIN_SOCKET_READ_MODE)
      return (T)getReadMode(); 
    return super.getOption(option);
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == EpollChannelOption.DOMAIN_SOCKET_READ_MODE) {
      setReadMode((DomainSocketReadMode)value);
    } else {
      return super.setOption(option, value);
    } 
    return true;
  }
  
  @Deprecated
  public EpollDomainSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public EpollDomainSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  public EpollDomainSocketChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public EpollDomainSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public EpollDomainSocketChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public EpollDomainSocketChannelConfig setAutoClose(boolean autoClose) {
    super.setAutoClose(autoClose);
    return this;
  }
  
  public EpollDomainSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
  
  @Deprecated
  public EpollDomainSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  @Deprecated
  public EpollDomainSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  public EpollDomainSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public EpollDomainSocketChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  public EpollDomainSocketChannelConfig setEpollMode(EpollMode mode) {
    super.setEpollMode(mode);
    return this;
  }
  
  public EpollDomainSocketChannelConfig setReadMode(DomainSocketReadMode mode) {
    if (mode == null)
      throw new NullPointerException("mode"); 
    this.mode = mode;
    return this;
  }
  
  public DomainSocketReadMode getReadMode() {
    return this.mode;
  }
}

package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.DefaultChannelConfig;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.channel.unix.Limits;
import java.io.IOException;
import java.util.Map;

public class EpollChannelConfig extends DefaultChannelConfig {
  final AbstractEpollChannel channel;
  
  private volatile long maxBytesPerGatheringWrite = Limits.SSIZE_MAX;
  
  EpollChannelConfig(AbstractEpollChannel channel) {
    super((Channel)channel);
    this.channel = channel;
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super.getOptions(), new ChannelOption[] { EpollChannelOption.EPOLL_MODE });
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == EpollChannelOption.EPOLL_MODE)
      return (T)getEpollMode(); 
    return (T)super.getOption(option);
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == EpollChannelOption.EPOLL_MODE) {
      setEpollMode((EpollMode)value);
    } else {
      return super.setOption(option, value);
    } 
    return true;
  }
  
  public EpollChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  @Deprecated
  public EpollChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public EpollChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public EpollChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public EpollChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    if (!(allocator.newHandle() instanceof RecvByteBufAllocator.ExtendedHandle))
      throw new IllegalArgumentException("allocator.newHandle() must return an object of type: " + RecvByteBufAllocator.ExtendedHandle.class); 
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public EpollChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  @Deprecated
  public EpollChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  @Deprecated
  public EpollChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  public EpollChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public EpollChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
  
  public EpollMode getEpollMode() {
    return this.channel.isFlagSet(Native.EPOLLET) ? EpollMode.EDGE_TRIGGERED : EpollMode.LEVEL_TRIGGERED;
  }
  
  public EpollChannelConfig setEpollMode(EpollMode mode) {
    if (mode == null)
      throw new NullPointerException("mode"); 
    try {
      switch (mode) {
        case EDGE_TRIGGERED:
          checkChannelNotRegistered();
          this.channel.setFlag(Native.EPOLLET);
          return this;
        case LEVEL_TRIGGERED:
          checkChannelNotRegistered();
          this.channel.clearFlag(Native.EPOLLET);
          return this;
      } 
      throw new Error();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  private void checkChannelNotRegistered() {
    if (this.channel.isRegistered())
      throw new IllegalStateException("EpollMode can only be changed before channel is registered"); 
  }
  
  protected final void autoReadCleared() {
    this.channel.clearEpollIn();
  }
  
  final void setMaxBytesPerGatheringWrite(long maxBytesPerGatheringWrite) {
    this.maxBytesPerGatheringWrite = maxBytesPerGatheringWrite;
  }
  
  final long getMaxBytesPerGatheringWrite() {
    return this.maxBytesPerGatheringWrite;
  }
}

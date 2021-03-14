package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.DefaultChannelConfig;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.channel.unix.Limits;
import java.util.Map;

public class KQueueChannelConfig extends DefaultChannelConfig {
  final AbstractKQueueChannel channel;
  
  private volatile boolean transportProvidesGuess;
  
  private volatile long maxBytesPerGatheringWrite = Limits.SSIZE_MAX;
  
  KQueueChannelConfig(AbstractKQueueChannel channel) {
    super((Channel)channel);
    this.channel = channel;
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super.getOptions(), new ChannelOption[] { KQueueChannelOption.RCV_ALLOC_TRANSPORT_PROVIDES_GUESS });
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == KQueueChannelOption.RCV_ALLOC_TRANSPORT_PROVIDES_GUESS)
      return (T)Boolean.valueOf(getRcvAllocTransportProvidesGuess()); 
    return (T)super.getOption(option);
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == KQueueChannelOption.RCV_ALLOC_TRANSPORT_PROVIDES_GUESS) {
      setRcvAllocTransportProvidesGuess(((Boolean)value).booleanValue());
    } else {
      return super.setOption(option, value);
    } 
    return true;
  }
  
  public KQueueChannelConfig setRcvAllocTransportProvidesGuess(boolean transportProvidesGuess) {
    this.transportProvidesGuess = transportProvidesGuess;
    return this;
  }
  
  public boolean getRcvAllocTransportProvidesGuess() {
    return this.transportProvidesGuess;
  }
  
  public KQueueChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  @Deprecated
  public KQueueChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public KQueueChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public KQueueChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public KQueueChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    if (!(allocator.newHandle() instanceof RecvByteBufAllocator.ExtendedHandle))
      throw new IllegalArgumentException("allocator.newHandle() must return an object of type: " + RecvByteBufAllocator.ExtendedHandle.class); 
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public KQueueChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  @Deprecated
  public KQueueChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  @Deprecated
  public KQueueChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  public KQueueChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public KQueueChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
  
  protected final void autoReadCleared() {
    this.channel.clearReadFilter();
  }
  
  final void setMaxBytesPerGatheringWrite(long maxBytesPerGatheringWrite) {
    this.maxBytesPerGatheringWrite = Math.min(Limits.SSIZE_MAX, maxBytesPerGatheringWrite);
  }
  
  final long getMaxBytesPerGatheringWrite() {
    return this.maxBytesPerGatheringWrite;
  }
}

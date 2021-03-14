package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class DefaultChannelConfig implements ChannelConfig {
  private static final MessageSizeEstimator DEFAULT_MSG_SIZE_ESTIMATOR = DefaultMessageSizeEstimator.DEFAULT;
  
  private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
  
  private static final AtomicIntegerFieldUpdater<DefaultChannelConfig> AUTOREAD_UPDATER = AtomicIntegerFieldUpdater.newUpdater(DefaultChannelConfig.class, "autoRead");
  
  private static final AtomicReferenceFieldUpdater<DefaultChannelConfig, WriteBufferWaterMark> WATERMARK_UPDATER = AtomicReferenceFieldUpdater.newUpdater(DefaultChannelConfig.class, WriteBufferWaterMark.class, "writeBufferWaterMark");
  
  protected final Channel channel;
  
  private volatile ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
  
  private volatile RecvByteBufAllocator rcvBufAllocator;
  
  private volatile MessageSizeEstimator msgSizeEstimator = DEFAULT_MSG_SIZE_ESTIMATOR;
  
  private volatile int connectTimeoutMillis = 30000;
  
  private volatile int writeSpinCount = 16;
  
  private volatile int autoRead = 1;
  
  private volatile boolean autoClose = true;
  
  private volatile WriteBufferWaterMark writeBufferWaterMark = WriteBufferWaterMark.DEFAULT;
  
  private volatile boolean pinEventExecutor = true;
  
  public DefaultChannelConfig(Channel channel) {
    this(channel, new AdaptiveRecvByteBufAllocator());
  }
  
  protected DefaultChannelConfig(Channel channel, RecvByteBufAllocator allocator) {
    setRecvByteBufAllocator(allocator, channel.metadata());
    this.channel = channel;
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(null, (ChannelOption<?>[])new ChannelOption[] { 
          ChannelOption.CONNECT_TIMEOUT_MILLIS, ChannelOption.MAX_MESSAGES_PER_READ, ChannelOption.WRITE_SPIN_COUNT, ChannelOption.ALLOCATOR, ChannelOption.AUTO_READ, ChannelOption.AUTO_CLOSE, ChannelOption.RCVBUF_ALLOCATOR, ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, ChannelOption.WRITE_BUFFER_WATER_MARK, 
          ChannelOption.MESSAGE_SIZE_ESTIMATOR, ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP });
  }
  
  protected Map<ChannelOption<?>, Object> getOptions(Map<ChannelOption<?>, Object> result, ChannelOption<?>... options) {
    if (result == null)
      result = new IdentityHashMap<ChannelOption<?>, Object>(); 
    for (ChannelOption<?> o : options)
      result.put(o, getOption(o)); 
    return result;
  }
  
  public boolean setOptions(Map<ChannelOption<?>, ?> options) {
    if (options == null)
      throw new NullPointerException("options"); 
    boolean setAllOptions = true;
    for (Map.Entry<ChannelOption<?>, ?> e : options.entrySet()) {
      if (!setOption(e.getKey(), e.getValue()))
        setAllOptions = false; 
    } 
    return setAllOptions;
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == null)
      throw new NullPointerException("option"); 
    if (option == ChannelOption.CONNECT_TIMEOUT_MILLIS)
      return (T)Integer.valueOf(getConnectTimeoutMillis()); 
    if (option == ChannelOption.MAX_MESSAGES_PER_READ)
      return (T)Integer.valueOf(getMaxMessagesPerRead()); 
    if (option == ChannelOption.WRITE_SPIN_COUNT)
      return (T)Integer.valueOf(getWriteSpinCount()); 
    if (option == ChannelOption.ALLOCATOR)
      return (T)getAllocator(); 
    if (option == ChannelOption.RCVBUF_ALLOCATOR)
      return (T)getRecvByteBufAllocator(); 
    if (option == ChannelOption.AUTO_READ)
      return (T)Boolean.valueOf(isAutoRead()); 
    if (option == ChannelOption.AUTO_CLOSE)
      return (T)Boolean.valueOf(isAutoClose()); 
    if (option == ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK)
      return (T)Integer.valueOf(getWriteBufferHighWaterMark()); 
    if (option == ChannelOption.WRITE_BUFFER_LOW_WATER_MARK)
      return (T)Integer.valueOf(getWriteBufferLowWaterMark()); 
    if (option == ChannelOption.WRITE_BUFFER_WATER_MARK)
      return (T)getWriteBufferWaterMark(); 
    if (option == ChannelOption.MESSAGE_SIZE_ESTIMATOR)
      return (T)getMessageSizeEstimator(); 
    if (option == ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP)
      return (T)Boolean.valueOf(getPinEventExecutorPerGroup()); 
    return null;
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == ChannelOption.CONNECT_TIMEOUT_MILLIS) {
      setConnectTimeoutMillis(((Integer)value).intValue());
    } else if (option == ChannelOption.MAX_MESSAGES_PER_READ) {
      setMaxMessagesPerRead(((Integer)value).intValue());
    } else if (option == ChannelOption.WRITE_SPIN_COUNT) {
      setWriteSpinCount(((Integer)value).intValue());
    } else if (option == ChannelOption.ALLOCATOR) {
      setAllocator((ByteBufAllocator)value);
    } else if (option == ChannelOption.RCVBUF_ALLOCATOR) {
      setRecvByteBufAllocator((RecvByteBufAllocator)value);
    } else if (option == ChannelOption.AUTO_READ) {
      setAutoRead(((Boolean)value).booleanValue());
    } else if (option == ChannelOption.AUTO_CLOSE) {
      setAutoClose(((Boolean)value).booleanValue());
    } else if (option == ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK) {
      setWriteBufferHighWaterMark(((Integer)value).intValue());
    } else if (option == ChannelOption.WRITE_BUFFER_LOW_WATER_MARK) {
      setWriteBufferLowWaterMark(((Integer)value).intValue());
    } else if (option == ChannelOption.WRITE_BUFFER_WATER_MARK) {
      setWriteBufferWaterMark((WriteBufferWaterMark)value);
    } else if (option == ChannelOption.MESSAGE_SIZE_ESTIMATOR) {
      setMessageSizeEstimator((MessageSizeEstimator)value);
    } else if (option == ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP) {
      setPinEventExecutorPerGroup(((Boolean)value).booleanValue());
    } else {
      return false;
    } 
    return true;
  }
  
  protected <T> void validate(ChannelOption<T> option, T value) {
    if (option == null)
      throw new NullPointerException("option"); 
    option.validate(value);
  }
  
  public int getConnectTimeoutMillis() {
    return this.connectTimeoutMillis;
  }
  
  public ChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    if (connectTimeoutMillis < 0)
      throw new IllegalArgumentException(String.format("connectTimeoutMillis: %d (expected: >= 0)", new Object[] { Integer.valueOf(connectTimeoutMillis) })); 
    this.connectTimeoutMillis = connectTimeoutMillis;
    return this;
  }
  
  @Deprecated
  public int getMaxMessagesPerRead() {
    try {
      MaxMessagesRecvByteBufAllocator allocator = getRecvByteBufAllocator();
      return allocator.maxMessagesPerRead();
    } catch (ClassCastException e) {
      throw new IllegalStateException("getRecvByteBufAllocator() must return an object of type MaxMessagesRecvByteBufAllocator", e);
    } 
  }
  
  @Deprecated
  public ChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    try {
      MaxMessagesRecvByteBufAllocator allocator = getRecvByteBufAllocator();
      allocator.maxMessagesPerRead(maxMessagesPerRead);
      return this;
    } catch (ClassCastException e) {
      throw new IllegalStateException("getRecvByteBufAllocator() must return an object of type MaxMessagesRecvByteBufAllocator", e);
    } 
  }
  
  public int getWriteSpinCount() {
    return this.writeSpinCount;
  }
  
  public ChannelConfig setWriteSpinCount(int writeSpinCount) {
    if (writeSpinCount <= 0)
      throw new IllegalArgumentException("writeSpinCount must be a positive integer."); 
    if (writeSpinCount == Integer.MAX_VALUE)
      writeSpinCount--; 
    this.writeSpinCount = writeSpinCount;
    return this;
  }
  
  public ByteBufAllocator getAllocator() {
    return this.allocator;
  }
  
  public ChannelConfig setAllocator(ByteBufAllocator allocator) {
    if (allocator == null)
      throw new NullPointerException("allocator"); 
    this.allocator = allocator;
    return this;
  }
  
  public <T extends RecvByteBufAllocator> T getRecvByteBufAllocator() {
    return (T)this.rcvBufAllocator;
  }
  
  public ChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    this.rcvBufAllocator = (RecvByteBufAllocator)ObjectUtil.checkNotNull(allocator, "allocator");
    return this;
  }
  
  private void setRecvByteBufAllocator(RecvByteBufAllocator allocator, ChannelMetadata metadata) {
    if (allocator instanceof MaxMessagesRecvByteBufAllocator) {
      ((MaxMessagesRecvByteBufAllocator)allocator).maxMessagesPerRead(metadata.defaultMaxMessagesPerRead());
    } else if (allocator == null) {
      throw new NullPointerException("allocator");
    } 
    setRecvByteBufAllocator(allocator);
  }
  
  public boolean isAutoRead() {
    return (this.autoRead == 1);
  }
  
  public ChannelConfig setAutoRead(boolean autoRead) {
    boolean oldAutoRead = (AUTOREAD_UPDATER.getAndSet(this, autoRead ? 1 : 0) == 1);
    if (autoRead && !oldAutoRead) {
      this.channel.read();
    } else if (!autoRead && oldAutoRead) {
      autoReadCleared();
    } 
    return this;
  }
  
  protected void autoReadCleared() {}
  
  public boolean isAutoClose() {
    return this.autoClose;
  }
  
  public ChannelConfig setAutoClose(boolean autoClose) {
    this.autoClose = autoClose;
    return this;
  }
  
  public int getWriteBufferHighWaterMark() {
    return this.writeBufferWaterMark.high();
  }
  
  public ChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    WriteBufferWaterMark waterMark;
    if (writeBufferHighWaterMark < 0)
      throw new IllegalArgumentException("writeBufferHighWaterMark must be >= 0"); 
    do {
      waterMark = this.writeBufferWaterMark;
      if (writeBufferHighWaterMark < waterMark.low())
        throw new IllegalArgumentException("writeBufferHighWaterMark cannot be less than writeBufferLowWaterMark (" + waterMark
            
            .low() + "): " + writeBufferHighWaterMark); 
    } while (!WATERMARK_UPDATER.compareAndSet(this, waterMark, new WriteBufferWaterMark(waterMark
          .low(), writeBufferHighWaterMark, false)));
    return this;
  }
  
  public int getWriteBufferLowWaterMark() {
    return this.writeBufferWaterMark.low();
  }
  
  public ChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    WriteBufferWaterMark waterMark;
    if (writeBufferLowWaterMark < 0)
      throw new IllegalArgumentException("writeBufferLowWaterMark must be >= 0"); 
    do {
      waterMark = this.writeBufferWaterMark;
      if (writeBufferLowWaterMark > waterMark.high())
        throw new IllegalArgumentException("writeBufferLowWaterMark cannot be greater than writeBufferHighWaterMark (" + waterMark
            
            .high() + "): " + writeBufferLowWaterMark); 
    } while (!WATERMARK_UPDATER.compareAndSet(this, waterMark, new WriteBufferWaterMark(writeBufferLowWaterMark, waterMark
          .high(), false)));
    return this;
  }
  
  public ChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    this.writeBufferWaterMark = (WriteBufferWaterMark)ObjectUtil.checkNotNull(writeBufferWaterMark, "writeBufferWaterMark");
    return this;
  }
  
  public WriteBufferWaterMark getWriteBufferWaterMark() {
    return this.writeBufferWaterMark;
  }
  
  public MessageSizeEstimator getMessageSizeEstimator() {
    return this.msgSizeEstimator;
  }
  
  public ChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    if (estimator == null)
      throw new NullPointerException("estimator"); 
    this.msgSizeEstimator = estimator;
    return this;
  }
  
  private ChannelConfig setPinEventExecutorPerGroup(boolean pinEventExecutor) {
    this.pinEventExecutor = pinEventExecutor;
    return this;
  }
  
  private boolean getPinEventExecutorPerGroup() {
    return this.pinEventExecutor;
  }
}

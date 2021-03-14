package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;

abstract class MpscArrayQueueProducerLimitField<E> extends MpscArrayQueueMidPad<E> {
  private static final long P_LIMIT_OFFSET;
  
  private volatile long producerLimit;
  
  static {
    try {
      P_LIMIT_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(MpscArrayQueueProducerLimitField.class.getDeclaredField("producerLimit"));
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public MpscArrayQueueProducerLimitField(int capacity) {
    super(capacity);
    this.producerLimit = capacity;
  }
  
  protected final long lvProducerLimit() {
    return this.producerLimit;
  }
  
  protected final void soProducerLimit(long newValue) {
    UnsafeAccess.UNSAFE.putOrderedLong(this, P_LIMIT_OFFSET, newValue);
  }
}

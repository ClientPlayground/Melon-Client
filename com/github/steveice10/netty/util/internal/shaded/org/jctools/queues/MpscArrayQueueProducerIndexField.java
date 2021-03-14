package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;

abstract class MpscArrayQueueProducerIndexField<E> extends MpscArrayQueueL1Pad<E> {
  private static final long P_INDEX_OFFSET;
  
  private volatile long producerIndex;
  
  static {
    try {
      P_INDEX_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(MpscArrayQueueProducerIndexField.class.getDeclaredField("producerIndex"));
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public MpscArrayQueueProducerIndexField(int capacity) {
    super(capacity);
  }
  
  public final long lvProducerIndex() {
    return this.producerIndex;
  }
  
  protected final boolean casProducerIndex(long expect, long newValue) {
    return UnsafeAccess.UNSAFE.compareAndSwapLong(this, P_INDEX_OFFSET, expect, newValue);
  }
}

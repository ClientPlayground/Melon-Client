package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;

abstract class MpscArrayQueueConsumerIndexField<E> extends MpscArrayQueueL2Pad<E> {
  private static final long C_INDEX_OFFSET;
  
  protected long consumerIndex;
  
  static {
    try {
      C_INDEX_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(MpscArrayQueueConsumerIndexField.class.getDeclaredField("consumerIndex"));
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public MpscArrayQueueConsumerIndexField(int capacity) {
    super(capacity);
  }
  
  protected final long lpConsumerIndex() {
    return this.consumerIndex;
  }
  
  public final long lvConsumerIndex() {
    return UnsafeAccess.UNSAFE.getLongVolatile(this, C_INDEX_OFFSET);
  }
  
  protected void soConsumerIndex(long newValue) {
    UnsafeAccess.UNSAFE.putOrderedLong(this, C_INDEX_OFFSET, newValue);
  }
}

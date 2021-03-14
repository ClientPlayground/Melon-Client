package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import java.lang.reflect.Field;

abstract class BaseMpscLinkedArrayQueueColdProducerFields<E> extends BaseMpscLinkedArrayQueuePad3<E> {
  private static final long P_LIMIT_OFFSET;
  
  private volatile long producerLimit;
  
  protected long producerMask;
  
  protected E[] producerBuffer;
  
  static {
    try {
      Field iField = BaseMpscLinkedArrayQueueColdProducerFields.class.getDeclaredField("producerLimit");
      P_LIMIT_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(iField);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } 
  }
  
  final long lvProducerLimit() {
    return this.producerLimit;
  }
  
  final boolean casProducerLimit(long expect, long newValue) {
    return UnsafeAccess.UNSAFE.compareAndSwapLong(this, P_LIMIT_OFFSET, expect, newValue);
  }
  
  final void soProducerLimit(long newValue) {
    UnsafeAccess.UNSAFE.putOrderedLong(this, P_LIMIT_OFFSET, newValue);
  }
}

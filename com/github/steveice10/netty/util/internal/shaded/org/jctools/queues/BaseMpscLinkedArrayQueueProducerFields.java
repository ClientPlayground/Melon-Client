package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import java.lang.reflect.Field;

abstract class BaseMpscLinkedArrayQueueProducerFields<E> extends BaseMpscLinkedArrayQueuePad1<E> {
  private static final long P_INDEX_OFFSET;
  
  protected long producerIndex;
  
  static {
    try {
      Field iField = BaseMpscLinkedArrayQueueProducerFields.class.getDeclaredField("producerIndex");
      P_INDEX_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(iField);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public final long lvProducerIndex() {
    return UnsafeAccess.UNSAFE.getLongVolatile(this, P_INDEX_OFFSET);
  }
  
  final void soProducerIndex(long newValue) {
    UnsafeAccess.UNSAFE.putOrderedLong(this, P_INDEX_OFFSET, newValue);
  }
  
  final boolean casProducerIndex(long expect, long newValue) {
    return UnsafeAccess.UNSAFE.compareAndSwapLong(this, P_INDEX_OFFSET, expect, newValue);
  }
}

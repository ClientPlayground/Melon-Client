package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import java.lang.reflect.Field;

abstract class BaseMpscLinkedArrayQueueConsumerFields<E> extends BaseMpscLinkedArrayQueuePad2<E> {
  private static final long C_INDEX_OFFSET;
  
  protected long consumerMask;
  
  protected E[] consumerBuffer;
  
  protected long consumerIndex;
  
  static {
    try {
      Field iField = BaseMpscLinkedArrayQueueConsumerFields.class.getDeclaredField("consumerIndex");
      C_INDEX_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(iField);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public final long lvConsumerIndex() {
    return UnsafeAccess.UNSAFE.getLongVolatile(this, C_INDEX_OFFSET);
  }
  
  final void soConsumerIndex(long newValue) {
    UnsafeAccess.UNSAFE.putOrderedLong(this, C_INDEX_OFFSET, newValue);
  }
}

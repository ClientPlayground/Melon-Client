package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import java.lang.reflect.Field;

abstract class BaseLinkedQueueProducerNodeRef<E> extends BaseLinkedQueuePad0<E> {
  protected static final long P_NODE_OFFSET;
  
  protected LinkedQueueNode<E> producerNode;
  
  static {
    try {
      Field pNodeField = BaseLinkedQueueProducerNodeRef.class.getDeclaredField("producerNode");
      P_NODE_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(pNodeField);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } 
  }
  
  protected final void spProducerNode(LinkedQueueNode<E> newValue) {
    this.producerNode = newValue;
  }
  
  protected final LinkedQueueNode<E> lvProducerNode() {
    return (LinkedQueueNode<E>)UnsafeAccess.UNSAFE.getObjectVolatile(this, P_NODE_OFFSET);
  }
  
  protected final boolean casProducerNode(LinkedQueueNode<E> expect, LinkedQueueNode<E> newValue) {
    return UnsafeAccess.UNSAFE.compareAndSwapObject(this, P_NODE_OFFSET, expect, newValue);
  }
  
  protected final LinkedQueueNode<E> lpProducerNode() {
    return this.producerNode;
  }
}

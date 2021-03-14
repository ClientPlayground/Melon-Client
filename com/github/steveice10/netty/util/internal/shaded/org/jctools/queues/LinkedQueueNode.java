package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;

final class LinkedQueueNode<E> {
  private static final long NEXT_OFFSET;
  
  private E value;
  
  private volatile LinkedQueueNode<E> next;
  
  static {
    try {
      NEXT_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(LinkedQueueNode.class.getDeclaredField("next"));
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } 
  }
  
  LinkedQueueNode() {
    this(null);
  }
  
  LinkedQueueNode(E val) {
    spValue(val);
  }
  
  public E getAndNullValue() {
    E temp = lpValue();
    spValue(null);
    return temp;
  }
  
  public E lpValue() {
    return this.value;
  }
  
  public void spValue(E newValue) {
    this.value = newValue;
  }
  
  public void soNext(LinkedQueueNode<E> n) {
    UnsafeAccess.UNSAFE.putOrderedObject(this, NEXT_OFFSET, n);
  }
  
  public LinkedQueueNode<E> lvNext() {
    return this.next;
  }
}

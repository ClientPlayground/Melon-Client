package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.atomic;

import java.util.concurrent.atomic.AtomicReference;

public final class LinkedQueueAtomicNode<E> extends AtomicReference<LinkedQueueAtomicNode<E>> {
  private static final long serialVersionUID = 2404266111789071508L;
  
  private E value;
  
  LinkedQueueAtomicNode() {}
  
  LinkedQueueAtomicNode(E val) {
    spValue(val);
  }
  
  public E getAndNullValue() {
    E temp = lpValue();
    spValue((E)null);
    return temp;
  }
  
  public E lpValue() {
    return this.value;
  }
  
  public void spValue(E newValue) {
    this.value = newValue;
  }
  
  public void soNext(LinkedQueueAtomicNode<E> n) {
    lazySet(n);
  }
  
  public LinkedQueueAtomicNode<E> lvNext() {
    return get();
  }
}

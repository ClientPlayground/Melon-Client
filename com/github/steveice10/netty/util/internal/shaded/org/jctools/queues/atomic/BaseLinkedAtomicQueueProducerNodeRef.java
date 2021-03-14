package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.atomic;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

abstract class BaseLinkedAtomicQueueProducerNodeRef<E> extends BaseLinkedAtomicQueuePad0<E> {
  private static final AtomicReferenceFieldUpdater<BaseLinkedAtomicQueueProducerNodeRef, LinkedQueueAtomicNode> P_NODE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(BaseLinkedAtomicQueueProducerNodeRef.class, LinkedQueueAtomicNode.class, "producerNode");
  
  protected volatile LinkedQueueAtomicNode<E> producerNode;
  
  protected final void spProducerNode(LinkedQueueAtomicNode<E> newValue) {
    P_NODE_UPDATER.lazySet(this, newValue);
  }
  
  protected final LinkedQueueAtomicNode<E> lvProducerNode() {
    return this.producerNode;
  }
  
  protected final boolean casProducerNode(LinkedQueueAtomicNode<E> expect, LinkedQueueAtomicNode<E> newValue) {
    return P_NODE_UPDATER.compareAndSet(this, expect, newValue);
  }
  
  protected final LinkedQueueAtomicNode<E> lpProducerNode() {
    return this.producerNode;
  }
  
  protected final LinkedQueueAtomicNode<E> xchgProducerNode(LinkedQueueAtomicNode<E> newValue) {
    return P_NODE_UPDATER.getAndSet(this, newValue);
  }
}

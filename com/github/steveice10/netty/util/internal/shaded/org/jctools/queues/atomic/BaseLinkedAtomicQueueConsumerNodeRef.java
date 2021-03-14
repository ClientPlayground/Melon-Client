package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.atomic;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

abstract class BaseLinkedAtomicQueueConsumerNodeRef<E> extends BaseLinkedAtomicQueuePad1<E> {
  private static final AtomicReferenceFieldUpdater<BaseLinkedAtomicQueueConsumerNodeRef, LinkedQueueAtomicNode> C_NODE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(BaseLinkedAtomicQueueConsumerNodeRef.class, LinkedQueueAtomicNode.class, "consumerNode");
  
  protected volatile LinkedQueueAtomicNode<E> consumerNode;
  
  protected final void spConsumerNode(LinkedQueueAtomicNode<E> newValue) {
    C_NODE_UPDATER.lazySet(this, newValue);
  }
  
  protected final LinkedQueueAtomicNode<E> lvConsumerNode() {
    return this.consumerNode;
  }
  
  protected final LinkedQueueAtomicNode<E> lpConsumerNode() {
    return this.consumerNode;
  }
}

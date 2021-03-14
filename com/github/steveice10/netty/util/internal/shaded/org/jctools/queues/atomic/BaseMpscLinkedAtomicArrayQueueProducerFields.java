package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.atomic;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

abstract class BaseMpscLinkedAtomicArrayQueueProducerFields<E> extends BaseMpscLinkedAtomicArrayQueuePad1<E> {
  private static final AtomicLongFieldUpdater<BaseMpscLinkedAtomicArrayQueueProducerFields> P_INDEX_UPDATER = AtomicLongFieldUpdater.newUpdater(BaseMpscLinkedAtomicArrayQueueProducerFields.class, "producerIndex");
  
  protected volatile long producerIndex;
  
  public final long lvProducerIndex() {
    return this.producerIndex;
  }
  
  final void soProducerIndex(long newValue) {
    P_INDEX_UPDATER.lazySet(this, newValue);
  }
  
  final boolean casProducerIndex(long expect, long newValue) {
    return P_INDEX_UPDATER.compareAndSet(this, expect, newValue);
  }
}

package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.atomic;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

abstract class MpscAtomicArrayQueueProducerIndexField<E> extends MpscAtomicArrayQueueL1Pad<E> {
  private static final AtomicLongFieldUpdater<MpscAtomicArrayQueueProducerIndexField> P_INDEX_UPDATER = AtomicLongFieldUpdater.newUpdater(MpscAtomicArrayQueueProducerIndexField.class, "producerIndex");
  
  private volatile long producerIndex;
  
  public MpscAtomicArrayQueueProducerIndexField(int capacity) {
    super(capacity);
  }
  
  public final long lvProducerIndex() {
    return this.producerIndex;
  }
  
  protected final boolean casProducerIndex(long expect, long newValue) {
    return P_INDEX_UPDATER.compareAndSet(this, expect, newValue);
  }
}

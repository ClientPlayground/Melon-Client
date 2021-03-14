package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.Pow2;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.RangeUtil;

abstract class MpscChunkedArrayQueueColdProducerFields<E> extends BaseMpscLinkedArrayQueue<E> {
  protected final long maxQueueCapacity;
  
  public MpscChunkedArrayQueueColdProducerFields(int initialCapacity, int maxCapacity) {
    super(initialCapacity);
    RangeUtil.checkGreaterThanOrEqual(maxCapacity, 4, "maxCapacity");
    RangeUtil.checkLessThan(Pow2.roundToPowerOfTwo(initialCapacity), Pow2.roundToPowerOfTwo(maxCapacity), "initialCapacity");
    this.maxQueueCapacity = Pow2.roundToPowerOfTwo(maxCapacity) << 1L;
  }
}

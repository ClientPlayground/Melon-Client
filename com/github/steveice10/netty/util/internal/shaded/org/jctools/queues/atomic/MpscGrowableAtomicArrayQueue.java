package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.atomic;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.Pow2;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.RangeUtil;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class MpscGrowableAtomicArrayQueue<E> extends MpscChunkedAtomicArrayQueue<E> {
  public MpscGrowableAtomicArrayQueue(int maxCapacity) {
    super(Math.max(2, Pow2.roundToPowerOfTwo(maxCapacity / 8)), maxCapacity);
  }
  
  public MpscGrowableAtomicArrayQueue(int initialCapacity, int maxCapacity) {
    super(initialCapacity, maxCapacity);
  }
  
  protected int getNextBufferSize(AtomicReferenceArray<E> buffer) {
    long maxSize = this.maxQueueCapacity / 2L;
    RangeUtil.checkLessThanOrEqual(LinkedAtomicArrayQueueUtil.length(buffer), maxSize, "buffer.length");
    int newSize = 2 * (LinkedAtomicArrayQueueUtil.length(buffer) - 1);
    return newSize + 1;
  }
  
  protected long getCurrentBufferCapacity(long mask) {
    return (mask + 2L == this.maxQueueCapacity) ? this.maxQueueCapacity : mask;
  }
}

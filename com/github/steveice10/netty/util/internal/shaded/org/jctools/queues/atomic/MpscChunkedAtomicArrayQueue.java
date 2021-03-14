package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.atomic;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.Pow2;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class MpscChunkedAtomicArrayQueue<E> extends MpscChunkedAtomicArrayQueueColdProducerFields<E> {
  long p0;
  
  long p1;
  
  long p2;
  
  long p3;
  
  long p4;
  
  long p5;
  
  long p6;
  
  long p7;
  
  long p10;
  
  long p11;
  
  long p12;
  
  long p13;
  
  long p14;
  
  long p15;
  
  long p16;
  
  long p17;
  
  public MpscChunkedAtomicArrayQueue(int maxCapacity) {
    super(Math.max(2, Math.min(1024, Pow2.roundToPowerOfTwo(maxCapacity / 8))), maxCapacity);
  }
  
  public MpscChunkedAtomicArrayQueue(int initialCapacity, int maxCapacity) {
    super(initialCapacity, maxCapacity);
  }
  
  protected long availableInQueue(long pIndex, long cIndex) {
    return this.maxQueueCapacity - pIndex - cIndex;
  }
  
  public int capacity() {
    return (int)(this.maxQueueCapacity / 2L);
  }
  
  protected int getNextBufferSize(AtomicReferenceArray<E> buffer) {
    return LinkedAtomicArrayQueueUtil.length(buffer);
  }
  
  protected long getCurrentBufferCapacity(long mask) {
    return mask;
  }
}

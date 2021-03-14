package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.atomic;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.QueueProgressIndicators;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.Pow2;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.RangeUtil;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReferenceArray;

public abstract class BaseMpscLinkedAtomicArrayQueue<E> extends BaseMpscLinkedAtomicArrayQueueColdProducerFields<E> implements MessagePassingQueue<E>, QueueProgressIndicators {
  private static final Object JUMP = new Object();
  
  public BaseMpscLinkedAtomicArrayQueue(int initialCapacity) {
    RangeUtil.checkGreaterThanOrEqual(initialCapacity, 2, "initialCapacity");
    int p2capacity = Pow2.roundToPowerOfTwo(initialCapacity);
    long mask = (p2capacity - 1 << 1);
    AtomicReferenceArray<E> buffer = LinkedAtomicArrayQueueUtil.allocate(p2capacity + 1);
    this.producerBuffer = buffer;
    this.producerMask = mask;
    this.consumerBuffer = buffer;
    this.consumerMask = mask;
    soProducerLimit(mask);
  }
  
  public final Iterator<E> iterator() {
    throw new UnsupportedOperationException();
  }
  
  public final int size() {
    long after = lvConsumerIndex();
    while (true) {
      long before = after;
      long currentProducerIndex = lvProducerIndex();
      after = lvConsumerIndex();
      if (before == after) {
        long size = currentProducerIndex - after >> 1L;
        if (size > 2147483647L)
          return Integer.MAX_VALUE; 
        return (int)size;
      } 
    } 
  }
  
  public final boolean isEmpty() {
    return (lvConsumerIndex() == lvProducerIndex());
  }
  
  public String toString() {
    return getClass().getName();
  }
  
  public boolean offer(E e) {
    long pIndex, mask;
    AtomicReferenceArray<E> buffer;
    if (null == e)
      throw new NullPointerException(); 
    while (true) {
      long producerLimit = lvProducerLimit();
      pIndex = lvProducerIndex();
      if ((pIndex & 0x1L) == 1L)
        continue; 
      mask = this.producerMask;
      buffer = this.producerBuffer;
      if (producerLimit <= pIndex) {
        int result = offerSlowPath(mask, pIndex, producerLimit);
        switch (result) {
          case 1:
            continue;
          case 2:
            return false;
          case 3:
            resize(mask, buffer, pIndex, e);
            return true;
        } 
      } 
      if (casProducerIndex(pIndex, pIndex + 2L))
        break; 
    } 
    int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(pIndex, mask);
    LinkedAtomicArrayQueueUtil.soElement(buffer, offset, e);
    return true;
  }
  
  public E poll() {
    AtomicReferenceArray<E> buffer = this.consumerBuffer;
    long index = this.consumerIndex;
    long mask = this.consumerMask;
    int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(index, mask);
    Object e = LinkedAtomicArrayQueueUtil.lvElement(buffer, offset);
    if (e == null)
      if (index != lvProducerIndex()) {
        do {
          e = LinkedAtomicArrayQueueUtil.lvElement(buffer, offset);
        } while (e == null);
      } else {
        return null;
      }  
    if (e == JUMP) {
      AtomicReferenceArray<E> nextBuffer = getNextBuffer(buffer, mask);
      return newBufferPoll(nextBuffer, index);
    } 
    LinkedAtomicArrayQueueUtil.soElement(buffer, offset, null);
    soConsumerIndex(index + 2L);
    return (E)e;
  }
  
  public E peek() {
    AtomicReferenceArray<E> buffer = this.consumerBuffer;
    long index = this.consumerIndex;
    long mask = this.consumerMask;
    int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(index, mask);
    Object e = LinkedAtomicArrayQueueUtil.lvElement(buffer, offset);
    if (e == null && index != lvProducerIndex())
      while ((e = LinkedAtomicArrayQueueUtil.<E>lvElement(buffer, offset)) == null); 
    if (e == JUMP)
      return newBufferPeek(getNextBuffer(buffer, mask), index); 
    return (E)e;
  }
  
  private int offerSlowPath(long mask, long pIndex, long producerLimit) {
    long cIndex = lvConsumerIndex();
    long bufferCapacity = getCurrentBufferCapacity(mask);
    int result = 0;
    if (cIndex + bufferCapacity > pIndex) {
      if (!casProducerLimit(producerLimit, cIndex + bufferCapacity))
        result = 1; 
    } else if (availableInQueue(pIndex, cIndex) <= 0L) {
      result = 2;
    } else if (casProducerIndex(pIndex, pIndex + 1L)) {
      result = 3;
    } else {
      result = 1;
    } 
    return result;
  }
  
  protected abstract long availableInQueue(long paramLong1, long paramLong2);
  
  private AtomicReferenceArray<E> getNextBuffer(AtomicReferenceArray<E> buffer, long mask) {
    int offset = nextArrayOffset(mask);
    AtomicReferenceArray<E> nextBuffer = LinkedAtomicArrayQueueUtil.<AtomicReferenceArray<E>>lvElement((AtomicReferenceArray)buffer, offset);
    LinkedAtomicArrayQueueUtil.soElement(buffer, offset, null);
    return nextBuffer;
  }
  
  private int nextArrayOffset(long mask) {
    return LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(mask + 2L, Long.MAX_VALUE);
  }
  
  private E newBufferPoll(AtomicReferenceArray<E> nextBuffer, long index) {
    int offset = newBufferAndOffset(nextBuffer, index);
    E n = LinkedAtomicArrayQueueUtil.lvElement(nextBuffer, offset);
    if (n == null)
      throw new IllegalStateException("new buffer must have at least one element"); 
    LinkedAtomicArrayQueueUtil.soElement(nextBuffer, offset, null);
    soConsumerIndex(index + 2L);
    return n;
  }
  
  private E newBufferPeek(AtomicReferenceArray<E> nextBuffer, long index) {
    int offset = newBufferAndOffset(nextBuffer, index);
    E n = LinkedAtomicArrayQueueUtil.lvElement(nextBuffer, offset);
    if (null == n)
      throw new IllegalStateException("new buffer must have at least one element"); 
    return n;
  }
  
  private int newBufferAndOffset(AtomicReferenceArray<E> nextBuffer, long index) {
    this.consumerBuffer = nextBuffer;
    this.consumerMask = (LinkedAtomicArrayQueueUtil.length(nextBuffer) - 2 << 1);
    int offsetInNew = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(index, this.consumerMask);
    return offsetInNew;
  }
  
  public long currentProducerIndex() {
    return lvProducerIndex() / 2L;
  }
  
  public long currentConsumerIndex() {
    return lvConsumerIndex() / 2L;
  }
  
  public abstract int capacity();
  
  public boolean relaxedOffer(E e) {
    return offer(e);
  }
  
  public E relaxedPoll() {
    AtomicReferenceArray<E> buffer = this.consumerBuffer;
    long index = this.consumerIndex;
    long mask = this.consumerMask;
    int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(index, mask);
    Object e = LinkedAtomicArrayQueueUtil.lvElement(buffer, offset);
    if (e == null)
      return null; 
    if (e == JUMP) {
      AtomicReferenceArray<E> nextBuffer = getNextBuffer(buffer, mask);
      return newBufferPoll(nextBuffer, index);
    } 
    LinkedAtomicArrayQueueUtil.soElement(buffer, offset, null);
    soConsumerIndex(index + 2L);
    return (E)e;
  }
  
  public E relaxedPeek() {
    AtomicReferenceArray<E> buffer = this.consumerBuffer;
    long index = this.consumerIndex;
    long mask = this.consumerMask;
    int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(index, mask);
    Object e = LinkedAtomicArrayQueueUtil.lvElement(buffer, offset);
    if (e == JUMP)
      return newBufferPeek(getNextBuffer(buffer, mask), index); 
    return (E)e;
  }
  
  public int fill(MessagePassingQueue.Supplier<E> s) {
    long result = 0L;
    int capacity = capacity();
    while (true) {
      int filled = fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH);
      if (filled == 0)
        return (int)result; 
      result += filled;
      if (result > capacity)
        return (int)result; 
    } 
  }
  
  public int fill(MessagePassingQueue.Supplier<E> s, int batchSize) {
    while (true) {
      long producerLimit = lvProducerLimit();
      long pIndex = lvProducerIndex();
      if ((pIndex & 0x1L) == 1L)
        continue; 
      long mask = this.producerMask;
      AtomicReferenceArray<E> buffer = this.producerBuffer;
      long batchIndex = Math.min(producerLimit, pIndex + (2 * batchSize));
      if (pIndex == producerLimit || producerLimit < batchIndex) {
        int result = offerSlowPath(mask, pIndex, producerLimit);
        switch (result) {
          case 1:
            continue;
          case 2:
            return 0;
          case 3:
            resize(mask, buffer, pIndex, (E)s.get());
            return 1;
        } 
      } 
      if (casProducerIndex(pIndex, batchIndex)) {
        int claimedSlots = (int)((batchIndex - pIndex) / 2L);
        int i = 0;
        for (i = 0; i < claimedSlots; i++) {
          int offset = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(pIndex + (2 * i), mask);
          LinkedAtomicArrayQueueUtil.soElement(buffer, offset, s.get());
        } 
        return claimedSlots;
      } 
    } 
  }
  
  public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit) {
    while (exit.keepRunning()) {
      while (fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) != 0 && exit.keepRunning());
      int idleCounter = 0;
      while (exit.keepRunning() && fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) == 0)
        idleCounter = w.idle(idleCounter); 
    } 
  }
  
  public int drain(MessagePassingQueue.Consumer<E> c) {
    return drain(c, capacity());
  }
  
  public int drain(MessagePassingQueue.Consumer<E> c, int limit) {
    int i = 0;
    E m;
    for (; i < limit && (m = relaxedPoll()) != null; i++)
      c.accept(m); 
    return i;
  }
  
  public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit) {
    int idleCounter = 0;
    while (exit.keepRunning()) {
      E e = relaxedPoll();
      if (e == null) {
        idleCounter = w.idle(idleCounter);
        continue;
      } 
      idleCounter = 0;
      c.accept(e);
    } 
  }
  
  private void resize(long oldMask, AtomicReferenceArray<E> oldBuffer, long pIndex, E e) {
    int newBufferLength = getNextBufferSize(oldBuffer);
    AtomicReferenceArray<E> newBuffer = LinkedAtomicArrayQueueUtil.allocate(newBufferLength);
    this.producerBuffer = newBuffer;
    int newMask = newBufferLength - 2 << 1;
    this.producerMask = newMask;
    int offsetInOld = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(pIndex, oldMask);
    int offsetInNew = LinkedAtomicArrayQueueUtil.modifiedCalcElementOffset(pIndex, newMask);
    LinkedAtomicArrayQueueUtil.soElement(newBuffer, offsetInNew, e);
    LinkedAtomicArrayQueueUtil.soElement(oldBuffer, nextArrayOffset(oldMask), newBuffer);
    long cIndex = lvConsumerIndex();
    long availableInQueue = availableInQueue(pIndex, cIndex);
    RangeUtil.checkPositive(availableInQueue, "availableInQueue");
    soProducerLimit(pIndex + Math.min(newMask, availableInQueue));
    soProducerIndex(pIndex + 2L);
    LinkedAtomicArrayQueueUtil.soElement(oldBuffer, offsetInOld, JUMP);
  }
  
  protected abstract int getNextBufferSize(AtomicReferenceArray<E> paramAtomicReferenceArray);
  
  protected abstract long getCurrentBufferCapacity(long paramLong);
}

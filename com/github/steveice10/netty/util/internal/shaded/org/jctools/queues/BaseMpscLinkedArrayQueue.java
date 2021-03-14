package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.Pow2;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.RangeUtil;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess;
import java.util.Iterator;

public abstract class BaseMpscLinkedArrayQueue<E> extends BaseMpscLinkedArrayQueueColdProducerFields<E> implements MessagePassingQueue<E>, QueueProgressIndicators {
  private static final Object JUMP = new Object();
  
  private static final int CONTINUE_TO_P_INDEX_CAS = 0;
  
  private static final int RETRY = 1;
  
  private static final int QUEUE_FULL = 2;
  
  private static final int QUEUE_RESIZE = 3;
  
  public BaseMpscLinkedArrayQueue(int initialCapacity) {
    RangeUtil.checkGreaterThanOrEqual(initialCapacity, 2, "initialCapacity");
    int p2capacity = Pow2.roundToPowerOfTwo(initialCapacity);
    long mask = (p2capacity - 1 << 1);
    E[] buffer = CircularArrayOffsetCalculator.allocate(p2capacity + 1);
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
    long before, currentProducerIndex, after = lvConsumerIndex();
    do {
      before = after;
      currentProducerIndex = lvProducerIndex();
      after = lvConsumerIndex();
    } while (before != after);
    long size = currentProducerIndex - after >> 1L;
    if (size > 2147483647L)
      return Integer.MAX_VALUE; 
    return (int)size;
  }
  
  public final boolean isEmpty() {
    return (lvConsumerIndex() == lvProducerIndex());
  }
  
  public String toString() {
    return getClass().getName();
  }
  
  public boolean offer(E e) {
    long pIndex, mask;
    E[] buffer;
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
    long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(pIndex, mask);
    UnsafeRefArrayAccess.soElement((Object[])buffer, offset, e);
    return true;
  }
  
  public E poll() {
    E[] buffer = this.consumerBuffer;
    long index = this.consumerIndex;
    long mask = this.consumerMask;
    long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(index, mask);
    Object e = UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
    if (e == null)
      if (index != lvProducerIndex()) {
        do {
          e = UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
        } while (e == null);
      } else {
        return null;
      }  
    if (e == JUMP) {
      E[] nextBuffer = getNextBuffer(buffer, mask);
      return newBufferPoll(nextBuffer, index);
    } 
    UnsafeRefArrayAccess.soElement((Object[])buffer, offset, null);
    soConsumerIndex(index + 2L);
    return (E)e;
  }
  
  public E peek() {
    E[] buffer = this.consumerBuffer;
    long index = this.consumerIndex;
    long mask = this.consumerMask;
    long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(index, mask);
    Object e = UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
    if (e == null && index != lvProducerIndex())
      do {
        e = UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
      } while (e == null); 
    if (e == JUMP)
      return newBufferPeek(getNextBuffer(buffer, mask), index); 
    return (E)e;
  }
  
  private int offerSlowPath(long mask, long pIndex, long producerLimit) {
    long cIndex = lvConsumerIndex();
    long bufferCapacity = getCurrentBufferCapacity(mask);
    if (cIndex + bufferCapacity > pIndex) {
      if (!casProducerLimit(producerLimit, cIndex + bufferCapacity))
        return 1; 
      return 0;
    } 
    if (availableInQueue(pIndex, cIndex) <= 0L)
      return 2; 
    if (casProducerIndex(pIndex, pIndex + 1L))
      return 3; 
    return 1;
  }
  
  protected abstract long availableInQueue(long paramLong1, long paramLong2);
  
  private E[] getNextBuffer(E[] buffer, long mask) {
    long offset = nextArrayOffset(mask);
    E[] nextBuffer = (E[])UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
    UnsafeRefArrayAccess.soElement((Object[])buffer, offset, null);
    return nextBuffer;
  }
  
  private long nextArrayOffset(long mask) {
    return LinkedArrayQueueUtil.modifiedCalcElementOffset(mask + 2L, Long.MAX_VALUE);
  }
  
  private E newBufferPoll(E[] nextBuffer, long index) {
    long offset = newBufferAndOffset(nextBuffer, index);
    E n = (E)UnsafeRefArrayAccess.lvElement((Object[])nextBuffer, offset);
    if (n == null)
      throw new IllegalStateException("new buffer must have at least one element"); 
    UnsafeRefArrayAccess.soElement((Object[])nextBuffer, offset, null);
    soConsumerIndex(index + 2L);
    return n;
  }
  
  private E newBufferPeek(E[] nextBuffer, long index) {
    long offset = newBufferAndOffset(nextBuffer, index);
    E n = (E)UnsafeRefArrayAccess.lvElement((Object[])nextBuffer, offset);
    if (null == n)
      throw new IllegalStateException("new buffer must have at least one element"); 
    return n;
  }
  
  private long newBufferAndOffset(E[] nextBuffer, long index) {
    this.consumerBuffer = nextBuffer;
    this.consumerMask = (LinkedArrayQueueUtil.length((Object[])nextBuffer) - 2 << 1);
    return LinkedArrayQueueUtil.modifiedCalcElementOffset(index, this.consumerMask);
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
    E[] buffer = this.consumerBuffer;
    long index = this.consumerIndex;
    long mask = this.consumerMask;
    long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(index, mask);
    Object e = UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
    if (e == null)
      return null; 
    if (e == JUMP) {
      E[] nextBuffer = getNextBuffer(buffer, mask);
      return newBufferPoll(nextBuffer, index);
    } 
    UnsafeRefArrayAccess.soElement((Object[])buffer, offset, null);
    soConsumerIndex(index + 2L);
    return (E)e;
  }
  
  public E relaxedPeek() {
    E[] buffer = this.consumerBuffer;
    long index = this.consumerIndex;
    long mask = this.consumerMask;
    long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(index, mask);
    Object e = UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
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
    long pIndex, mask;
    E[] buffer;
    long batchIndex;
    while (true) {
      long producerLimit = lvProducerLimit();
      pIndex = lvProducerIndex();
      if ((pIndex & 0x1L) == 1L)
        continue; 
      mask = this.producerMask;
      buffer = this.producerBuffer;
      batchIndex = Math.min(producerLimit, pIndex + (2 * batchSize));
      if (pIndex >= producerLimit || producerLimit < batchIndex) {
        int result = offerSlowPath(mask, pIndex, producerLimit);
        switch (result) {
          case 0:
          case 1:
            continue;
          case 2:
            return 0;
          case 3:
            resize(mask, buffer, pIndex, s.get());
            return 1;
        } 
      } 
      if (casProducerIndex(pIndex, batchIndex))
        break; 
    } 
    int claimedSlots = (int)((batchIndex - pIndex) / 2L);
    for (int i = 0; i < claimedSlots; i++) {
      long offset = LinkedArrayQueueUtil.modifiedCalcElementOffset(pIndex + (2 * i), mask);
      UnsafeRefArrayAccess.soElement((Object[])buffer, offset, s.get());
    } 
    return claimedSlots;
  }
  
  public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit) {
    while (exit.keepRunning()) {
      if (fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) == 0) {
        int idleCounter = 0;
        while (exit.keepRunning() && fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) == 0)
          idleCounter = w.idle(idleCounter); 
      } 
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
  
  private void resize(long oldMask, E[] oldBuffer, long pIndex, E e) {
    int newBufferLength = getNextBufferSize(oldBuffer);
    E[] newBuffer = CircularArrayOffsetCalculator.allocate(newBufferLength);
    this.producerBuffer = newBuffer;
    int newMask = newBufferLength - 2 << 1;
    this.producerMask = newMask;
    long offsetInOld = LinkedArrayQueueUtil.modifiedCalcElementOffset(pIndex, oldMask);
    long offsetInNew = LinkedArrayQueueUtil.modifiedCalcElementOffset(pIndex, newMask);
    UnsafeRefArrayAccess.soElement((Object[])newBuffer, offsetInNew, e);
    UnsafeRefArrayAccess.soElement((Object[])oldBuffer, nextArrayOffset(oldMask), newBuffer);
    long cIndex = lvConsumerIndex();
    long availableInQueue = availableInQueue(pIndex, cIndex);
    RangeUtil.checkPositive(availableInQueue, "availableInQueue");
    soProducerLimit(pIndex + Math.min(newMask, availableInQueue));
    soProducerIndex(pIndex + 2L);
    UnsafeRefArrayAccess.soElement((Object[])oldBuffer, offsetInOld, JUMP);
  }
  
  protected abstract int getNextBufferSize(E[] paramArrayOfE);
  
  protected abstract long getCurrentBufferCapacity(long paramLong);
}

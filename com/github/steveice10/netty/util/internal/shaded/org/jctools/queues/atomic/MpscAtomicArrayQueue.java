package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.atomic;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class MpscAtomicArrayQueue<E> extends MpscAtomicArrayQueueL3Pad<E> {
  public MpscAtomicArrayQueue(int capacity) {
    super(capacity);
  }
  
  public boolean offerIfBelowThreshold(E e, int threshold) {
    long pIndex;
    if (null == e)
      throw new NullPointerException(); 
    int mask = this.mask;
    long capacity = (mask + 1);
    long producerLimit = lvProducerLimit();
    do {
      pIndex = lvProducerIndex();
      long available = producerLimit - pIndex;
      long size = capacity - available;
      if (size < threshold)
        continue; 
      long cIndex = lvConsumerIndex();
      size = pIndex - cIndex;
      if (size >= threshold)
        return false; 
      producerLimit = cIndex + capacity;
      soProducerLimit(producerLimit);
    } while (!casProducerIndex(pIndex, pIndex + 1L));
    int offset = calcElementOffset(pIndex, mask);
    soElement(this.buffer, offset, e);
    return true;
  }
  
  public boolean offer(E e) {
    long pIndex;
    if (null == e)
      throw new NullPointerException(); 
    int mask = this.mask;
    long producerLimit = lvProducerLimit();
    do {
      pIndex = lvProducerIndex();
      if (pIndex < producerLimit)
        continue; 
      long cIndex = lvConsumerIndex();
      producerLimit = cIndex + mask + 1L;
      if (pIndex >= producerLimit)
        return false; 
      soProducerLimit(producerLimit);
    } while (!casProducerIndex(pIndex, pIndex + 1L));
    int offset = calcElementOffset(pIndex, mask);
    soElement(this.buffer, offset, e);
    return true;
  }
  
  public final int failFastOffer(E e) {
    if (null == e)
      throw new NullPointerException(); 
    int mask = this.mask;
    long capacity = (mask + 1);
    long pIndex = lvProducerIndex();
    long producerLimit = lvProducerLimit();
    if (pIndex >= producerLimit) {
      long cIndex = lvConsumerIndex();
      producerLimit = cIndex + capacity;
      if (pIndex >= producerLimit)
        return 1; 
      soProducerLimit(producerLimit);
    } 
    if (!casProducerIndex(pIndex, pIndex + 1L))
      return -1; 
    int offset = calcElementOffset(pIndex, mask);
    soElement(this.buffer, offset, e);
    return 0;
  }
  
  public E poll() {
    long cIndex = lpConsumerIndex();
    int offset = calcElementOffset(cIndex);
    AtomicReferenceArray<E> buffer = this.buffer;
    E e = lvElement(buffer, offset);
    if (null == e)
      if (cIndex != lvProducerIndex()) {
        do {
          e = lvElement(buffer, offset);
        } while (e == null);
      } else {
        return null;
      }  
    spElement(buffer, offset, null);
    soConsumerIndex(cIndex + 1L);
    return e;
  }
  
  public E peek() {
    AtomicReferenceArray<E> buffer = this.buffer;
    long cIndex = lpConsumerIndex();
    int offset = calcElementOffset(cIndex);
    E e = lvElement(buffer, offset);
    if (null == e)
      if (cIndex != lvProducerIndex()) {
        do {
          e = lvElement(buffer, offset);
        } while (e == null);
      } else {
        return null;
      }  
    return e;
  }
  
  public boolean relaxedOffer(E e) {
    return offer(e);
  }
  
  public E relaxedPoll() {
    AtomicReferenceArray<E> buffer = this.buffer;
    long cIndex = lpConsumerIndex();
    int offset = calcElementOffset(cIndex);
    E e = lvElement(buffer, offset);
    if (null == e)
      return null; 
    spElement(buffer, offset, null);
    soConsumerIndex(cIndex + 1L);
    return e;
  }
  
  public E relaxedPeek() {
    AtomicReferenceArray<E> buffer = this.buffer;
    int mask = this.mask;
    long cIndex = lpConsumerIndex();
    return lvElement(buffer, calcElementOffset(cIndex, mask));
  }
  
  public int drain(MessagePassingQueue.Consumer<E> c) {
    return drain(c, capacity());
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
  
  public int drain(MessagePassingQueue.Consumer<E> c, int limit) {
    AtomicReferenceArray<E> buffer = this.buffer;
    int mask = this.mask;
    long cIndex = lpConsumerIndex();
    for (int i = 0; i < limit; i++) {
      long index = cIndex + i;
      int offset = calcElementOffset(index, mask);
      E e = lvElement(buffer, offset);
      if (null == e)
        return i; 
      spElement(buffer, offset, null);
      soConsumerIndex(index + 1L);
      c.accept(e);
    } 
    return limit;
  }
  
  public int fill(MessagePassingQueue.Supplier<E> s, int limit) {
    long pIndex;
    int mask = this.mask;
    long capacity = (mask + 1);
    long producerLimit = lvProducerLimit();
    int actualLimit = 0;
    do {
      pIndex = lvProducerIndex();
      long available = producerLimit - pIndex;
      if (available <= 0L) {
        long cIndex = lvConsumerIndex();
        producerLimit = cIndex + capacity;
        available = producerLimit - pIndex;
        if (available <= 0L)
          return 0; 
        soProducerLimit(producerLimit);
      } 
      actualLimit = Math.min((int)available, limit);
    } while (!casProducerIndex(pIndex, pIndex + actualLimit));
    AtomicReferenceArray<E> buffer = this.buffer;
    for (int i = 0; i < actualLimit; i++) {
      int offset = calcElementOffset(pIndex + i, mask);
      soElement(buffer, offset, (E)s.get());
    } 
    return actualLimit;
  }
  
  public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit) {
    AtomicReferenceArray<E> buffer = this.buffer;
    int mask = this.mask;
    long cIndex = lpConsumerIndex();
    int counter = 0;
    while (exit.keepRunning()) {
      for (int i = 0; i < 4096; i++) {
        int offset = calcElementOffset(cIndex, mask);
        E e = lvElement(buffer, offset);
        if (null == e) {
          counter = w.idle(counter);
        } else {
          cIndex++;
          counter = 0;
          spElement(buffer, offset, null);
          soConsumerIndex(cIndex);
          c.accept(e);
        } 
      } 
    } 
  }
  
  public void fill(MessagePassingQueue.Supplier<E> s, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit) {
    int idleCounter = 0;
    while (exit.keepRunning()) {
      if (fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH) == 0) {
        idleCounter = w.idle(idleCounter);
        continue;
      } 
      idleCounter = 0;
    } 
  }
  
  @Deprecated
  public int weakOffer(E e) {
    return failFastOffer(e);
  }
}

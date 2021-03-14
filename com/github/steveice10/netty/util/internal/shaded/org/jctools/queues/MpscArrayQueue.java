package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess;

public class MpscArrayQueue<E> extends MpscArrayQueueL3Pad<E> {
  public MpscArrayQueue(int capacity) {
    super(capacity);
  }
  
  public boolean offerIfBelowThreshold(E e, int threshold) {
    long pIndex;
    if (null == e)
      throw new NullPointerException(); 
    long mask = this.mask;
    long capacity = mask + 1L;
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
    long offset = calcElementOffset(pIndex, mask);
    UnsafeRefArrayAccess.soElement((Object[])this.buffer, offset, e);
    return true;
  }
  
  public boolean offer(E e) {
    long pIndex;
    if (null == e)
      throw new NullPointerException(); 
    long mask = this.mask;
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
    long offset = calcElementOffset(pIndex, mask);
    UnsafeRefArrayAccess.soElement((Object[])this.buffer, offset, e);
    return true;
  }
  
  public final int failFastOffer(E e) {
    if (null == e)
      throw new NullPointerException(); 
    long mask = this.mask;
    long capacity = mask + 1L;
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
    long offset = calcElementOffset(pIndex, mask);
    UnsafeRefArrayAccess.soElement((Object[])this.buffer, offset, e);
    return 0;
  }
  
  public E poll() {
    long cIndex = lpConsumerIndex();
    long offset = calcElementOffset(cIndex);
    E[] buffer = this.buffer;
    E e = (E)UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
    if (null == e)
      if (cIndex != lvProducerIndex()) {
        do {
          e = (E)UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
        } while (e == null);
      } else {
        return null;
      }  
    UnsafeRefArrayAccess.spElement((Object[])buffer, offset, null);
    soConsumerIndex(cIndex + 1L);
    return e;
  }
  
  public E peek() {
    E[] buffer = this.buffer;
    long cIndex = lpConsumerIndex();
    long offset = calcElementOffset(cIndex);
    E e = (E)UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
    if (null == e)
      if (cIndex != lvProducerIndex()) {
        do {
          e = (E)UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
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
    E[] buffer = this.buffer;
    long cIndex = lpConsumerIndex();
    long offset = calcElementOffset(cIndex);
    E e = (E)UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
    if (null == e)
      return null; 
    UnsafeRefArrayAccess.spElement((Object[])buffer, offset, null);
    soConsumerIndex(cIndex + 1L);
    return e;
  }
  
  public E relaxedPeek() {
    E[] buffer = this.buffer;
    long mask = this.mask;
    long cIndex = lpConsumerIndex();
    return (E)UnsafeRefArrayAccess.lvElement((Object[])buffer, calcElementOffset(cIndex, mask));
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
    E[] buffer = this.buffer;
    long mask = this.mask;
    long cIndex = lpConsumerIndex();
    for (int i = 0; i < limit; i++) {
      long index = cIndex + i;
      long offset = calcElementOffset(index, mask);
      E e = (E)UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
      if (null == e)
        return i; 
      UnsafeRefArrayAccess.spElement((Object[])buffer, offset, null);
      soConsumerIndex(index + 1L);
      c.accept(e);
    } 
    return limit;
  }
  
  public int fill(MessagePassingQueue.Supplier<E> s, int limit) {
    long pIndex, mask = this.mask;
    long capacity = mask + 1L;
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
    E[] buffer = this.buffer;
    for (int i = 0; i < actualLimit; i++) {
      long offset = calcElementOffset(pIndex + i, mask);
      UnsafeRefArrayAccess.soElement((Object[])buffer, offset, s.get());
    } 
    return actualLimit;
  }
  
  public void drain(MessagePassingQueue.Consumer<E> c, MessagePassingQueue.WaitStrategy w, MessagePassingQueue.ExitCondition exit) {
    E[] buffer = this.buffer;
    long mask = this.mask;
    long cIndex = lpConsumerIndex();
    int counter = 0;
    while (exit.keepRunning()) {
      for (int i = 0; i < 4096; i++) {
        long offset = calcElementOffset(cIndex, mask);
        E e = (E)UnsafeRefArrayAccess.lvElement((Object[])buffer, offset);
        if (null == e) {
          counter = w.idle(counter);
        } else {
          cIndex++;
          counter = 0;
          UnsafeRefArrayAccess.spElement((Object[])buffer, offset, null);
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
}

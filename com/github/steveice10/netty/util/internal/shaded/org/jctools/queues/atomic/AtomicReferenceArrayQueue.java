package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.atomic;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.IndexedQueueSizeUtil;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.queues.QueueProgressIndicators;
import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.Pow2;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReferenceArray;

abstract class AtomicReferenceArrayQueue<E> extends AbstractQueue<E> implements IndexedQueueSizeUtil.IndexedQueue, QueueProgressIndicators, MessagePassingQueue<E> {
  protected final AtomicReferenceArray<E> buffer;
  
  protected final int mask;
  
  public AtomicReferenceArrayQueue(int capacity) {
    int actualCapacity = Pow2.roundToPowerOfTwo(capacity);
    this.mask = actualCapacity - 1;
    this.buffer = new AtomicReferenceArray<E>(actualCapacity);
  }
  
  public Iterator<E> iterator() {
    throw new UnsupportedOperationException();
  }
  
  public String toString() {
    return getClass().getName();
  }
  
  public void clear() {
    while (poll() != null);
  }
  
  protected final int calcElementOffset(long index, int mask) {
    return (int)index & mask;
  }
  
  protected final int calcElementOffset(long index) {
    return (int)index & this.mask;
  }
  
  public static <E> E lvElement(AtomicReferenceArray<E> buffer, int offset) {
    return buffer.get(offset);
  }
  
  public static <E> E lpElement(AtomicReferenceArray<E> buffer, int offset) {
    return buffer.get(offset);
  }
  
  protected final E lpElement(int offset) {
    return this.buffer.get(offset);
  }
  
  public static <E> void spElement(AtomicReferenceArray<E> buffer, int offset, E value) {
    buffer.lazySet(offset, value);
  }
  
  protected final void spElement(int offset, E value) {
    this.buffer.lazySet(offset, value);
  }
  
  public static <E> void soElement(AtomicReferenceArray<E> buffer, int offset, E value) {
    buffer.lazySet(offset, value);
  }
  
  protected final void soElement(int offset, E value) {
    this.buffer.lazySet(offset, value);
  }
  
  public static <E> void svElement(AtomicReferenceArray<E> buffer, int offset, E value) {
    buffer.set(offset, value);
  }
  
  protected final E lvElement(int offset) {
    return lvElement(this.buffer, offset);
  }
  
  public final int capacity() {
    return this.mask + 1;
  }
  
  public final int size() {
    return IndexedQueueSizeUtil.size(this);
  }
  
  public final boolean isEmpty() {
    return IndexedQueueSizeUtil.isEmpty(this);
  }
  
  public final long currentProducerIndex() {
    return lvProducerIndex();
  }
  
  public final long currentConsumerIndex() {
    return lvConsumerIndex();
  }
}

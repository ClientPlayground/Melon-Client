package com.github.steveice10.netty.util.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class EmptyPriorityQueue<T> implements PriorityQueue<T> {
  private static final PriorityQueue<Object> INSTANCE = new EmptyPriorityQueue();
  
  public static <V> EmptyPriorityQueue<V> instance() {
    return (EmptyPriorityQueue)INSTANCE;
  }
  
  public boolean removeTyped(T node) {
    return false;
  }
  
  public boolean containsTyped(T node) {
    return false;
  }
  
  public void priorityChanged(T node) {}
  
  public int size() {
    return 0;
  }
  
  public boolean isEmpty() {
    return true;
  }
  
  public boolean contains(Object o) {
    return false;
  }
  
  public Iterator<T> iterator() {
    return Collections.<T>emptyList().iterator();
  }
  
  public Object[] toArray() {
    return EmptyArrays.EMPTY_OBJECTS;
  }
  
  public <T1> T1[] toArray(T1[] a) {
    if (a.length > 0)
      a[0] = null; 
    return a;
  }
  
  public boolean add(T t) {
    return false;
  }
  
  public boolean remove(Object o) {
    return false;
  }
  
  public boolean containsAll(Collection<?> c) {
    return false;
  }
  
  public boolean addAll(Collection<? extends T> c) {
    return false;
  }
  
  public boolean removeAll(Collection<?> c) {
    return false;
  }
  
  public boolean retainAll(Collection<?> c) {
    return false;
  }
  
  public void clear() {}
  
  public void clearIgnoringIndexes() {}
  
  public boolean equals(Object o) {
    return (o instanceof PriorityQueue && ((PriorityQueue)o).isEmpty());
  }
  
  public int hashCode() {
    return 0;
  }
  
  public boolean offer(T t) {
    return false;
  }
  
  public T remove() {
    throw new NoSuchElementException();
  }
  
  public T poll() {
    return null;
  }
  
  public T element() {
    throw new NoSuchElementException();
  }
  
  public T peek() {
    return null;
  }
  
  public String toString() {
    return EmptyPriorityQueue.class.getSimpleName();
  }
}

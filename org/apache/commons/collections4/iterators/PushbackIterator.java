package org.apache.commons.collections4.iterators;

import java.util.Iterator;
import org.apache.commons.collections4.ArrayStack;

public class PushbackIterator<E> implements Iterator<E> {
  private final Iterator<? extends E> iterator;
  
  private ArrayStack<E> items = new ArrayStack();
  
  public static <E> PushbackIterator<E> pushbackIterator(Iterator<? extends E> iterator) {
    if (iterator == null)
      throw new IllegalArgumentException("Iterator must not be null"); 
    if (iterator instanceof PushbackIterator) {
      PushbackIterator<E> it = (PushbackIterator)iterator;
      return it;
    } 
    return new PushbackIterator<E>(iterator);
  }
  
  public PushbackIterator(Iterator<? extends E> iterator) {
    this.iterator = iterator;
  }
  
  public void pushback(E item) {
    this.items.push(item);
  }
  
  public boolean hasNext() {
    return !this.items.isEmpty() ? true : this.iterator.hasNext();
  }
  
  public E next() {
    return !this.items.isEmpty() ? (E)this.items.pop() : this.iterator.next();
  }
  
  public void remove() {
    throw new UnsupportedOperationException();
  }
}

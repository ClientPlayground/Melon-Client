package org.apache.commons.collections4.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PeekingIterator<E> implements Iterator<E> {
  private final Iterator<? extends E> iterator;
  
  private boolean exhausted = false;
  
  private boolean slotFilled = false;
  
  private E slot;
  
  public static <E> PeekingIterator<E> peekingIterator(Iterator<? extends E> iterator) {
    if (iterator == null)
      throw new IllegalArgumentException("Iterator must not be null"); 
    if (iterator instanceof PeekingIterator) {
      PeekingIterator<E> it = (PeekingIterator)iterator;
      return it;
    } 
    return new PeekingIterator<E>(iterator);
  }
  
  public PeekingIterator(Iterator<? extends E> iterator) {
    this.iterator = iterator;
  }
  
  private void fill() {
    if (this.exhausted || this.slotFilled)
      return; 
    if (this.iterator.hasNext()) {
      this.slot = this.iterator.next();
      this.slotFilled = true;
    } else {
      this.exhausted = true;
      this.slot = null;
      this.slotFilled = false;
    } 
  }
  
  public boolean hasNext() {
    if (this.exhausted)
      return false; 
    return this.slotFilled ? true : this.iterator.hasNext();
  }
  
  public E peek() {
    fill();
    return this.exhausted ? null : this.slot;
  }
  
  public E element() {
    fill();
    if (this.exhausted)
      throw new NoSuchElementException(); 
    return this.slot;
  }
  
  public E next() {
    if (!hasNext())
      throw new NoSuchElementException(); 
    E x = this.slotFilled ? this.slot : this.iterator.next();
    this.slot = null;
    this.slotFilled = false;
    return x;
  }
  
  public void remove() {
    if (this.slotFilled)
      throw new IllegalStateException(); 
    this.iterator.remove();
  }
}

package org.apache.commons.collections4.iterators;

import java.util.Iterator;

public abstract class LazyIteratorChain<E> implements Iterator<E> {
  private int callCounter = 0;
  
  private boolean chainExhausted = false;
  
  private Iterator<? extends E> currentIterator = null;
  
  private Iterator<? extends E> lastUsedIterator = null;
  
  protected abstract Iterator<? extends E> nextIterator(int paramInt);
  
  private void updateCurrentIterator() {
    if (this.callCounter == 0) {
      this.currentIterator = nextIterator(++this.callCounter);
      if (this.currentIterator == null) {
        this.currentIterator = EmptyIterator.emptyIterator();
        this.chainExhausted = true;
      } 
      this.lastUsedIterator = this.currentIterator;
    } 
    while (!this.currentIterator.hasNext() && !this.chainExhausted) {
      Iterator<? extends E> nextIterator = nextIterator(++this.callCounter);
      if (nextIterator != null) {
        this.currentIterator = nextIterator;
        continue;
      } 
      this.chainExhausted = true;
    } 
  }
  
  public boolean hasNext() {
    updateCurrentIterator();
    this.lastUsedIterator = this.currentIterator;
    return this.currentIterator.hasNext();
  }
  
  public E next() {
    updateCurrentIterator();
    this.lastUsedIterator = this.currentIterator;
    return this.currentIterator.next();
  }
  
  public void remove() {
    if (this.currentIterator == null)
      updateCurrentIterator(); 
    this.lastUsedIterator.remove();
  }
}

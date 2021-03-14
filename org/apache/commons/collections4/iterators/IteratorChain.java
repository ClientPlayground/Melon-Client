package org.apache.commons.collections4.iterators;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class IteratorChain<E> implements Iterator<E> {
  private final Queue<Iterator<? extends E>> iteratorChain = new LinkedList<Iterator<? extends E>>();
  
  private Iterator<? extends E> currentIterator = null;
  
  private Iterator<? extends E> lastUsedIterator = null;
  
  private boolean isLocked = false;
  
  public IteratorChain() {}
  
  public IteratorChain(Iterator<? extends E> iterator) {
    addIterator(iterator);
  }
  
  public IteratorChain(Iterator<? extends E> first, Iterator<? extends E> second) {
    addIterator(first);
    addIterator(second);
  }
  
  public IteratorChain(Iterator<? extends E>... iteratorChain) {
    for (Iterator<? extends E> element : iteratorChain)
      addIterator(element); 
  }
  
  public IteratorChain(Collection<Iterator<? extends E>> iteratorChain) {
    for (Iterator<? extends E> iterator : iteratorChain)
      addIterator(iterator); 
  }
  
  public void addIterator(Iterator<? extends E> iterator) {
    checkLocked();
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    this.iteratorChain.add(iterator);
  }
  
  public int size() {
    return this.iteratorChain.size();
  }
  
  public boolean isLocked() {
    return this.isLocked;
  }
  
  private void checkLocked() {
    if (this.isLocked == true)
      throw new UnsupportedOperationException("IteratorChain cannot be changed after the first use of a method from the Iterator interface"); 
  }
  
  private void lockChain() {
    if (!this.isLocked)
      this.isLocked = true; 
  }
  
  protected void updateCurrentIterator() {
    if (this.currentIterator == null) {
      if (this.iteratorChain.isEmpty()) {
        this.currentIterator = EmptyIterator.emptyIterator();
      } else {
        this.currentIterator = this.iteratorChain.remove();
      } 
      this.lastUsedIterator = this.currentIterator;
    } 
    while (!this.currentIterator.hasNext() && !this.iteratorChain.isEmpty())
      this.currentIterator = this.iteratorChain.remove(); 
  }
  
  public boolean hasNext() {
    lockChain();
    updateCurrentIterator();
    this.lastUsedIterator = this.currentIterator;
    return this.currentIterator.hasNext();
  }
  
  public E next() {
    lockChain();
    updateCurrentIterator();
    this.lastUsedIterator = this.currentIterator;
    return this.currentIterator.next();
  }
  
  public void remove() {
    lockChain();
    if (this.currentIterator == null)
      updateCurrentIterator(); 
    this.lastUsedIterator.remove();
  }
}

package org.apache.commons.collections4.iterators;

import java.util.NoSuchElementException;
import org.apache.commons.collections4.ResettableIterator;

public class SingletonIterator<E> implements ResettableIterator<E> {
  private final boolean removeAllowed;
  
  private boolean beforeFirst = true;
  
  private boolean removed = false;
  
  private E object;
  
  public SingletonIterator(E object) {
    this(object, true);
  }
  
  public SingletonIterator(E object, boolean removeAllowed) {
    this.object = object;
    this.removeAllowed = removeAllowed;
  }
  
  public boolean hasNext() {
    return (this.beforeFirst && !this.removed);
  }
  
  public E next() {
    if (!this.beforeFirst || this.removed)
      throw new NoSuchElementException(); 
    this.beforeFirst = false;
    return this.object;
  }
  
  public void remove() {
    if (this.removeAllowed) {
      if (this.removed || this.beforeFirst)
        throw new IllegalStateException(); 
      this.object = null;
      this.removed = true;
    } else {
      throw new UnsupportedOperationException();
    } 
  }
  
  public void reset() {
    this.beforeFirst = true;
  }
}

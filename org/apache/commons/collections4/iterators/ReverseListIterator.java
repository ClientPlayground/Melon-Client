package org.apache.commons.collections4.iterators;

import java.util.List;
import java.util.ListIterator;
import org.apache.commons.collections4.ResettableListIterator;

public class ReverseListIterator<E> implements ResettableListIterator<E> {
  private final List<E> list;
  
  private ListIterator<E> iterator;
  
  private boolean validForUpdate = true;
  
  public ReverseListIterator(List<E> list) {
    this.list = list;
    this.iterator = list.listIterator(list.size());
  }
  
  public boolean hasNext() {
    return this.iterator.hasPrevious();
  }
  
  public E next() {
    E obj = this.iterator.previous();
    this.validForUpdate = true;
    return obj;
  }
  
  public int nextIndex() {
    return this.iterator.previousIndex();
  }
  
  public boolean hasPrevious() {
    return this.iterator.hasNext();
  }
  
  public E previous() {
    E obj = this.iterator.next();
    this.validForUpdate = true;
    return obj;
  }
  
  public int previousIndex() {
    return this.iterator.nextIndex();
  }
  
  public void remove() {
    if (!this.validForUpdate)
      throw new IllegalStateException("Cannot remove from list until next() or previous() called"); 
    this.iterator.remove();
  }
  
  public void set(E obj) {
    if (!this.validForUpdate)
      throw new IllegalStateException("Cannot set to list until next() or previous() called"); 
    this.iterator.set(obj);
  }
  
  public void add(E obj) {
    if (!this.validForUpdate)
      throw new IllegalStateException("Cannot add to list until next() or previous() called"); 
    this.validForUpdate = false;
    this.iterator.add(obj);
    this.iterator.previous();
  }
  
  public void reset() {
    this.iterator = this.list.listIterator(this.list.size());
  }
}

package org.apache.commons.collections4.iterators;

import java.util.ListIterator;

public class AbstractListIteratorDecorator<E> implements ListIterator<E> {
  private final ListIterator<E> iterator;
  
  public AbstractListIteratorDecorator(ListIterator<E> iterator) {
    if (iterator == null)
      throw new IllegalArgumentException("ListIterator must not be null"); 
    this.iterator = iterator;
  }
  
  protected ListIterator<E> getListIterator() {
    return this.iterator;
  }
  
  public boolean hasNext() {
    return this.iterator.hasNext();
  }
  
  public E next() {
    return this.iterator.next();
  }
  
  public int nextIndex() {
    return this.iterator.nextIndex();
  }
  
  public boolean hasPrevious() {
    return this.iterator.hasPrevious();
  }
  
  public E previous() {
    return this.iterator.previous();
  }
  
  public int previousIndex() {
    return this.iterator.previousIndex();
  }
  
  public void remove() {
    this.iterator.remove();
  }
  
  public void set(E obj) {
    this.iterator.set(obj);
  }
  
  public void add(E obj) {
    this.iterator.add(obj);
  }
}

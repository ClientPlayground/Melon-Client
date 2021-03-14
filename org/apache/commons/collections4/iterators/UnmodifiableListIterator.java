package org.apache.commons.collections4.iterators;

import java.util.ListIterator;
import org.apache.commons.collections4.Unmodifiable;

public final class UnmodifiableListIterator<E> implements ListIterator<E>, Unmodifiable {
  private final ListIterator<? extends E> iterator;
  
  public static <E> ListIterator<E> umodifiableListIterator(ListIterator<? extends E> iterator) {
    if (iterator == null)
      throw new IllegalArgumentException("ListIterator must not be null"); 
    if (iterator instanceof Unmodifiable)
      return (ListIterator)iterator; 
    return new UnmodifiableListIterator<E>(iterator);
  }
  
  private UnmodifiableListIterator(ListIterator<? extends E> iterator) {
    this.iterator = iterator;
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
    throw new UnsupportedOperationException("remove() is not supported");
  }
  
  public void set(E obj) {
    throw new UnsupportedOperationException("set() is not supported");
  }
  
  public void add(E obj) {
    throw new UnsupportedOperationException("add() is not supported");
  }
}

package org.apache.commons.collections4.iterators;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.apache.commons.collections4.ResettableListIterator;

public class ListIteratorWrapper<E> implements ResettableListIterator<E> {
  private static final String UNSUPPORTED_OPERATION_MESSAGE = "ListIteratorWrapper does not support optional operations of ListIterator.";
  
  private static final String CANNOT_REMOVE_MESSAGE = "Cannot remove element at index {0}.";
  
  private final Iterator<? extends E> iterator;
  
  private final List<E> list = new ArrayList<E>();
  
  private int currentIndex = 0;
  
  private int wrappedIteratorIndex = 0;
  
  private boolean removeState;
  
  public ListIteratorWrapper(Iterator<? extends E> iterator) {
    if (iterator == null)
      throw new NullPointerException("Iterator must not be null"); 
    this.iterator = iterator;
  }
  
  public void add(E obj) throws UnsupportedOperationException {
    if (this.iterator instanceof ListIterator) {
      ListIterator<E> li = (ListIterator)this.iterator;
      li.add(obj);
      return;
    } 
    throw new UnsupportedOperationException("ListIteratorWrapper does not support optional operations of ListIterator.");
  }
  
  public boolean hasNext() {
    if (this.currentIndex == this.wrappedIteratorIndex || this.iterator instanceof ListIterator)
      return this.iterator.hasNext(); 
    return true;
  }
  
  public boolean hasPrevious() {
    if (this.iterator instanceof ListIterator) {
      ListIterator<?> li = (ListIterator)this.iterator;
      return li.hasPrevious();
    } 
    return (this.currentIndex > 0);
  }
  
  public E next() throws NoSuchElementException {
    if (this.iterator instanceof ListIterator)
      return this.iterator.next(); 
    if (this.currentIndex < this.wrappedIteratorIndex) {
      this.currentIndex++;
      return this.list.get(this.currentIndex - 1);
    } 
    E retval = this.iterator.next();
    this.list.add(retval);
    this.currentIndex++;
    this.wrappedIteratorIndex++;
    this.removeState = true;
    return retval;
  }
  
  public int nextIndex() {
    if (this.iterator instanceof ListIterator) {
      ListIterator<?> li = (ListIterator)this.iterator;
      return li.nextIndex();
    } 
    return this.currentIndex;
  }
  
  public E previous() throws NoSuchElementException {
    if (this.iterator instanceof ListIterator) {
      ListIterator<E> li = (ListIterator)this.iterator;
      return li.previous();
    } 
    if (this.currentIndex == 0)
      throw new NoSuchElementException(); 
    this.removeState = (this.wrappedIteratorIndex == this.currentIndex);
    return this.list.get(--this.currentIndex);
  }
  
  public int previousIndex() {
    if (this.iterator instanceof ListIterator) {
      ListIterator<?> li = (ListIterator)this.iterator;
      return li.previousIndex();
    } 
    return this.currentIndex - 1;
  }
  
  public void remove() throws UnsupportedOperationException {
    if (this.iterator instanceof ListIterator) {
      this.iterator.remove();
      return;
    } 
    int removeIndex = this.currentIndex;
    if (this.currentIndex == this.wrappedIteratorIndex)
      removeIndex--; 
    if (!this.removeState || this.wrappedIteratorIndex - this.currentIndex > 1)
      throw new IllegalStateException(MessageFormat.format("Cannot remove element at index {0}.", new Object[] { Integer.valueOf(removeIndex) })); 
    this.iterator.remove();
    this.list.remove(removeIndex);
    this.currentIndex = removeIndex;
    this.wrappedIteratorIndex--;
    this.removeState = false;
  }
  
  public void set(E obj) throws UnsupportedOperationException {
    if (this.iterator instanceof ListIterator) {
      ListIterator<E> li = (ListIterator)this.iterator;
      li.set(obj);
      return;
    } 
    throw new UnsupportedOperationException("ListIteratorWrapper does not support optional operations of ListIterator.");
  }
  
  public void reset() {
    if (this.iterator instanceof ListIterator) {
      ListIterator<?> li = (ListIterator)this.iterator;
      while (li.previousIndex() >= 0)
        li.previous(); 
      return;
    } 
    this.currentIndex = 0;
  }
}

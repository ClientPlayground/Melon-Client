package org.apache.commons.collections4.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.collections4.ArrayStack;
import org.apache.commons.collections4.Transformer;

public class ObjectGraphIterator<E> implements Iterator<E> {
  private final ArrayStack<Iterator<? extends E>> stack = new ArrayStack(8);
  
  private E root;
  
  private final Transformer<? super E, ? extends E> transformer;
  
  private boolean hasNext = false;
  
  private Iterator<? extends E> currentIterator;
  
  private E currentValue;
  
  private Iterator<? extends E> lastUsedIterator;
  
  public ObjectGraphIterator(E root, Transformer<? super E, ? extends E> transformer) {
    if (root instanceof Iterator) {
      this.currentIterator = (Iterator<? extends E>)root;
    } else {
      this.root = root;
    } 
    this.transformer = transformer;
  }
  
  public ObjectGraphIterator(Iterator<? extends E> rootIterator) {
    this.currentIterator = rootIterator;
    this.transformer = null;
  }
  
  protected void updateCurrentIterator() {
    if (this.hasNext)
      return; 
    if (this.currentIterator == null) {
      if (this.root != null) {
        if (this.transformer == null) {
          findNext(this.root);
        } else {
          findNext((E)this.transformer.transform(this.root));
        } 
        this.root = null;
      } 
    } else {
      findNextByIterator(this.currentIterator);
    } 
  }
  
  protected void findNext(E value) {
    if (value instanceof Iterator) {
      findNextByIterator((Iterator<? extends E>)value);
    } else {
      this.currentValue = value;
      this.hasNext = true;
    } 
  }
  
  protected void findNextByIterator(Iterator<? extends E> iterator) {
    if (iterator != this.currentIterator) {
      if (this.currentIterator != null)
        this.stack.push(this.currentIterator); 
      this.currentIterator = iterator;
    } 
    while (this.currentIterator.hasNext() && !this.hasNext) {
      E next = this.currentIterator.next();
      if (this.transformer != null)
        next = (E)this.transformer.transform(next); 
      findNext(next);
    } 
    if (!this.hasNext && !this.stack.isEmpty()) {
      this.currentIterator = (Iterator<? extends E>)this.stack.pop();
      findNextByIterator(this.currentIterator);
    } 
  }
  
  public boolean hasNext() {
    updateCurrentIterator();
    return this.hasNext;
  }
  
  public E next() {
    updateCurrentIterator();
    if (!this.hasNext)
      throw new NoSuchElementException("No more elements in the iteration"); 
    this.lastUsedIterator = this.currentIterator;
    E result = this.currentValue;
    this.currentValue = null;
    this.hasNext = false;
    return result;
  }
  
  public void remove() {
    if (this.lastUsedIterator == null)
      throw new IllegalStateException("Iterator remove() cannot be called at this time"); 
    this.lastUsedIterator.remove();
    this.lastUsedIterator = null;
  }
}

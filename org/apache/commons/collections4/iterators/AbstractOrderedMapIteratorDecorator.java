package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.OrderedMapIterator;

public class AbstractOrderedMapIteratorDecorator<K, V> implements OrderedMapIterator<K, V> {
  private final OrderedMapIterator<K, V> iterator;
  
  public AbstractOrderedMapIteratorDecorator(OrderedMapIterator<K, V> iterator) {
    if (iterator == null)
      throw new IllegalArgumentException("OrderedMapIterator must not be null"); 
    this.iterator = iterator;
  }
  
  protected OrderedMapIterator<K, V> getOrderedMapIterator() {
    return this.iterator;
  }
  
  public boolean hasNext() {
    return this.iterator.hasNext();
  }
  
  public K next() {
    return (K)this.iterator.next();
  }
  
  public boolean hasPrevious() {
    return this.iterator.hasPrevious();
  }
  
  public K previous() {
    return (K)this.iterator.previous();
  }
  
  public void remove() {
    this.iterator.remove();
  }
  
  public K getKey() {
    return (K)this.iterator.getKey();
  }
  
  public V getValue() {
    return (V)this.iterator.getValue();
  }
  
  public V setValue(V obj) {
    return (V)this.iterator.setValue(obj);
  }
}

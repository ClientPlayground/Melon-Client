package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.MapIterator;

public class AbstractMapIteratorDecorator<K, V> implements MapIterator<K, V> {
  private final MapIterator<K, V> iterator;
  
  public AbstractMapIteratorDecorator(MapIterator<K, V> iterator) {
    if (iterator == null)
      throw new IllegalArgumentException("MapIterator must not be null"); 
    this.iterator = iterator;
  }
  
  protected MapIterator<K, V> getMapIterator() {
    return this.iterator;
  }
  
  public boolean hasNext() {
    return this.iterator.hasNext();
  }
  
  public K next() {
    return (K)this.iterator.next();
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

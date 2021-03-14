package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.Unmodifiable;

public final class UnmodifiableOrderedMapIterator<K, V> implements OrderedMapIterator<K, V>, Unmodifiable {
  private final OrderedMapIterator<? extends K, ? extends V> iterator;
  
  public static <K, V> OrderedMapIterator<K, V> unmodifiableOrderedMapIterator(OrderedMapIterator<K, ? extends V> iterator) {
    if (iterator == null)
      throw new IllegalArgumentException("OrderedMapIterator must not be null"); 
    if (iterator instanceof Unmodifiable)
      return (OrderedMapIterator)iterator; 
    return new UnmodifiableOrderedMapIterator<K, V>(iterator);
  }
  
  private UnmodifiableOrderedMapIterator(OrderedMapIterator<K, ? extends V> iterator) {
    this.iterator = iterator;
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
  
  public K getKey() {
    return (K)this.iterator.getKey();
  }
  
  public V getValue() {
    return (V)this.iterator.getValue();
  }
  
  public V setValue(V value) {
    throw new UnsupportedOperationException("setValue() is not supported");
  }
  
  public void remove() {
    throw new UnsupportedOperationException("remove() is not supported");
  }
}

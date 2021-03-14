package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.Unmodifiable;

public final class UnmodifiableMapIterator<K, V> implements MapIterator<K, V>, Unmodifiable {
  private final MapIterator<? extends K, ? extends V> iterator;
  
  public static <K, V> MapIterator<K, V> unmodifiableMapIterator(MapIterator<? extends K, ? extends V> iterator) {
    if (iterator == null)
      throw new IllegalArgumentException("MapIterator must not be null"); 
    if (iterator instanceof Unmodifiable)
      return (MapIterator)iterator; 
    return new UnmodifiableMapIterator<K, V>(iterator);
  }
  
  private UnmodifiableMapIterator(MapIterator<? extends K, ? extends V> iterator) {
    this.iterator = iterator;
  }
  
  public boolean hasNext() {
    return this.iterator.hasNext();
  }
  
  public K next() {
    return (K)this.iterator.next();
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

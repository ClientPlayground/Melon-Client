package org.apache.commons.collections4.bidimap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedBidiMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableOrderedMapIterator;
import org.apache.commons.collections4.map.UnmodifiableEntrySet;
import org.apache.commons.collections4.map.UnmodifiableSortedMap;
import org.apache.commons.collections4.set.UnmodifiableSet;

public final class UnmodifiableSortedBidiMap<K, V> extends AbstractSortedBidiMapDecorator<K, V> implements Unmodifiable {
  private UnmodifiableSortedBidiMap<V, K> inverse;
  
  public static <K, V> SortedBidiMap<K, V> unmodifiableSortedBidiMap(SortedBidiMap<K, ? extends V> map) {
    if (map instanceof Unmodifiable)
      return (SortedBidiMap)map; 
    return new UnmodifiableSortedBidiMap<K, V>(map);
  }
  
  private UnmodifiableSortedBidiMap(SortedBidiMap<K, ? extends V> map) {
    super((SortedBidiMap)map);
  }
  
  public void clear() {
    throw new UnsupportedOperationException();
  }
  
  public V put(K key, V value) {
    throw new UnsupportedOperationException();
  }
  
  public void putAll(Map<? extends K, ? extends V> mapToCopy) {
    throw new UnsupportedOperationException();
  }
  
  public V remove(Object key) {
    throw new UnsupportedOperationException();
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    Set<Map.Entry<K, V>> set = super.entrySet();
    return UnmodifiableEntrySet.unmodifiableEntrySet(set);
  }
  
  public Set<K> keySet() {
    Set<K> set = super.keySet();
    return UnmodifiableSet.unmodifiableSet(set);
  }
  
  public Set<V> values() {
    Set<V> set = super.values();
    return UnmodifiableSet.unmodifiableSet(set);
  }
  
  public K removeValue(Object value) {
    throw new UnsupportedOperationException();
  }
  
  public OrderedMapIterator<K, V> mapIterator() {
    OrderedMapIterator<K, V> it = decorated().mapIterator();
    return UnmodifiableOrderedMapIterator.unmodifiableOrderedMapIterator(it);
  }
  
  public SortedBidiMap<V, K> inverseBidiMap() {
    if (this.inverse == null) {
      this.inverse = new UnmodifiableSortedBidiMap(decorated().inverseBidiMap());
      this.inverse.inverse = this;
    } 
    return this.inverse;
  }
  
  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    SortedMap<K, V> sm = decorated().subMap(fromKey, toKey);
    return UnmodifiableSortedMap.unmodifiableSortedMap(sm);
  }
  
  public SortedMap<K, V> headMap(K toKey) {
    SortedMap<K, V> sm = decorated().headMap(toKey);
    return UnmodifiableSortedMap.unmodifiableSortedMap(sm);
  }
  
  public SortedMap<K, V> tailMap(K fromKey) {
    SortedMap<K, V> sm = decorated().tailMap(fromKey);
    return UnmodifiableSortedMap.unmodifiableSortedMap(sm);
  }
}

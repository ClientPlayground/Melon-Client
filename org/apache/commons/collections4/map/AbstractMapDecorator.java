package org.apache.commons.collections4.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMapDecorator<K, V> extends AbstractIterableMap<K, V> {
  transient Map<K, V> map;
  
  protected AbstractMapDecorator() {}
  
  protected AbstractMapDecorator(Map<K, V> map) {
    if (map == null)
      throw new IllegalArgumentException("Map must not be null"); 
    this.map = map;
  }
  
  protected Map<K, V> decorated() {
    return this.map;
  }
  
  public void clear() {
    decorated().clear();
  }
  
  public boolean containsKey(Object key) {
    return decorated().containsKey(key);
  }
  
  public boolean containsValue(Object value) {
    return decorated().containsValue(value);
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    return decorated().entrySet();
  }
  
  public V get(Object key) {
    return decorated().get(key);
  }
  
  public boolean isEmpty() {
    return decorated().isEmpty();
  }
  
  public Set<K> keySet() {
    return decorated().keySet();
  }
  
  public V put(K key, V value) {
    return decorated().put(key, value);
  }
  
  public void putAll(Map<? extends K, ? extends V> mapToCopy) {
    decorated().putAll(mapToCopy);
  }
  
  public V remove(Object key) {
    return decorated().remove(key);
  }
  
  public int size() {
    return decorated().size();
  }
  
  public Collection<V> values() {
    return decorated().values();
  }
  
  public boolean equals(Object object) {
    if (object == this)
      return true; 
    return decorated().equals(object);
  }
  
  public int hashCode() {
    return decorated().hashCode();
  }
  
  public String toString() {
    return decorated().toString();
  }
}

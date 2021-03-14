package org.apache.commons.collections4.splitmap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.IterableGet;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.EntrySetToMapIteratorAdapter;

public class AbstractIterableGetMapDecorator<K, V> implements IterableGet<K, V> {
  transient Map<K, V> map;
  
  public AbstractIterableGetMapDecorator(Map<K, V> decorated) {
    this.map = decorated;
  }
  
  protected AbstractIterableGetMapDecorator() {}
  
  protected Map<K, V> decorated() {
    return this.map;
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
  
  public V remove(Object key) {
    return decorated().remove(key);
  }
  
  public boolean isEmpty() {
    return decorated().isEmpty();
  }
  
  public Set<K> keySet() {
    return decorated().keySet();
  }
  
  public int size() {
    return decorated().size();
  }
  
  public Collection<V> values() {
    return decorated().values();
  }
  
  public MapIterator<K, V> mapIterator() {
    return (MapIterator<K, V>)new EntrySetToMapIteratorAdapter(entrySet());
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

package org.apache.commons.collections4.map;

import java.util.Map;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.OrderedMapIterator;

public abstract class AbstractOrderedMapDecorator<K, V> extends AbstractMapDecorator<K, V> implements OrderedMap<K, V> {
  protected AbstractOrderedMapDecorator() {}
  
  public AbstractOrderedMapDecorator(OrderedMap<K, V> map) {
    super((Map<K, V>)map);
  }
  
  protected OrderedMap<K, V> decorated() {
    return (OrderedMap<K, V>)super.decorated();
  }
  
  public K firstKey() {
    return (K)decorated().firstKey();
  }
  
  public K lastKey() {
    return (K)decorated().lastKey();
  }
  
  public K nextKey(K key) {
    return (K)decorated().nextKey(key);
  }
  
  public K previousKey(K key) {
    return (K)decorated().previousKey(key);
  }
  
  public OrderedMapIterator<K, V> mapIterator() {
    return decorated().mapIterator();
  }
}

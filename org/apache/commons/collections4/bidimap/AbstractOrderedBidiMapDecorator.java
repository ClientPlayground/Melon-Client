package org.apache.commons.collections4.bidimap;

import java.util.Map;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedBidiMap;
import org.apache.commons.collections4.OrderedMapIterator;

public abstract class AbstractOrderedBidiMapDecorator<K, V> extends AbstractBidiMapDecorator<K, V> implements OrderedBidiMap<K, V> {
  protected AbstractOrderedBidiMapDecorator(OrderedBidiMap<K, V> map) {
    super((BidiMap<K, V>)map);
  }
  
  protected OrderedBidiMap<K, V> decorated() {
    return (OrderedBidiMap<K, V>)super.decorated();
  }
  
  public OrderedMapIterator<K, V> mapIterator() {
    return decorated().mapIterator();
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
  
  public OrderedBidiMap<V, K> inverseBidiMap() {
    return decorated().inverseBidiMap();
  }
}

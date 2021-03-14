package org.apache.commons.collections4;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface Get<K, V> {
  boolean containsKey(Object paramObject);
  
  boolean containsValue(Object paramObject);
  
  Set<Map.Entry<K, V>> entrySet();
  
  V get(Object paramObject);
  
  V remove(Object paramObject);
  
  boolean isEmpty();
  
  Set<K> keySet();
  
  int size();
  
  Collection<V> values();
}

package org.apache.commons.collections4;

import java.util.Map;

public interface Put<K, V> {
  void clear();
  
  Object put(K paramK, V paramV);
  
  void putAll(Map<? extends K, ? extends V> paramMap);
}

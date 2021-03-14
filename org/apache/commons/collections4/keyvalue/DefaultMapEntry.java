package org.apache.commons.collections4.keyvalue;

import java.util.Map;
import org.apache.commons.collections4.KeyValue;

public final class DefaultMapEntry<K, V> extends AbstractMapEntry<K, V> {
  public DefaultMapEntry(K key, V value) {
    super(key, value);
  }
  
  public DefaultMapEntry(KeyValue<? extends K, ? extends V> pair) {
    super((K)pair.getKey(), (V)pair.getValue());
  }
  
  public DefaultMapEntry(Map.Entry<? extends K, ? extends V> entry) {
    super(entry.getKey(), entry.getValue());
  }
}

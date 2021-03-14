package org.apache.commons.collections4.keyvalue;

import java.util.Map;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.Unmodifiable;

public final class UnmodifiableMapEntry<K, V> extends AbstractMapEntry<K, V> implements Unmodifiable {
  public UnmodifiableMapEntry(K key, V value) {
    super(key, value);
  }
  
  public UnmodifiableMapEntry(KeyValue<? extends K, ? extends V> pair) {
    super((K)pair.getKey(), (V)pair.getValue());
  }
  
  public UnmodifiableMapEntry(Map.Entry<? extends K, ? extends V> entry) {
    super(entry.getKey(), entry.getValue());
  }
  
  public V setValue(V value) {
    throw new UnsupportedOperationException("setValue() is not supported");
  }
}

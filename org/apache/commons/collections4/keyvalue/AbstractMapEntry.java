package org.apache.commons.collections4.keyvalue;

import java.util.Map;

public abstract class AbstractMapEntry<K, V> extends AbstractKeyValue<K, V> implements Map.Entry<K, V> {
  protected AbstractMapEntry(K key, V value) {
    super(key, value);
  }
  
  public V setValue(V value) {
    return super.setValue(value);
  }
  
  public boolean equals(Object obj) {
    if (obj == this)
      return true; 
    if (!(obj instanceof Map.Entry))
      return false; 
    Map.Entry<?, ?> other = (Map.Entry<?, ?>)obj;
    return (((getKey() == null) ? (other.getKey() == null) : getKey().equals(other.getKey())) && ((getValue() == null) ? (other.getValue() == null) : getValue().equals(other.getValue())));
  }
  
  public int hashCode() {
    return ((getKey() == null) ? 0 : getKey().hashCode()) ^ ((getValue() == null) ? 0 : getValue().hashCode());
  }
}

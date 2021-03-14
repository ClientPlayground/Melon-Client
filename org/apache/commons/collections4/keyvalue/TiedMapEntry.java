package org.apache.commons.collections4.keyvalue;

import java.io.Serializable;
import java.util.Map;
import org.apache.commons.collections4.KeyValue;

public class TiedMapEntry<K, V> implements Map.Entry<K, V>, KeyValue<K, V>, Serializable {
  private static final long serialVersionUID = -8453869361373831205L;
  
  private final Map<K, V> map;
  
  private final K key;
  
  public TiedMapEntry(Map<K, V> map, K key) {
    this.map = map;
    this.key = key;
  }
  
  public K getKey() {
    return this.key;
  }
  
  public V getValue() {
    return this.map.get(this.key);
  }
  
  public V setValue(V value) {
    if (value == this)
      throw new IllegalArgumentException("Cannot set value to this map entry"); 
    return this.map.put(this.key, value);
  }
  
  public boolean equals(Object obj) {
    if (obj == this)
      return true; 
    if (!(obj instanceof Map.Entry))
      return false; 
    Map.Entry<?, ?> other = (Map.Entry<?, ?>)obj;
    Object value = getValue();
    return (((this.key == null) ? (other.getKey() == null) : this.key.equals(other.getKey())) && ((value == null) ? (other.getValue() == null) : value.equals(other.getValue())));
  }
  
  public int hashCode() {
    Object value = getValue();
    return ((getKey() == null) ? 0 : getKey().hashCode()) ^ ((value == null) ? 0 : value.hashCode());
  }
  
  public String toString() {
    return (new StringBuilder()).append(getKey()).append("=").append(getValue()).toString();
  }
}

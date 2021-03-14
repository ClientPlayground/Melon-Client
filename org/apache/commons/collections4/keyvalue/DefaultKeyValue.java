package org.apache.commons.collections4.keyvalue;

import java.util.Map;
import org.apache.commons.collections4.KeyValue;

public class DefaultKeyValue<K, V> extends AbstractKeyValue<K, V> {
  public DefaultKeyValue() {
    super(null, null);
  }
  
  public DefaultKeyValue(K key, V value) {
    super(key, value);
  }
  
  public DefaultKeyValue(KeyValue<? extends K, ? extends V> pair) {
    super((K)pair.getKey(), (V)pair.getValue());
  }
  
  public DefaultKeyValue(Map.Entry<? extends K, ? extends V> entry) {
    super(entry.getKey(), entry.getValue());
  }
  
  public K setKey(K key) {
    if (key == this)
      throw new IllegalArgumentException("DefaultKeyValue may not contain itself as a key."); 
    return super.setKey(key);
  }
  
  public V setValue(V value) {
    if (value == this)
      throw new IllegalArgumentException("DefaultKeyValue may not contain itself as a value."); 
    return super.setValue(value);
  }
  
  public Map.Entry<K, V> toMapEntry() {
    return new DefaultMapEntry<K, V>(this);
  }
  
  public boolean equals(Object obj) {
    if (obj == this)
      return true; 
    if (!(obj instanceof DefaultKeyValue))
      return false; 
    DefaultKeyValue<?, ?> other = (DefaultKeyValue<?, ?>)obj;
    return (((getKey() == null) ? (other.getKey() == null) : getKey().equals(other.getKey())) && ((getValue() == null) ? (other.getValue() == null) : getValue().equals(other.getValue())));
  }
  
  public int hashCode() {
    return ((getKey() == null) ? 0 : getKey().hashCode()) ^ ((getValue() == null) ? 0 : getValue().hashCode());
  }
}

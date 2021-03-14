package org.apache.commons.collections4.keyvalue;

import org.apache.commons.collections4.KeyValue;

public abstract class AbstractKeyValue<K, V> implements KeyValue<K, V> {
  private K key;
  
  private V value;
  
  protected AbstractKeyValue(K key, V value) {
    this.key = key;
    this.value = value;
  }
  
  public K getKey() {
    return this.key;
  }
  
  protected K setKey(K key) {
    K old = this.key;
    this.key = key;
    return old;
  }
  
  public V getValue() {
    return this.value;
  }
  
  protected V setValue(V value) {
    V old = this.value;
    this.value = value;
    return old;
  }
  
  public String toString() {
    return (new StringBuilder()).append(getKey()).append('=').append(getValue()).toString();
  }
}

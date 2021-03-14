package org.apache.commons.collections4;

public interface BoundedMap<K, V> extends IterableMap<K, V> {
  boolean isFull();
  
  int maxSize();
}

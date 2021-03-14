package org.apache.commons.collections4;

public interface OrderedMapIterator<K, V> extends MapIterator<K, V>, OrderedIterator<K> {
  boolean hasPrevious();
  
  K previous();
}

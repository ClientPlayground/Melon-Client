package org.apache.commons.collections4;

public interface IterableGet<K, V> extends Get<K, V> {
  MapIterator<K, V> mapIterator();
}

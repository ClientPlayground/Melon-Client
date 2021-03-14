package org.apache.commons.collections4;

public interface OrderedBidiMap<K, V> extends BidiMap<K, V>, OrderedMap<K, V> {
  OrderedBidiMap<V, K> inverseBidiMap();
}

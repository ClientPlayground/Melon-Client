package org.apache.commons.collections4;

import java.util.Comparator;
import java.util.SortedMap;

public interface SortedBidiMap<K, V> extends OrderedBidiMap<K, V>, SortedMap<K, V> {
  SortedBidiMap<V, K> inverseBidiMap();
  
  Comparator<? super V> valueComparator();
}

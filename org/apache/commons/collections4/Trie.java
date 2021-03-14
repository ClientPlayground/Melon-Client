package org.apache.commons.collections4;

import java.util.SortedMap;

public interface Trie<K, V> extends IterableSortedMap<K, V> {
  SortedMap<K, V> prefixMap(K paramK);
}

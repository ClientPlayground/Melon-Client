package org.apache.commons.collections4;

import org.apache.commons.collections4.trie.UnmodifiableTrie;

public class TrieUtils {
  public static <K, V> Trie<K, V> unmodifiableTrie(Trie<K, ? extends V> trie) {
    return UnmodifiableTrie.unmodifiableTrie(trie);
  }
}

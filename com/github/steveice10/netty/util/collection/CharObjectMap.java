package com.github.steveice10.netty.util.collection;

import java.util.Map;

public interface CharObjectMap<V> extends Map<Character, V> {
  V get(char paramChar);
  
  V put(char paramChar, V paramV);
  
  V remove(char paramChar);
  
  Iterable<PrimitiveEntry<V>> entries();
  
  boolean containsKey(char paramChar);
  
  public static interface PrimitiveEntry<V> {
    char key();
    
    V value();
    
    void setValue(V param1V);
  }
}

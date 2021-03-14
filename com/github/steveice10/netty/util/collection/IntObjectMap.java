package com.github.steveice10.netty.util.collection;

import java.util.Map;

public interface IntObjectMap<V> extends Map<Integer, V> {
  V get(int paramInt);
  
  V put(int paramInt, V paramV);
  
  V remove(int paramInt);
  
  Iterable<PrimitiveEntry<V>> entries();
  
  boolean containsKey(int paramInt);
  
  public static interface PrimitiveEntry<V> {
    int key();
    
    V value();
    
    void setValue(V param1V);
  }
}

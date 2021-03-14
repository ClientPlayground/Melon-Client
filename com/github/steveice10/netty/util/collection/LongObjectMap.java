package com.github.steveice10.netty.util.collection;

import java.util.Map;

public interface LongObjectMap<V> extends Map<Long, V> {
  V get(long paramLong);
  
  V put(long paramLong, V paramV);
  
  V remove(long paramLong);
  
  Iterable<PrimitiveEntry<V>> entries();
  
  boolean containsKey(long paramLong);
  
  public static interface PrimitiveEntry<V> {
    long key();
    
    V value();
    
    void setValue(V param1V);
  }
}

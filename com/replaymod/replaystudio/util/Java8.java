package com.replaymod.replaystudio.util;

import com.google.common.base.Supplier;
import java.util.Map;

public class Java8 {
  public static class Map8 {
    public static <K, V> V getOrCreate(Map<K, V> map, K key, Supplier<V> supplier) {
      V value = map.get(key);
      if (value == null) {
        value = (V)supplier.get();
        map.put(key, value);
      } 
      return value;
    }
    
    public static <K, V> void putIfAbsent(Map<K, V> map, K key, V value) {
      if (!map.containsKey(key))
        map.put(key, value); 
    }
  }
}

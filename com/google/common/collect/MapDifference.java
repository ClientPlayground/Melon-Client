package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Map;
import javax.annotation.Nullable;

@GwtCompatible
public interface MapDifference<K, V> {
  boolean areEqual();
  
  Map<K, V> entriesOnlyOnLeft();
  
  Map<K, V> entriesOnlyOnRight();
  
  Map<K, V> entriesInCommon();
  
  Map<K, ValueDifference<V>> entriesDiffering();
  
  boolean equals(@Nullable Object paramObject);
  
  int hashCode();
  
  public static interface ValueDifference<V> {
    V leftValue();
    
    V rightValue();
    
    boolean equals(@Nullable Object param1Object);
    
    int hashCode();
  }
}

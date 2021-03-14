package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

@GwtCompatible
public interface SetMultimap<K, V> extends Multimap<K, V> {
  Set<V> get(@Nullable K paramK);
  
  Set<V> removeAll(@Nullable Object paramObject);
  
  Set<V> replaceValues(K paramK, Iterable<? extends V> paramIterable);
  
  Set<Map.Entry<K, V>> entries();
  
  Map<K, Collection<V>> asMap();
  
  boolean equals(@Nullable Object paramObject);
}

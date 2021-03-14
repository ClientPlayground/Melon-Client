package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@GwtCompatible
public interface ListMultimap<K, V> extends Multimap<K, V> {
  List<V> get(@Nullable K paramK);
  
  List<V> removeAll(@Nullable Object paramObject);
  
  List<V> replaceValues(K paramK, Iterable<? extends V> paramIterable);
  
  Map<K, Collection<V>> asMap();
  
  boolean equals(@Nullable Object paramObject);
}

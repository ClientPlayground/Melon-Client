package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;

@GwtCompatible
public abstract class ForwardingSortedSetMultimap<K, V> extends ForwardingSetMultimap<K, V> implements SortedSetMultimap<K, V> {
  public SortedSet<V> get(@Nullable K key) {
    return delegate().get(key);
  }
  
  public SortedSet<V> removeAll(@Nullable Object key) {
    return delegate().removeAll(key);
  }
  
  public SortedSet<V> replaceValues(K key, Iterable<? extends V> values) {
    return delegate().replaceValues(key, values);
  }
  
  public Comparator<? super V> valueComparator() {
    return delegate().valueComparator();
  }
  
  protected abstract SortedSetMultimap<K, V> delegate();
}

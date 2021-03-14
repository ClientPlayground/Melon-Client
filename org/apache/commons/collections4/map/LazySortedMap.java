package org.apache.commons.collections4.map;

import java.util.Comparator;
import java.util.SortedMap;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.Transformer;

public class LazySortedMap<K, V> extends LazyMap<K, V> implements SortedMap<K, V> {
  private static final long serialVersionUID = 2715322183617658933L;
  
  public static <K, V> LazySortedMap<K, V> lazySortedMap(SortedMap<K, V> map, Factory<? extends V> factory) {
    return new LazySortedMap<K, V>(map, factory);
  }
  
  public static <K, V> LazySortedMap<K, V> lazySortedMap(SortedMap<K, V> map, Transformer<? super K, ? extends V> factory) {
    return new LazySortedMap<K, V>(map, factory);
  }
  
  protected LazySortedMap(SortedMap<K, V> map, Factory<? extends V> factory) {
    super(map, factory);
  }
  
  protected LazySortedMap(SortedMap<K, V> map, Transformer<? super K, ? extends V> factory) {
    super(map, factory);
  }
  
  protected SortedMap<K, V> getSortedMap() {
    return (SortedMap<K, V>)this.map;
  }
  
  public K firstKey() {
    return getSortedMap().firstKey();
  }
  
  public K lastKey() {
    return getSortedMap().lastKey();
  }
  
  public Comparator<? super K> comparator() {
    return getSortedMap().comparator();
  }
  
  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    SortedMap<K, V> map = getSortedMap().subMap(fromKey, toKey);
    return new LazySortedMap(map, this.factory);
  }
  
  public SortedMap<K, V> headMap(K toKey) {
    SortedMap<K, V> map = getSortedMap().headMap(toKey);
    return new LazySortedMap(map, this.factory);
  }
  
  public SortedMap<K, V> tailMap(K fromKey) {
    SortedMap<K, V> map = getSortedMap().tailMap(fromKey);
    return new LazySortedMap(map, this.factory);
  }
}

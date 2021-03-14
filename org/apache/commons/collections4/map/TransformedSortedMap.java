package org.apache.commons.collections4.map;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import org.apache.commons.collections4.Transformer;

public class TransformedSortedMap<K, V> extends TransformedMap<K, V> implements SortedMap<K, V> {
  private static final long serialVersionUID = -8751771676410385778L;
  
  public static <K, V> TransformedSortedMap<K, V> transformingSortedMap(SortedMap<K, V> map, Transformer<? super K, ? extends K> keyTransformer, Transformer<? super V, ? extends V> valueTransformer) {
    return new TransformedSortedMap<K, V>(map, keyTransformer, valueTransformer);
  }
  
  public static <K, V> TransformedSortedMap<K, V> transformedSortedMap(SortedMap<K, V> map, Transformer<? super K, ? extends K> keyTransformer, Transformer<? super V, ? extends V> valueTransformer) {
    TransformedSortedMap<K, V> decorated = new TransformedSortedMap<K, V>(map, keyTransformer, valueTransformer);
    if (map.size() > 0) {
      Map<K, V> transformed = decorated.transformMap(map);
      decorated.clear();
      decorated.decorated().putAll(transformed);
    } 
    return decorated;
  }
  
  protected TransformedSortedMap(SortedMap<K, V> map, Transformer<? super K, ? extends K> keyTransformer, Transformer<? super V, ? extends V> valueTransformer) {
    super(map, keyTransformer, valueTransformer);
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
    return new TransformedSortedMap(map, this.keyTransformer, this.valueTransformer);
  }
  
  public SortedMap<K, V> headMap(K toKey) {
    SortedMap<K, V> map = getSortedMap().headMap(toKey);
    return new TransformedSortedMap(map, this.keyTransformer, this.valueTransformer);
  }
  
  public SortedMap<K, V> tailMap(K fromKey) {
    SortedMap<K, V> map = getSortedMap().tailMap(fromKey);
    return new TransformedSortedMap(map, this.keyTransformer, this.valueTransformer);
  }
}

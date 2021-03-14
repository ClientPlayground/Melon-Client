package org.apache.commons.collections4;

public interface OrderedMap<K, V> extends IterableMap<K, V> {
  OrderedMapIterator<K, V> mapIterator();
  
  K firstKey();
  
  K lastKey();
  
  K nextKey(K paramK);
  
  K previousKey(K paramK);
}

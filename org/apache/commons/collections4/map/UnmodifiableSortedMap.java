package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.collection.UnmodifiableCollection;
import org.apache.commons.collections4.set.UnmodifiableSet;

public final class UnmodifiableSortedMap<K, V> extends AbstractSortedMapDecorator<K, V> implements Unmodifiable, Serializable {
  private static final long serialVersionUID = 5805344239827376360L;
  
  public static <K, V> SortedMap<K, V> unmodifiableSortedMap(SortedMap<K, ? extends V> map) {
    if (map instanceof Unmodifiable)
      return (SortedMap)map; 
    return (SortedMap<K, V>)new UnmodifiableSortedMap<K, V>(map);
  }
  
  private UnmodifiableSortedMap(SortedMap<K, ? extends V> map) {
    super((SortedMap)map);
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(this.map);
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    this.map = (Map<K, V>)in.readObject();
  }
  
  public void clear() {
    throw new UnsupportedOperationException();
  }
  
  public V put(K key, V value) {
    throw new UnsupportedOperationException();
  }
  
  public void putAll(Map<? extends K, ? extends V> mapToCopy) {
    throw new UnsupportedOperationException();
  }
  
  public V remove(Object key) {
    throw new UnsupportedOperationException();
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    return UnmodifiableEntrySet.unmodifiableEntrySet(super.entrySet());
  }
  
  public Set<K> keySet() {
    return UnmodifiableSet.unmodifiableSet(super.keySet());
  }
  
  public Collection<V> values() {
    return UnmodifiableCollection.unmodifiableCollection(super.values());
  }
  
  public K firstKey() {
    return decorated().firstKey();
  }
  
  public K lastKey() {
    return decorated().lastKey();
  }
  
  public Comparator<? super K> comparator() {
    return decorated().comparator();
  }
  
  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    return (SortedMap<K, V>)new UnmodifiableSortedMap(decorated().subMap(fromKey, toKey));
  }
  
  public SortedMap<K, V> headMap(K toKey) {
    return (SortedMap<K, V>)new UnmodifiableSortedMap(decorated().headMap(toKey));
  }
  
  public SortedMap<K, V> tailMap(K fromKey) {
    return (SortedMap<K, V>)new UnmodifiableSortedMap(decorated().tailMap(fromKey));
  }
}

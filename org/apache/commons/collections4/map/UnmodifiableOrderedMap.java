package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.collection.UnmodifiableCollection;
import org.apache.commons.collections4.iterators.UnmodifiableOrderedMapIterator;
import org.apache.commons.collections4.set.UnmodifiableSet;

public final class UnmodifiableOrderedMap<K, V> extends AbstractOrderedMapDecorator<K, V> implements Unmodifiable, Serializable {
  private static final long serialVersionUID = 8136428161720526266L;
  
  public static <K, V> OrderedMap<K, V> unmodifiableOrderedMap(OrderedMap<? extends K, ? extends V> map) {
    if (map instanceof Unmodifiable)
      return (OrderedMap)map; 
    return new UnmodifiableOrderedMap<K, V>(map);
  }
  
  private UnmodifiableOrderedMap(OrderedMap<? extends K, ? extends V> map) {
    super((OrderedMap)map);
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(this.map);
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    this.map = (Map<K, V>)in.readObject();
  }
  
  public OrderedMapIterator<K, V> mapIterator() {
    OrderedMapIterator<K, V> it = decorated().mapIterator();
    return UnmodifiableOrderedMapIterator.unmodifiableOrderedMapIterator(it);
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
    Set<Map.Entry<K, V>> set = super.entrySet();
    return UnmodifiableEntrySet.unmodifiableEntrySet(set);
  }
  
  public Set<K> keySet() {
    Set<K> set = super.keySet();
    return UnmodifiableSet.unmodifiableSet(set);
  }
  
  public Collection<V> values() {
    Collection<V> coll = super.values();
    return UnmodifiableCollection.unmodifiableCollection(coll);
  }
}

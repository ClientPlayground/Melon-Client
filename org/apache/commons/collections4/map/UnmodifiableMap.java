package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.collection.UnmodifiableCollection;
import org.apache.commons.collections4.iterators.EntrySetMapIterator;
import org.apache.commons.collections4.iterators.UnmodifiableMapIterator;
import org.apache.commons.collections4.set.UnmodifiableSet;

public final class UnmodifiableMap<K, V> extends AbstractMapDecorator<K, V> implements Unmodifiable, Serializable {
  private static final long serialVersionUID = 2737023427269031941L;
  
  public static <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V> map) {
    if (map instanceof Unmodifiable)
      return (Map)map; 
    return (Map<K, V>)new UnmodifiableMap<K, V>(map);
  }
  
  private UnmodifiableMap(Map<? extends K, ? extends V> map) {
    super((Map)map);
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
  
  public MapIterator<K, V> mapIterator() {
    if (this.map instanceof IterableMap) {
      MapIterator<K, V> it = ((IterableMap)this.map).mapIterator();
      return UnmodifiableMapIterator.unmodifiableMapIterator(it);
    } 
    EntrySetMapIterator entrySetMapIterator = new EntrySetMapIterator(this.map);
    return UnmodifiableMapIterator.unmodifiableMapIterator((MapIterator)entrySetMapIterator);
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

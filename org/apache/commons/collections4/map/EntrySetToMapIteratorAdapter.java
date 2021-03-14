package org.apache.commons.collections4.map;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.ResettableIterator;

public class EntrySetToMapIteratorAdapter<K, V> implements MapIterator<K, V>, ResettableIterator<K> {
  Set<Map.Entry<K, V>> entrySet;
  
  transient Iterator<Map.Entry<K, V>> iterator;
  
  transient Map.Entry<K, V> entry;
  
  public EntrySetToMapIteratorAdapter(Set<Map.Entry<K, V>> entrySet) {
    this.entrySet = entrySet;
    reset();
  }
  
  public K getKey() {
    return current().getKey();
  }
  
  public V getValue() {
    return current().getValue();
  }
  
  public V setValue(V value) {
    return current().setValue(value);
  }
  
  public boolean hasNext() {
    return this.iterator.hasNext();
  }
  
  public K next() {
    this.entry = this.iterator.next();
    return getKey();
  }
  
  public synchronized void reset() {
    this.iterator = this.entrySet.iterator();
  }
  
  public void remove() {
    this.iterator.remove();
    this.entry = null;
  }
  
  protected synchronized Map.Entry<K, V> current() {
    if (this.entry == null)
      throw new IllegalStateException(); 
    return this.entry;
  }
}

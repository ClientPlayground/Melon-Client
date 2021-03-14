package org.apache.commons.collections4.map;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.collection.CompositeCollection;
import org.apache.commons.collections4.set.CompositeSet;

public class CompositeMap<K, V> extends AbstractIterableMap<K, V> implements Serializable {
  private static final long serialVersionUID = -6096931280583808322L;
  
  private Map<K, V>[] composite;
  
  private MapMutator<K, V> mutator;
  
  public CompositeMap() {
    this((Map<K, V>[])new Map[0], (MapMutator<K, V>)null);
  }
  
  public CompositeMap(Map<K, V> one, Map<K, V> two) {
    this((Map<K, V>[])new Map[] { one, two }, (MapMutator<K, V>)null);
  }
  
  public CompositeMap(Map<K, V> one, Map<K, V> two, MapMutator<K, V> mutator) {
    this((Map<K, V>[])new Map[] { one, two }, mutator);
  }
  
  public CompositeMap(Map<K, V>... composite) {
    this(composite, (MapMutator<K, V>)null);
  }
  
  public CompositeMap(Map<K, V>[] composite, MapMutator<K, V> mutator) {
    this.mutator = mutator;
    this.composite = (Map<K, V>[])new Map[0];
    for (int i = composite.length - 1; i >= 0; i--)
      addComposited(composite[i]); 
  }
  
  public void setMutator(MapMutator<K, V> mutator) {
    this.mutator = mutator;
  }
  
  public synchronized void addComposited(Map<K, V> map) throws IllegalArgumentException {
    for (int i = this.composite.length - 1; i >= 0; i--) {
      Collection<K> intersect = CollectionUtils.intersection(this.composite[i].keySet(), map.keySet());
      if (intersect.size() != 0) {
        if (this.mutator == null)
          throw new IllegalArgumentException("Key collision adding Map to CompositeMap"); 
        this.mutator.resolveCollision(this, this.composite[i], map, intersect);
      } 
    } 
    Map[] arrayOfMap = new Map[this.composite.length + 1];
    System.arraycopy(this.composite, 0, arrayOfMap, 0, this.composite.length);
    arrayOfMap[arrayOfMap.length - 1] = map;
    this.composite = (Map<K, V>[])arrayOfMap;
  }
  
  public synchronized Map<K, V> removeComposited(Map<K, V> map) {
    int size = this.composite.length;
    for (int i = 0; i < size; i++) {
      if (this.composite[i].equals(map)) {
        Map[] arrayOfMap = new Map[size - 1];
        System.arraycopy(this.composite, 0, arrayOfMap, 0, i);
        System.arraycopy(this.composite, i + 1, arrayOfMap, i, size - i - 1);
        this.composite = (Map<K, V>[])arrayOfMap;
        return map;
      } 
    } 
    return null;
  }
  
  public void clear() {
    for (int i = this.composite.length - 1; i >= 0; i--)
      this.composite[i].clear(); 
  }
  
  public boolean containsKey(Object key) {
    for (int i = this.composite.length - 1; i >= 0; i--) {
      if (this.composite[i].containsKey(key))
        return true; 
    } 
    return false;
  }
  
  public boolean containsValue(Object value) {
    for (int i = this.composite.length - 1; i >= 0; i--) {
      if (this.composite[i].containsValue(value))
        return true; 
    } 
    return false;
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    CompositeSet<Map.Entry<K, V>> entries = new CompositeSet();
    for (int i = this.composite.length - 1; i >= 0; i--)
      entries.addComposited(this.composite[i].entrySet()); 
    return (Set<Map.Entry<K, V>>)entries;
  }
  
  public V get(Object key) {
    for (int i = this.composite.length - 1; i >= 0; i--) {
      if (this.composite[i].containsKey(key))
        return this.composite[i].get(key); 
    } 
    return null;
  }
  
  public boolean isEmpty() {
    for (int i = this.composite.length - 1; i >= 0; i--) {
      if (!this.composite[i].isEmpty())
        return false; 
    } 
    return true;
  }
  
  public Set<K> keySet() {
    CompositeSet<K> keys = new CompositeSet();
    for (int i = this.composite.length - 1; i >= 0; i--)
      keys.addComposited(this.composite[i].keySet()); 
    return (Set<K>)keys;
  }
  
  public V put(K key, V value) {
    if (this.mutator == null)
      throw new UnsupportedOperationException("No mutator specified"); 
    return this.mutator.put(this, this.composite, key, value);
  }
  
  public void putAll(Map<? extends K, ? extends V> map) {
    if (this.mutator == null)
      throw new UnsupportedOperationException("No mutator specified"); 
    this.mutator.putAll(this, this.composite, map);
  }
  
  public V remove(Object key) {
    for (int i = this.composite.length - 1; i >= 0; i--) {
      if (this.composite[i].containsKey(key))
        return this.composite[i].remove(key); 
    } 
    return null;
  }
  
  public int size() {
    int size = 0;
    for (int i = this.composite.length - 1; i >= 0; i--)
      size += this.composite[i].size(); 
    return size;
  }
  
  public Collection<V> values() {
    CompositeCollection<V> values = new CompositeCollection();
    for (int i = this.composite.length - 1; i >= 0; i--)
      values.addComposited(this.composite[i].values()); 
    return (Collection<V>)values;
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof Map) {
      Map<?, ?> map = (Map<?, ?>)obj;
      return entrySet().equals(map.entrySet());
    } 
    return false;
  }
  
  public int hashCode() {
    int code = 0;
    for (Map.Entry<K, V> entry : entrySet())
      code += entry.hashCode(); 
    return code;
  }
  
  public static interface MapMutator<K, V> extends Serializable {
    void resolveCollision(CompositeMap<K, V> param1CompositeMap, Map<K, V> param1Map1, Map<K, V> param1Map2, Collection<K> param1Collection);
    
    V put(CompositeMap<K, V> param1CompositeMap, Map<K, V>[] param1ArrayOfMap, K param1K, V param1V);
    
    void putAll(CompositeMap<K, V> param1CompositeMap, Map<K, V>[] param1ArrayOfMap, Map<? extends K, ? extends V> param1Map);
  }
}

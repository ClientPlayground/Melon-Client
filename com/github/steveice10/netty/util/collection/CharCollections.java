package com.github.steveice10.netty.util.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public final class CharCollections {
  private static final CharObjectMap<Object> EMPTY_MAP = new EmptyMap();
  
  public static <V> CharObjectMap<V> emptyMap() {
    return (CharObjectMap)EMPTY_MAP;
  }
  
  public static <V> CharObjectMap<V> unmodifiableMap(CharObjectMap<V> map) {
    return new UnmodifiableMap<V>(map);
  }
  
  private static final class EmptyMap implements CharObjectMap<Object> {
    private EmptyMap() {}
    
    public Object get(char key) {
      return null;
    }
    
    public Object put(char key, Object value) {
      throw new UnsupportedOperationException("put");
    }
    
    public Object remove(char key) {
      return null;
    }
    
    public int size() {
      return 0;
    }
    
    public boolean isEmpty() {
      return true;
    }
    
    public boolean containsKey(Object key) {
      return false;
    }
    
    public void clear() {}
    
    public Set<Character> keySet() {
      return Collections.emptySet();
    }
    
    public boolean containsKey(char key) {
      return false;
    }
    
    public boolean containsValue(Object value) {
      return false;
    }
    
    public Iterable<CharObjectMap.PrimitiveEntry<Object>> entries() {
      return Collections.emptySet();
    }
    
    public Object get(Object key) {
      return null;
    }
    
    public Object put(Character key, Object value) {
      throw new UnsupportedOperationException();
    }
    
    public Object remove(Object key) {
      return null;
    }
    
    public void putAll(Map<? extends Character, ?> m) {
      throw new UnsupportedOperationException();
    }
    
    public Collection<Object> values() {
      return Collections.emptyList();
    }
    
    public Set<Map.Entry<Character, Object>> entrySet() {
      return Collections.emptySet();
    }
  }
  
  private static final class UnmodifiableMap<V> implements CharObjectMap<V> {
    private final CharObjectMap<V> map;
    
    private Set<Character> keySet;
    
    private Set<Map.Entry<Character, V>> entrySet;
    
    private Collection<V> values;
    
    private Iterable<CharObjectMap.PrimitiveEntry<V>> entries;
    
    UnmodifiableMap(CharObjectMap<V> map) {
      this.map = map;
    }
    
    public V get(char key) {
      return this.map.get(key);
    }
    
    public V put(char key, V value) {
      throw new UnsupportedOperationException("put");
    }
    
    public V remove(char key) {
      throw new UnsupportedOperationException("remove");
    }
    
    public int size() {
      return this.map.size();
    }
    
    public boolean isEmpty() {
      return this.map.isEmpty();
    }
    
    public void clear() {
      throw new UnsupportedOperationException("clear");
    }
    
    public boolean containsKey(char key) {
      return this.map.containsKey(key);
    }
    
    public boolean containsValue(Object value) {
      return this.map.containsValue(value);
    }
    
    public boolean containsKey(Object key) {
      return this.map.containsKey(key);
    }
    
    public V get(Object key) {
      return this.map.get(key);
    }
    
    public V put(Character key, V value) {
      throw new UnsupportedOperationException("put");
    }
    
    public V remove(Object key) {
      throw new UnsupportedOperationException("remove");
    }
    
    public void putAll(Map<? extends Character, ? extends V> m) {
      throw new UnsupportedOperationException("putAll");
    }
    
    public Iterable<CharObjectMap.PrimitiveEntry<V>> entries() {
      if (this.entries == null)
        this.entries = new Iterable<CharObjectMap.PrimitiveEntry<V>>() {
            public Iterator<CharObjectMap.PrimitiveEntry<V>> iterator() {
              return new CharCollections.UnmodifiableMap.IteratorImpl(CharCollections.UnmodifiableMap.this.map.entries().iterator());
            }
          }; 
      return this.entries;
    }
    
    public Set<Character> keySet() {
      if (this.keySet == null)
        this.keySet = Collections.unmodifiableSet(this.map.keySet()); 
      return this.keySet;
    }
    
    public Set<Map.Entry<Character, V>> entrySet() {
      if (this.entrySet == null)
        this.entrySet = Collections.unmodifiableSet(this.map.entrySet()); 
      return this.entrySet;
    }
    
    public Collection<V> values() {
      if (this.values == null)
        this.values = Collections.unmodifiableCollection(this.map.values()); 
      return this.values;
    }
    
    private class IteratorImpl implements Iterator<CharObjectMap.PrimitiveEntry<V>> {
      final Iterator<CharObjectMap.PrimitiveEntry<V>> iter;
      
      IteratorImpl(Iterator<CharObjectMap.PrimitiveEntry<V>> iter) {
        this.iter = iter;
      }
      
      public boolean hasNext() {
        return this.iter.hasNext();
      }
      
      public CharObjectMap.PrimitiveEntry<V> next() {
        if (!hasNext())
          throw new NoSuchElementException(); 
        return new CharCollections.UnmodifiableMap.EntryImpl(this.iter.next());
      }
      
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    }
    
    private class EntryImpl implements CharObjectMap.PrimitiveEntry<V> {
      private final CharObjectMap.PrimitiveEntry<V> entry;
      
      EntryImpl(CharObjectMap.PrimitiveEntry<V> entry) {
        this.entry = entry;
      }
      
      public char key() {
        return this.entry.key();
      }
      
      public V value() {
        return this.entry.value();
      }
      
      public void setValue(V value) {
        throw new UnsupportedOperationException("setValue");
      }
    }
  }
}

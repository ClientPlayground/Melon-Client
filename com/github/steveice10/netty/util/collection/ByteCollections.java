package com.github.steveice10.netty.util.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public final class ByteCollections {
  private static final ByteObjectMap<Object> EMPTY_MAP = new EmptyMap();
  
  public static <V> ByteObjectMap<V> emptyMap() {
    return (ByteObjectMap)EMPTY_MAP;
  }
  
  public static <V> ByteObjectMap<V> unmodifiableMap(ByteObjectMap<V> map) {
    return new UnmodifiableMap<V>(map);
  }
  
  private static final class EmptyMap implements ByteObjectMap<Object> {
    private EmptyMap() {}
    
    public Object get(byte key) {
      return null;
    }
    
    public Object put(byte key, Object value) {
      throw new UnsupportedOperationException("put");
    }
    
    public Object remove(byte key) {
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
    
    public Set<Byte> keySet() {
      return Collections.emptySet();
    }
    
    public boolean containsKey(byte key) {
      return false;
    }
    
    public boolean containsValue(Object value) {
      return false;
    }
    
    public Iterable<ByteObjectMap.PrimitiveEntry<Object>> entries() {
      return Collections.emptySet();
    }
    
    public Object get(Object key) {
      return null;
    }
    
    public Object put(Byte key, Object value) {
      throw new UnsupportedOperationException();
    }
    
    public Object remove(Object key) {
      return null;
    }
    
    public void putAll(Map<? extends Byte, ?> m) {
      throw new UnsupportedOperationException();
    }
    
    public Collection<Object> values() {
      return Collections.emptyList();
    }
    
    public Set<Map.Entry<Byte, Object>> entrySet() {
      return Collections.emptySet();
    }
  }
  
  private static final class UnmodifiableMap<V> implements ByteObjectMap<V> {
    private final ByteObjectMap<V> map;
    
    private Set<Byte> keySet;
    
    private Set<Map.Entry<Byte, V>> entrySet;
    
    private Collection<V> values;
    
    private Iterable<ByteObjectMap.PrimitiveEntry<V>> entries;
    
    UnmodifiableMap(ByteObjectMap<V> map) {
      this.map = map;
    }
    
    public V get(byte key) {
      return this.map.get(key);
    }
    
    public V put(byte key, V value) {
      throw new UnsupportedOperationException("put");
    }
    
    public V remove(byte key) {
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
    
    public boolean containsKey(byte key) {
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
    
    public V put(Byte key, V value) {
      throw new UnsupportedOperationException("put");
    }
    
    public V remove(Object key) {
      throw new UnsupportedOperationException("remove");
    }
    
    public void putAll(Map<? extends Byte, ? extends V> m) {
      throw new UnsupportedOperationException("putAll");
    }
    
    public Iterable<ByteObjectMap.PrimitiveEntry<V>> entries() {
      if (this.entries == null)
        this.entries = new Iterable<ByteObjectMap.PrimitiveEntry<V>>() {
            public Iterator<ByteObjectMap.PrimitiveEntry<V>> iterator() {
              return new ByteCollections.UnmodifiableMap.IteratorImpl(ByteCollections.UnmodifiableMap.this.map.entries().iterator());
            }
          }; 
      return this.entries;
    }
    
    public Set<Byte> keySet() {
      if (this.keySet == null)
        this.keySet = Collections.unmodifiableSet(this.map.keySet()); 
      return this.keySet;
    }
    
    public Set<Map.Entry<Byte, V>> entrySet() {
      if (this.entrySet == null)
        this.entrySet = Collections.unmodifiableSet(this.map.entrySet()); 
      return this.entrySet;
    }
    
    public Collection<V> values() {
      if (this.values == null)
        this.values = Collections.unmodifiableCollection(this.map.values()); 
      return this.values;
    }
    
    private class IteratorImpl implements Iterator<ByteObjectMap.PrimitiveEntry<V>> {
      final Iterator<ByteObjectMap.PrimitiveEntry<V>> iter;
      
      IteratorImpl(Iterator<ByteObjectMap.PrimitiveEntry<V>> iter) {
        this.iter = iter;
      }
      
      public boolean hasNext() {
        return this.iter.hasNext();
      }
      
      public ByteObjectMap.PrimitiveEntry<V> next() {
        if (!hasNext())
          throw new NoSuchElementException(); 
        return new ByteCollections.UnmodifiableMap.EntryImpl(this.iter.next());
      }
      
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    }
    
    private class EntryImpl implements ByteObjectMap.PrimitiveEntry<V> {
      private final ByteObjectMap.PrimitiveEntry<V> entry;
      
      EntryImpl(ByteObjectMap.PrimitiveEntry<V> entry) {
        this.entry = entry;
      }
      
      public byte key() {
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

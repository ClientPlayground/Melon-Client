package com.github.steveice10.netty.util.collection;

import com.github.steveice10.netty.util.internal.MathUtil;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ByteObjectHashMap<V> implements ByteObjectMap<V> {
  public static final int DEFAULT_CAPACITY = 8;
  
  public static final float DEFAULT_LOAD_FACTOR = 0.5F;
  
  private static final Object NULL_VALUE = new Object();
  
  private int maxSize;
  
  private final float loadFactor;
  
  private byte[] keys;
  
  private V[] values;
  
  private int size;
  
  private int mask;
  
  private final Set<Byte> keySet = new KeySet();
  
  private final Set<Map.Entry<Byte, V>> entrySet = new EntrySet();
  
  private final Iterable<ByteObjectMap.PrimitiveEntry<V>> entries = new Iterable<ByteObjectMap.PrimitiveEntry<V>>() {
      public Iterator<ByteObjectMap.PrimitiveEntry<V>> iterator() {
        return new ByteObjectHashMap.PrimitiveIterator();
      }
    };
  
  public ByteObjectHashMap() {
    this(8, 0.5F);
  }
  
  public ByteObjectHashMap(int initialCapacity) {
    this(initialCapacity, 0.5F);
  }
  
  public ByteObjectHashMap(int initialCapacity, float loadFactor) {
    if (loadFactor <= 0.0F || loadFactor > 1.0F)
      throw new IllegalArgumentException("loadFactor must be > 0 and <= 1"); 
    this.loadFactor = loadFactor;
    int capacity = MathUtil.safeFindNextPositivePowerOfTwo(initialCapacity);
    this.mask = capacity - 1;
    this.keys = new byte[capacity];
    V[] temp = (V[])new Object[capacity];
    this.values = temp;
    this.maxSize = calcMaxSize(capacity);
  }
  
  private static <T> T toExternal(T value) {
    assert value != null : "null is not a legitimate internal value. Concurrent Modification?";
    return (value == NULL_VALUE) ? null : value;
  }
  
  private static <T> T toInternal(T value) {
    return (value == null) ? (T)NULL_VALUE : value;
  }
  
  public V get(byte key) {
    int index = indexOf(key);
    return (index == -1) ? null : toExternal(this.values[index]);
  }
  
  public V put(byte key, V value) {
    int startIndex = hashIndex(key);
    int index = startIndex;
    do {
      if (this.values[index] == null) {
        this.keys[index] = key;
        this.values[index] = toInternal(value);
        growSize();
        return null;
      } 
      if (this.keys[index] == key) {
        V previousValue = this.values[index];
        this.values[index] = toInternal(value);
        return toExternal(previousValue);
      } 
    } while ((index = probeNext(index)) != startIndex);
    throw new IllegalStateException("Unable to insert");
  }
  
  public void putAll(Map<? extends Byte, ? extends V> sourceMap) {
    if (sourceMap instanceof ByteObjectHashMap) {
      ByteObjectHashMap<V> source = (ByteObjectHashMap)sourceMap;
      for (int i = 0; i < source.values.length; i++) {
        V sourceValue = source.values[i];
        if (sourceValue != null)
          put(source.keys[i], sourceValue); 
      } 
      return;
    } 
    for (Map.Entry<? extends Byte, ? extends V> entry : sourceMap.entrySet())
      put(entry.getKey(), entry.getValue()); 
  }
  
  public V remove(byte key) {
    int index = indexOf(key);
    if (index == -1)
      return null; 
    V prev = this.values[index];
    removeAt(index);
    return toExternal(prev);
  }
  
  public int size() {
    return this.size;
  }
  
  public boolean isEmpty() {
    return (this.size == 0);
  }
  
  public void clear() {
    Arrays.fill(this.keys, (byte)0);
    Arrays.fill((Object[])this.values, (Object)null);
    this.size = 0;
  }
  
  public boolean containsKey(byte key) {
    return (indexOf(key) >= 0);
  }
  
  public boolean containsValue(Object value) {
    V v1 = toInternal((V)value);
    for (V v2 : this.values) {
      if (v2 != null && v2.equals(v1))
        return true; 
    } 
    return false;
  }
  
  public Iterable<ByteObjectMap.PrimitiveEntry<V>> entries() {
    return this.entries;
  }
  
  public Collection<V> values() {
    return new AbstractCollection<V>() {
        public Iterator<V> iterator() {
          return new Iterator<V>() {
              final ByteObjectHashMap<V>.PrimitiveIterator iter = new ByteObjectHashMap.PrimitiveIterator();
              
              public boolean hasNext() {
                return this.iter.hasNext();
              }
              
              public V next() {
                return this.iter.next().value();
              }
              
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
        }
        
        public int size() {
          return ByteObjectHashMap.this.size;
        }
      };
  }
  
  public int hashCode() {
    int hash = this.size;
    for (byte key : this.keys)
      hash ^= hashCode(key); 
    return hash;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!(obj instanceof ByteObjectMap))
      return false; 
    ByteObjectMap other = (ByteObjectMap)obj;
    if (this.size != other.size())
      return false; 
    for (int i = 0; i < this.values.length; i++) {
      V value = this.values[i];
      if (value != null) {
        byte key = this.keys[i];
        Object otherValue = other.get(key);
        if (value == NULL_VALUE) {
          if (otherValue != null)
            return false; 
        } else if (!value.equals(otherValue)) {
          return false;
        } 
      } 
    } 
    return true;
  }
  
  public boolean containsKey(Object key) {
    return containsKey(objectToKey(key));
  }
  
  public V get(Object key) {
    return get(objectToKey(key));
  }
  
  public V put(Byte key, V value) {
    return put(objectToKey(key), value);
  }
  
  public V remove(Object key) {
    return remove(objectToKey(key));
  }
  
  public Set<Byte> keySet() {
    return this.keySet;
  }
  
  public Set<Map.Entry<Byte, V>> entrySet() {
    return this.entrySet;
  }
  
  private byte objectToKey(Object key) {
    return ((Byte)key).byteValue();
  }
  
  private int indexOf(byte key) {
    int startIndex = hashIndex(key);
    int index = startIndex;
    while (true) {
      if (this.values[index] == null)
        return -1; 
      if (key == this.keys[index])
        return index; 
      if ((index = probeNext(index)) == startIndex)
        return -1; 
    } 
  }
  
  private int hashIndex(byte key) {
    return hashCode(key) & this.mask;
  }
  
  private static int hashCode(byte key) {
    return key;
  }
  
  private int probeNext(int index) {
    return index + 1 & this.mask;
  }
  
  private void growSize() {
    this.size++;
    if (this.size > this.maxSize) {
      if (this.keys.length == Integer.MAX_VALUE)
        throw new IllegalStateException("Max capacity reached at size=" + this.size); 
      rehash(this.keys.length << 1);
    } 
  }
  
  private boolean removeAt(int index) {
    this.size--;
    this.keys[index] = 0;
    this.values[index] = null;
    int nextFree = index;
    int i = probeNext(index);
    for (V value = this.values[i]; value != null; value = this.values[i = probeNext(i)]) {
      byte key = this.keys[i];
      int bucket = hashIndex(key);
      if ((i < bucket && (bucket <= nextFree || nextFree <= i)) || (bucket <= nextFree && nextFree <= i)) {
        this.keys[nextFree] = key;
        this.values[nextFree] = value;
        this.keys[i] = 0;
        this.values[i] = null;
        nextFree = i;
      } 
    } 
    return (nextFree != index);
  }
  
  private int calcMaxSize(int capacity) {
    int upperBound = capacity - 1;
    return Math.min(upperBound, (int)(capacity * this.loadFactor));
  }
  
  private void rehash(int newCapacity) {
    byte[] oldKeys = this.keys;
    V[] oldVals = this.values;
    this.keys = new byte[newCapacity];
    V[] temp = (V[])new Object[newCapacity];
    this.values = temp;
    this.maxSize = calcMaxSize(newCapacity);
    this.mask = newCapacity - 1;
    for (int i = 0; i < oldVals.length; i++) {
      V oldVal = oldVals[i];
      if (oldVal != null) {
        byte oldKey = oldKeys[i];
        int index = hashIndex(oldKey);
        while (true) {
          if (this.values[index] == null) {
            this.keys[index] = oldKey;
            this.values[index] = oldVal;
            break;
          } 
          index = probeNext(index);
        } 
      } 
    } 
  }
  
  public String toString() {
    if (isEmpty())
      return "{}"; 
    StringBuilder sb = new StringBuilder(4 * this.size);
    sb.append('{');
    boolean first = true;
    for (int i = 0; i < this.values.length; i++) {
      V value = this.values[i];
      if (value != null) {
        if (!first)
          sb.append(", "); 
        sb.append(keyToString(this.keys[i])).append('=').append((value == this) ? "(this Map)" : 
            toExternal(value));
        first = false;
      } 
    } 
    return sb.append('}').toString();
  }
  
  protected String keyToString(byte key) {
    return Byte.toString(key);
  }
  
  private final class EntrySet extends AbstractSet<Map.Entry<Byte, V>> {
    private EntrySet() {}
    
    public Iterator<Map.Entry<Byte, V>> iterator() {
      return new ByteObjectHashMap.MapIterator();
    }
    
    public int size() {
      return ByteObjectHashMap.this.size();
    }
  }
  
  private final class KeySet extends AbstractSet<Byte> {
    private KeySet() {}
    
    public int size() {
      return ByteObjectHashMap.this.size();
    }
    
    public boolean contains(Object o) {
      return ByteObjectHashMap.this.containsKey(o);
    }
    
    public boolean remove(Object o) {
      return (ByteObjectHashMap.this.remove(o) != null);
    }
    
    public boolean retainAll(Collection<?> retainedKeys) {
      boolean changed = false;
      for (Iterator<ByteObjectMap.PrimitiveEntry<V>> iter = ByteObjectHashMap.this.entries().iterator(); iter.hasNext(); ) {
        ByteObjectMap.PrimitiveEntry<V> entry = iter.next();
        if (!retainedKeys.contains(Byte.valueOf(entry.key()))) {
          changed = true;
          iter.remove();
        } 
      } 
      return changed;
    }
    
    public void clear() {
      ByteObjectHashMap.this.clear();
    }
    
    public Iterator<Byte> iterator() {
      return new Iterator<Byte>() {
          private final Iterator<Map.Entry<Byte, V>> iter = ByteObjectHashMap.this.entrySet.iterator();
          
          public boolean hasNext() {
            return this.iter.hasNext();
          }
          
          public Byte next() {
            return (Byte)((Map.Entry)this.iter.next()).getKey();
          }
          
          public void remove() {
            this.iter.remove();
          }
        };
    }
  }
  
  private final class PrimitiveIterator implements Iterator<ByteObjectMap.PrimitiveEntry<V>>, ByteObjectMap.PrimitiveEntry<V> {
    private int prevIndex = -1;
    
    private int nextIndex = -1;
    
    private int entryIndex = -1;
    
    private void scanNext() {
      while (++this.nextIndex != ByteObjectHashMap.this.values.length && ByteObjectHashMap.this.values[this.nextIndex] == null);
    }
    
    public boolean hasNext() {
      if (this.nextIndex == -1)
        scanNext(); 
      return (this.nextIndex != ByteObjectHashMap.this.values.length);
    }
    
    public ByteObjectMap.PrimitiveEntry<V> next() {
      if (!hasNext())
        throw new NoSuchElementException(); 
      this.prevIndex = this.nextIndex;
      scanNext();
      this.entryIndex = this.prevIndex;
      return this;
    }
    
    public void remove() {
      if (this.prevIndex == -1)
        throw new IllegalStateException("next must be called before each remove."); 
      if (ByteObjectHashMap.this.removeAt(this.prevIndex))
        this.nextIndex = this.prevIndex; 
      this.prevIndex = -1;
    }
    
    public byte key() {
      return ByteObjectHashMap.this.keys[this.entryIndex];
    }
    
    public V value() {
      return (V)ByteObjectHashMap.toExternal((T)ByteObjectHashMap.this.values[this.entryIndex]);
    }
    
    public void setValue(V value) {
      ByteObjectHashMap.this.values[this.entryIndex] = ByteObjectHashMap.toInternal((T)value);
    }
    
    private PrimitiveIterator() {}
  }
  
  private final class MapIterator implements Iterator<Map.Entry<Byte, V>> {
    private final ByteObjectHashMap<V>.PrimitiveIterator iter = new ByteObjectHashMap.PrimitiveIterator();
    
    public boolean hasNext() {
      return this.iter.hasNext();
    }
    
    public Map.Entry<Byte, V> next() {
      if (!hasNext())
        throw new NoSuchElementException(); 
      this.iter.next();
      return new ByteObjectHashMap.MapEntry(this.iter.entryIndex);
    }
    
    public void remove() {
      this.iter.remove();
    }
    
    private MapIterator() {}
  }
  
  final class MapEntry implements Map.Entry<Byte, V> {
    private final int entryIndex;
    
    MapEntry(int entryIndex) {
      this.entryIndex = entryIndex;
    }
    
    public Byte getKey() {
      verifyExists();
      return Byte.valueOf(ByteObjectHashMap.this.keys[this.entryIndex]);
    }
    
    public V getValue() {
      verifyExists();
      return (V)ByteObjectHashMap.toExternal((T)ByteObjectHashMap.this.values[this.entryIndex]);
    }
    
    public V setValue(V value) {
      verifyExists();
      V prevValue = (V)ByteObjectHashMap.toExternal((T)ByteObjectHashMap.this.values[this.entryIndex]);
      ByteObjectHashMap.this.values[this.entryIndex] = ByteObjectHashMap.toInternal((T)value);
      return prevValue;
    }
    
    private void verifyExists() {
      if (ByteObjectHashMap.this.values[this.entryIndex] == null)
        throw new IllegalStateException("The map entry has been removed"); 
    }
  }
}

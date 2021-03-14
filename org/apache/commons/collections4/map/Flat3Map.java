package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.collections4.iterators.EmptyMapIterator;

public class Flat3Map<K, V> implements IterableMap<K, V>, Serializable, Cloneable {
  private static final long serialVersionUID = -6701087419741928296L;
  
  private transient int size;
  
  private transient int hash1;
  
  private transient int hash2;
  
  private transient int hash3;
  
  private transient K key1;
  
  private transient K key2;
  
  private transient K key3;
  
  private transient V value1;
  
  private transient V value2;
  
  private transient V value3;
  
  private transient AbstractHashedMap<K, V> delegateMap;
  
  public Flat3Map() {}
  
  public Flat3Map(Map<? extends K, ? extends V> map) {
    putAll(map);
  }
  
  public V get(Object key) {
    if (this.delegateMap != null)
      return this.delegateMap.get(key); 
    if (key == null) {
      switch (this.size) {
        case 3:
          if (this.key3 == null)
            return this.value3; 
        case 2:
          if (this.key2 == null)
            return this.value2; 
        case 1:
          if (this.key1 == null)
            return this.value1; 
          break;
      } 
    } else if (this.size > 0) {
      int hashCode = key.hashCode();
      switch (this.size) {
        case 3:
          if (this.hash3 == hashCode && key.equals(this.key3))
            return this.value3; 
        case 2:
          if (this.hash2 == hashCode && key.equals(this.key2))
            return this.value2; 
        case 1:
          if (this.hash1 == hashCode && key.equals(this.key1))
            return this.value1; 
          break;
      } 
    } 
    return null;
  }
  
  public int size() {
    if (this.delegateMap != null)
      return this.delegateMap.size(); 
    return this.size;
  }
  
  public boolean isEmpty() {
    return (size() == 0);
  }
  
  public boolean containsKey(Object key) {
    if (this.delegateMap != null)
      return this.delegateMap.containsKey(key); 
    if (key == null) {
      switch (this.size) {
        case 3:
          if (this.key3 == null)
            return true; 
        case 2:
          if (this.key2 == null)
            return true; 
        case 1:
          if (this.key1 == null)
            return true; 
          break;
      } 
    } else if (this.size > 0) {
      int hashCode = key.hashCode();
      switch (this.size) {
        case 3:
          if (this.hash3 == hashCode && key.equals(this.key3))
            return true; 
        case 2:
          if (this.hash2 == hashCode && key.equals(this.key2))
            return true; 
        case 1:
          if (this.hash1 == hashCode && key.equals(this.key1))
            return true; 
          break;
      } 
    } 
    return false;
  }
  
  public boolean containsValue(Object value) {
    if (this.delegateMap != null)
      return this.delegateMap.containsValue(value); 
    if (value == null) {
      switch (this.size) {
        case 3:
          if (this.value3 == null)
            return true; 
        case 2:
          if (this.value2 == null)
            return true; 
        case 1:
          if (this.value1 == null)
            return true; 
          break;
      } 
    } else {
      switch (this.size) {
        case 3:
          if (value.equals(this.value3))
            return true; 
        case 2:
          if (value.equals(this.value2))
            return true; 
        case 1:
          if (value.equals(this.value1))
            return true; 
          break;
      } 
    } 
    return false;
  }
  
  public V put(K key, V value) {
    if (this.delegateMap != null)
      return this.delegateMap.put(key, value); 
    if (key == null) {
      switch (this.size) {
        case 3:
          if (this.key3 == null) {
            V old = this.value3;
            this.value3 = value;
            return old;
          } 
        case 2:
          if (this.key2 == null) {
            V old = this.value2;
            this.value2 = value;
            return old;
          } 
        case 1:
          if (this.key1 == null) {
            V old = this.value1;
            this.value1 = value;
            return old;
          } 
          break;
      } 
    } else if (this.size > 0) {
      int hashCode = key.hashCode();
      switch (this.size) {
        case 3:
          if (this.hash3 == hashCode && key.equals(this.key3)) {
            V old = this.value3;
            this.value3 = value;
            return old;
          } 
        case 2:
          if (this.hash2 == hashCode && key.equals(this.key2)) {
            V old = this.value2;
            this.value2 = value;
            return old;
          } 
        case 1:
          if (this.hash1 == hashCode && key.equals(this.key1)) {
            V old = this.value1;
            this.value1 = value;
            return old;
          } 
          break;
      } 
    } 
    switch (this.size) {
      default:
        convertToMap();
        this.delegateMap.put(key, value);
        return null;
      case 2:
        this.hash3 = (key == null) ? 0 : key.hashCode();
        this.key3 = key;
        this.value3 = value;
        this.size++;
        return null;
      case 1:
        this.hash2 = (key == null) ? 0 : key.hashCode();
        this.key2 = key;
        this.value2 = value;
        this.size++;
        return null;
      case 0:
        break;
    } 
    this.hash1 = (key == null) ? 0 : key.hashCode();
    this.key1 = key;
    this.value1 = value;
    this.size++;
    return null;
  }
  
  public void putAll(Map<? extends K, ? extends V> map) {
    int size = map.size();
    if (size == 0)
      return; 
    if (this.delegateMap != null) {
      this.delegateMap.putAll(map);
      return;
    } 
    if (size < 4) {
      for (Map.Entry<? extends K, ? extends V> entry : map.entrySet())
        put(entry.getKey(), entry.getValue()); 
    } else {
      convertToMap();
      this.delegateMap.putAll(map);
    } 
  }
  
  private void convertToMap() {
    this.delegateMap = createDelegateMap();
    switch (this.size) {
      case 3:
        this.delegateMap.put(this.key3, this.value3);
      case 2:
        this.delegateMap.put(this.key2, this.value2);
      case 1:
        this.delegateMap.put(this.key1, this.value1);
        break;
      case 0:
        break;
      default:
        throw new IllegalStateException("Invalid map index: " + this.size);
    } 
    this.size = 0;
    this.hash1 = this.hash2 = this.hash3 = 0;
    this.key1 = this.key2 = this.key3 = null;
    this.value1 = this.value2 = this.value3 = null;
  }
  
  protected AbstractHashedMap<K, V> createDelegateMap() {
    return new HashedMap<K, V>();
  }
  
  public V remove(Object key) {
    if (this.delegateMap != null)
      return this.delegateMap.remove(key); 
    if (this.size == 0)
      return null; 
    if (key == null) {
      switch (this.size) {
        case 3:
          if (this.key3 == null) {
            V old = this.value3;
            this.hash3 = 0;
            this.key3 = null;
            this.value3 = null;
            this.size = 2;
            return old;
          } 
          if (this.key2 == null) {
            V old = this.value2;
            this.hash2 = this.hash3;
            this.key2 = this.key3;
            this.value2 = this.value3;
            this.hash3 = 0;
            this.key3 = null;
            this.value3 = null;
            this.size = 2;
            return old;
          } 
          if (this.key1 == null) {
            V old = this.value1;
            this.hash1 = this.hash3;
            this.key1 = this.key3;
            this.value1 = this.value3;
            this.hash3 = 0;
            this.key3 = null;
            this.value3 = null;
            this.size = 2;
            return old;
          } 
          return null;
        case 2:
          if (this.key2 == null) {
            V old = this.value2;
            this.hash2 = 0;
            this.key2 = null;
            this.value2 = null;
            this.size = 1;
            return old;
          } 
          if (this.key1 == null) {
            V old = this.value1;
            this.hash1 = this.hash2;
            this.key1 = this.key2;
            this.value1 = this.value2;
            this.hash2 = 0;
            this.key2 = null;
            this.value2 = null;
            this.size = 1;
            return old;
          } 
          return null;
        case 1:
          if (this.key1 == null) {
            V old = this.value1;
            this.hash1 = 0;
            this.key1 = null;
            this.value1 = null;
            this.size = 0;
            return old;
          } 
          break;
      } 
    } else if (this.size > 0) {
      int hashCode = key.hashCode();
      switch (this.size) {
        case 3:
          if (this.hash3 == hashCode && key.equals(this.key3)) {
            V old = this.value3;
            this.hash3 = 0;
            this.key3 = null;
            this.value3 = null;
            this.size = 2;
            return old;
          } 
          if (this.hash2 == hashCode && key.equals(this.key2)) {
            V old = this.value2;
            this.hash2 = this.hash3;
            this.key2 = this.key3;
            this.value2 = this.value3;
            this.hash3 = 0;
            this.key3 = null;
            this.value3 = null;
            this.size = 2;
            return old;
          } 
          if (this.hash1 == hashCode && key.equals(this.key1)) {
            V old = this.value1;
            this.hash1 = this.hash3;
            this.key1 = this.key3;
            this.value1 = this.value3;
            this.hash3 = 0;
            this.key3 = null;
            this.value3 = null;
            this.size = 2;
            return old;
          } 
          return null;
        case 2:
          if (this.hash2 == hashCode && key.equals(this.key2)) {
            V old = this.value2;
            this.hash2 = 0;
            this.key2 = null;
            this.value2 = null;
            this.size = 1;
            return old;
          } 
          if (this.hash1 == hashCode && key.equals(this.key1)) {
            V old = this.value1;
            this.hash1 = this.hash2;
            this.key1 = this.key2;
            this.value1 = this.value2;
            this.hash2 = 0;
            this.key2 = null;
            this.value2 = null;
            this.size = 1;
            return old;
          } 
          return null;
        case 1:
          if (this.hash1 == hashCode && key.equals(this.key1)) {
            V old = this.value1;
            this.hash1 = 0;
            this.key1 = null;
            this.value1 = null;
            this.size = 0;
            return old;
          } 
          break;
      } 
    } 
    return null;
  }
  
  public void clear() {
    if (this.delegateMap != null) {
      this.delegateMap.clear();
      this.delegateMap = null;
    } else {
      this.size = 0;
      this.hash1 = this.hash2 = this.hash3 = 0;
      this.key1 = this.key2 = this.key3 = null;
      this.value1 = this.value2 = this.value3 = null;
    } 
  }
  
  public MapIterator<K, V> mapIterator() {
    if (this.delegateMap != null)
      return this.delegateMap.mapIterator(); 
    if (this.size == 0)
      return EmptyMapIterator.emptyMapIterator(); 
    return new FlatMapIterator<K, V>(this);
  }
  
  static class FlatMapIterator<K, V> implements MapIterator<K, V>, ResettableIterator<K> {
    private final Flat3Map<K, V> parent;
    
    private int nextIndex = 0;
    
    private boolean canRemove = false;
    
    FlatMapIterator(Flat3Map<K, V> parent) {
      this.parent = parent;
    }
    
    public boolean hasNext() {
      return (this.nextIndex < this.parent.size);
    }
    
    public K next() {
      if (!hasNext())
        throw new NoSuchElementException("No next() entry in the iteration"); 
      this.canRemove = true;
      this.nextIndex++;
      return getKey();
    }
    
    public void remove() {
      if (!this.canRemove)
        throw new IllegalStateException("remove() can only be called once after next()"); 
      this.parent.remove(getKey());
      this.nextIndex--;
      this.canRemove = false;
    }
    
    public K getKey() {
      if (!this.canRemove)
        throw new IllegalStateException("getKey() can only be called after next() and before remove()"); 
      switch (this.nextIndex) {
        case 3:
          return this.parent.key3;
        case 2:
          return this.parent.key2;
        case 1:
          return this.parent.key1;
      } 
      throw new IllegalStateException("Invalid map index: " + this.nextIndex);
    }
    
    public V getValue() {
      if (!this.canRemove)
        throw new IllegalStateException("getValue() can only be called after next() and before remove()"); 
      switch (this.nextIndex) {
        case 3:
          return this.parent.value3;
        case 2:
          return this.parent.value2;
        case 1:
          return this.parent.value1;
      } 
      throw new IllegalStateException("Invalid map index: " + this.nextIndex);
    }
    
    public V setValue(V value) {
      if (!this.canRemove)
        throw new IllegalStateException("setValue() can only be called after next() and before remove()"); 
      V old = getValue();
      switch (this.nextIndex) {
        case 3:
          this.parent.value3 = value;
          return old;
        case 2:
          this.parent.value2 = value;
          return old;
        case 1:
          this.parent.value1 = value;
          return old;
      } 
      throw new IllegalStateException("Invalid map index: " + this.nextIndex);
    }
    
    public void reset() {
      this.nextIndex = 0;
      this.canRemove = false;
    }
    
    public String toString() {
      if (this.canRemove)
        return "Iterator[" + getKey() + "=" + getValue() + "]"; 
      return "Iterator[]";
    }
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    if (this.delegateMap != null)
      return this.delegateMap.entrySet(); 
    return new EntrySet<K, V>(this);
  }
  
  static class EntrySet<K, V> extends AbstractSet<Map.Entry<K, V>> {
    private final Flat3Map<K, V> parent;
    
    EntrySet(Flat3Map<K, V> parent) {
      this.parent = parent;
    }
    
    public int size() {
      return this.parent.size();
    }
    
    public void clear() {
      this.parent.clear();
    }
    
    public boolean remove(Object obj) {
      if (!(obj instanceof Map.Entry))
        return false; 
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
      Object key = entry.getKey();
      boolean result = this.parent.containsKey(key);
      this.parent.remove(key);
      return result;
    }
    
    public Iterator<Map.Entry<K, V>> iterator() {
      if (this.parent.delegateMap != null)
        return this.parent.delegateMap.entrySet().iterator(); 
      if (this.parent.size() == 0)
        return EmptyIterator.emptyIterator(); 
      return new Flat3Map.EntrySetIterator<K, V>(this.parent);
    }
  }
  
  static class FlatMapEntry<K, V> implements Map.Entry<K, V> {
    private final Flat3Map<K, V> parent;
    
    private final int index;
    
    private volatile boolean removed;
    
    public FlatMapEntry(Flat3Map<K, V> parent, int index) {
      this.parent = parent;
      this.index = index;
      this.removed = false;
    }
    
    void setRemoved(boolean flag) {
      this.removed = flag;
    }
    
    public K getKey() {
      if (this.removed)
        throw new IllegalStateException("getKey() can only be called after next() and before remove()"); 
      switch (this.index) {
        case 3:
          return this.parent.key3;
        case 2:
          return this.parent.key2;
        case 1:
          return this.parent.key1;
      } 
      throw new IllegalStateException("Invalid map index: " + this.index);
    }
    
    public V getValue() {
      if (this.removed)
        throw new IllegalStateException("getValue() can only be called after next() and before remove()"); 
      switch (this.index) {
        case 3:
          return this.parent.value3;
        case 2:
          return this.parent.value2;
        case 1:
          return this.parent.value1;
      } 
      throw new IllegalStateException("Invalid map index: " + this.index);
    }
    
    public V setValue(V value) {
      if (this.removed)
        throw new IllegalStateException("setValue() can only be called after next() and before remove()"); 
      V old = getValue();
      switch (this.index) {
        case 3:
          this.parent.value3 = value;
          return old;
        case 2:
          this.parent.value2 = value;
          return old;
        case 1:
          this.parent.value1 = value;
          return old;
      } 
      throw new IllegalStateException("Invalid map index: " + this.index);
    }
    
    public boolean equals(Object obj) {
      if (this.removed)
        return false; 
      if (!(obj instanceof Map.Entry))
        return false; 
      Map.Entry<?, ?> other = (Map.Entry<?, ?>)obj;
      Object key = getKey();
      Object value = getValue();
      return (((key == null) ? (other.getKey() == null) : key.equals(other.getKey())) && ((value == null) ? (other.getValue() == null) : value.equals(other.getValue())));
    }
    
    public int hashCode() {
      if (this.removed)
        return 0; 
      Object key = getKey();
      Object value = getValue();
      return ((key == null) ? 0 : key.hashCode()) ^ ((value == null) ? 0 : value.hashCode());
    }
    
    public String toString() {
      if (!this.removed)
        return (new StringBuilder()).append(getKey()).append("=").append(getValue()).toString(); 
      return "";
    }
  }
  
  static abstract class EntryIterator<K, V> {
    private final Flat3Map<K, V> parent;
    
    private int nextIndex = 0;
    
    private Flat3Map.FlatMapEntry<K, V> currentEntry = null;
    
    public EntryIterator(Flat3Map<K, V> parent) {
      this.parent = parent;
    }
    
    public boolean hasNext() {
      return (this.nextIndex < this.parent.size);
    }
    
    public Map.Entry<K, V> nextEntry() {
      if (!hasNext())
        throw new NoSuchElementException("No next() entry in the iteration"); 
      this.currentEntry = new Flat3Map.FlatMapEntry<K, V>(this.parent, ++this.nextIndex);
      return this.currentEntry;
    }
    
    public void remove() {
      if (this.currentEntry == null)
        throw new IllegalStateException("remove() can only be called once after next()"); 
      this.currentEntry.setRemoved(true);
      this.parent.remove(this.currentEntry.getKey());
      this.nextIndex--;
      this.currentEntry = null;
    }
  }
  
  static class EntrySetIterator<K, V> extends EntryIterator<K, V> implements Iterator<Map.Entry<K, V>> {
    EntrySetIterator(Flat3Map<K, V> parent) {
      super(parent);
    }
    
    public Map.Entry<K, V> next() {
      return nextEntry();
    }
  }
  
  public Set<K> keySet() {
    if (this.delegateMap != null)
      return this.delegateMap.keySet(); 
    return new KeySet<K>(this);
  }
  
  static class KeySet<K> extends AbstractSet<K> {
    private final Flat3Map<K, ?> parent;
    
    KeySet(Flat3Map<K, ?> parent) {
      this.parent = parent;
    }
    
    public int size() {
      return this.parent.size();
    }
    
    public void clear() {
      this.parent.clear();
    }
    
    public boolean contains(Object key) {
      return this.parent.containsKey(key);
    }
    
    public boolean remove(Object key) {
      boolean result = this.parent.containsKey(key);
      this.parent.remove(key);
      return result;
    }
    
    public Iterator<K> iterator() {
      if (this.parent.delegateMap != null)
        return this.parent.delegateMap.keySet().iterator(); 
      if (this.parent.size() == 0)
        return EmptyIterator.emptyIterator(); 
      return new Flat3Map.KeySetIterator<K>(this.parent);
    }
  }
  
  static class KeySetIterator<K> extends EntryIterator<K, Object> implements Iterator<K> {
    KeySetIterator(Flat3Map<K, ?> parent) {
      super((Flat3Map)parent);
    }
    
    public K next() {
      return nextEntry().getKey();
    }
  }
  
  public Collection<V> values() {
    if (this.delegateMap != null)
      return this.delegateMap.values(); 
    return new Values<V>(this);
  }
  
  static class Values<V> extends AbstractCollection<V> {
    private final Flat3Map<?, V> parent;
    
    Values(Flat3Map<?, V> parent) {
      this.parent = parent;
    }
    
    public int size() {
      return this.parent.size();
    }
    
    public void clear() {
      this.parent.clear();
    }
    
    public boolean contains(Object value) {
      return this.parent.containsValue(value);
    }
    
    public Iterator<V> iterator() {
      if (this.parent.delegateMap != null)
        return this.parent.delegateMap.values().iterator(); 
      if (this.parent.size() == 0)
        return EmptyIterator.emptyIterator(); 
      return new Flat3Map.ValuesIterator<V>(this.parent);
    }
  }
  
  static class ValuesIterator<V> extends EntryIterator<Object, V> implements Iterator<V> {
    ValuesIterator(Flat3Map<?, V> parent) {
      super((Flat3Map)parent);
    }
    
    public V next() {
      return nextEntry().getValue();
    }
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeInt(size());
    for (MapIterator<?, ?> it = mapIterator(); it.hasNext(); ) {
      out.writeObject(it.next());
      out.writeObject(it.getValue());
    } 
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    int count = in.readInt();
    if (count > 3)
      this.delegateMap = createDelegateMap(); 
    for (int i = count; i > 0; i--)
      put((K)in.readObject(), (V)in.readObject()); 
  }
  
  public Flat3Map<K, V> clone() {
    try {
      Flat3Map<K, V> cloned = (Flat3Map<K, V>)super.clone();
      if (cloned.delegateMap != null)
        cloned.delegateMap = cloned.delegateMap.clone(); 
      return cloned;
    } catch (CloneNotSupportedException ex) {
      throw new InternalError();
    } 
  }
  
  public boolean equals(Object obj) {
    if (obj == this)
      return true; 
    if (this.delegateMap != null)
      return this.delegateMap.equals(obj); 
    if (!(obj instanceof Map))
      return false; 
    Map<?, ?> other = (Map<?, ?>)obj;
    if (this.size != other.size())
      return false; 
    if (this.size > 0) {
      Object otherValue = null;
      switch (this.size) {
        case 3:
          if (!other.containsKey(this.key3))
            return false; 
          otherValue = other.get(this.key3);
          if ((this.value3 == null) ? (otherValue != null) : !this.value3.equals(otherValue))
            return false; 
        case 2:
          if (!other.containsKey(this.key2))
            return false; 
          otherValue = other.get(this.key2);
          if ((this.value2 == null) ? (otherValue != null) : !this.value2.equals(otherValue))
            return false; 
        case 1:
          if (!other.containsKey(this.key1))
            return false; 
          otherValue = other.get(this.key1);
          if ((this.value1 == null) ? (otherValue != null) : !this.value1.equals(otherValue))
            return false; 
          break;
      } 
    } 
    return true;
  }
  
  public int hashCode() {
    if (this.delegateMap != null)
      return this.delegateMap.hashCode(); 
    int total = 0;
    switch (this.size) {
      case 3:
        total += this.hash3 ^ ((this.value3 == null) ? 0 : this.value3.hashCode());
      case 2:
        total += this.hash2 ^ ((this.value2 == null) ? 0 : this.value2.hashCode());
      case 1:
        total += this.hash1 ^ ((this.value1 == null) ? 0 : this.value1.hashCode());
      case 0:
        return total;
    } 
    throw new IllegalStateException("Invalid map index: " + this.size);
  }
  
  public String toString() {
    if (this.delegateMap != null)
      return this.delegateMap.toString(); 
    if (this.size == 0)
      return "{}"; 
    StringBuilder buf = new StringBuilder(128);
    buf.append('{');
    switch (this.size) {
      case 3:
        buf.append((this.key3 == this) ? "(this Map)" : this.key3);
        buf.append('=');
        buf.append((this.value3 == this) ? "(this Map)" : this.value3);
        buf.append(',');
      case 2:
        buf.append((this.key2 == this) ? "(this Map)" : this.key2);
        buf.append('=');
        buf.append((this.value2 == this) ? "(this Map)" : this.value2);
        buf.append(',');
      case 1:
        buf.append((this.key1 == this) ? "(this Map)" : this.key1);
        buf.append('=');
        buf.append((this.value1 == this) ? "(this Map)" : this.value1);
        buf.append('}');
        return buf.toString();
    } 
    throw new IllegalStateException("Invalid map index: " + this.size);
  }
}

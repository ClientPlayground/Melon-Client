package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;

public abstract class AbstractReferenceMap<K, V> extends AbstractHashedMap<K, V> {
  private ReferenceStrength keyType;
  
  private ReferenceStrength valueType;
  
  private boolean purgeValues;
  
  private transient ReferenceQueue<Object> queue;
  
  public enum ReferenceStrength {
    HARD(0),
    SOFT(1),
    WEAK(2);
    
    public final int value;
    
    public static ReferenceStrength resolve(int value) {
      switch (value) {
        case 0:
          return HARD;
        case 1:
          return SOFT;
        case 2:
          return WEAK;
      } 
      throw new IllegalArgumentException();
    }
    
    ReferenceStrength(int value) {
      this.value = value;
    }
  }
  
  protected AbstractReferenceMap() {}
  
  protected AbstractReferenceMap(ReferenceStrength keyType, ReferenceStrength valueType, int capacity, float loadFactor, boolean purgeValues) {
    super(capacity, loadFactor);
    this.keyType = keyType;
    this.valueType = valueType;
    this.purgeValues = purgeValues;
  }
  
  protected void init() {
    this.queue = new ReferenceQueue();
  }
  
  public int size() {
    purgeBeforeRead();
    return super.size();
  }
  
  public boolean isEmpty() {
    purgeBeforeRead();
    return super.isEmpty();
  }
  
  public boolean containsKey(Object key) {
    purgeBeforeRead();
    Map.Entry<K, V> entry = getEntry(key);
    if (entry == null)
      return false; 
    return (entry.getValue() != null);
  }
  
  public boolean containsValue(Object value) {
    purgeBeforeRead();
    if (value == null)
      return false; 
    return super.containsValue(value);
  }
  
  public V get(Object key) {
    purgeBeforeRead();
    Map.Entry<K, V> entry = getEntry(key);
    if (entry == null)
      return null; 
    return entry.getValue();
  }
  
  public V put(K key, V value) {
    if (key == null)
      throw new NullPointerException("null keys not allowed"); 
    if (value == null)
      throw new NullPointerException("null values not allowed"); 
    purgeBeforeWrite();
    return super.put(key, value);
  }
  
  public V remove(Object key) {
    if (key == null)
      return null; 
    purgeBeforeWrite();
    return super.remove(key);
  }
  
  public void clear() {
    super.clear();
    while (this.queue.poll() != null);
  }
  
  public MapIterator<K, V> mapIterator() {
    return new ReferenceMapIterator<K, V>(this);
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    if (this.entrySet == null)
      this.entrySet = new ReferenceEntrySet<K, V>(this); 
    return this.entrySet;
  }
  
  public Set<K> keySet() {
    if (this.keySet == null)
      this.keySet = new ReferenceKeySet<K>(this); 
    return this.keySet;
  }
  
  public Collection<V> values() {
    if (this.values == null)
      this.values = new ReferenceValues<V>(this); 
    return this.values;
  }
  
  protected void purgeBeforeRead() {
    purge();
  }
  
  protected void purgeBeforeWrite() {
    purge();
  }
  
  protected void purge() {
    Reference<?> ref = this.queue.poll();
    while (ref != null) {
      purge(ref);
      ref = this.queue.poll();
    } 
  }
  
  protected void purge(Reference<?> ref) {
    int hash = ref.hashCode();
    int index = hashIndex(hash, this.data.length);
    AbstractHashedMap.HashEntry<K, V> previous = null;
    AbstractHashedMap.HashEntry<K, V> entry = this.data[index];
    while (entry != null) {
      if (((ReferenceEntry)entry).purge(ref)) {
        if (previous == null) {
          this.data[index] = entry.next;
        } else {
          previous.next = entry.next;
        } 
        this.size--;
        return;
      } 
      previous = entry;
      entry = entry.next;
    } 
  }
  
  protected AbstractHashedMap.HashEntry<K, V> getEntry(Object key) {
    if (key == null)
      return null; 
    return super.getEntry(key);
  }
  
  protected int hashEntry(Object key, Object value) {
    return ((key == null) ? 0 : key.hashCode()) ^ ((value == null) ? 0 : value.hashCode());
  }
  
  protected boolean isEqualKey(Object key1, Object key2) {
    key2 = (this.keyType == ReferenceStrength.HARD) ? key2 : ((Reference)key2).get();
    return (key1 == key2 || key1.equals(key2));
  }
  
  protected ReferenceEntry<K, V> createEntry(AbstractHashedMap.HashEntry<K, V> next, int hashCode, K key, V value) {
    return new ReferenceEntry<K, V>(this, next, hashCode, key, value);
  }
  
  protected Iterator<Map.Entry<K, V>> createEntrySetIterator() {
    return new ReferenceEntrySetIterator<K, V>(this);
  }
  
  protected Iterator<K> createKeySetIterator() {
    return new ReferenceKeySetIterator<K>(this);
  }
  
  protected Iterator<V> createValuesIterator() {
    return new ReferenceValuesIterator<V>(this);
  }
  
  static class ReferenceEntrySet<K, V> extends AbstractHashedMap.EntrySet<K, V> {
    protected ReferenceEntrySet(AbstractHashedMap<K, V> parent) {
      super(parent);
    }
    
    public Object[] toArray() {
      return toArray(new Object[size()]);
    }
    
    public <T> T[] toArray(T[] arr) {
      ArrayList<Map.Entry<K, V>> list = new ArrayList<Map.Entry<K, V>>(size());
      for (Map.Entry<K, V> entry : this)
        list.add(new DefaultMapEntry(entry)); 
      return list.toArray(arr);
    }
  }
  
  static class ReferenceKeySet<K> extends AbstractHashedMap.KeySet<K> {
    protected ReferenceKeySet(AbstractHashedMap<K, ?> parent) {
      super(parent);
    }
    
    public Object[] toArray() {
      return toArray(new Object[size()]);
    }
    
    public <T> T[] toArray(T[] arr) {
      List<K> list = new ArrayList<K>(size());
      for (K key : this)
        list.add(key); 
      return list.toArray(arr);
    }
  }
  
  static class ReferenceValues<V> extends AbstractHashedMap.Values<V> {
    protected ReferenceValues(AbstractHashedMap<?, V> parent) {
      super(parent);
    }
    
    public Object[] toArray() {
      return toArray(new Object[size()]);
    }
    
    public <T> T[] toArray(T[] arr) {
      List<V> list = new ArrayList<V>(size());
      for (V value : this)
        list.add(value); 
      return list.toArray(arr);
    }
  }
  
  protected static class ReferenceEntry<K, V> extends AbstractHashedMap.HashEntry<K, V> {
    private final AbstractReferenceMap<K, V> parent;
    
    public ReferenceEntry(AbstractReferenceMap<K, V> parent, AbstractHashedMap.HashEntry<K, V> next, int hashCode, K key, V value) {
      super(next, hashCode, null, null);
      this.parent = parent;
      this.key = toReference(parent.keyType, key, hashCode);
      this.value = toReference(parent.valueType, value, hashCode);
    }
    
    public K getKey() {
      return (this.parent.keyType == AbstractReferenceMap.ReferenceStrength.HARD) ? (K)this.key : ((Reference<K>)this.key).get();
    }
    
    public V getValue() {
      return (this.parent.valueType == AbstractReferenceMap.ReferenceStrength.HARD) ? (V)this.value : ((Reference<V>)this.value).get();
    }
    
    public V setValue(V obj) {
      V old = getValue();
      if (this.parent.valueType != AbstractReferenceMap.ReferenceStrength.HARD)
        ((Reference)this.value).clear(); 
      this.value = toReference(this.parent.valueType, obj, this.hashCode);
      return old;
    }
    
    public boolean equals(Object obj) {
      if (obj == this)
        return true; 
      if (!(obj instanceof Map.Entry))
        return false; 
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
      Object entryKey = entry.getKey();
      Object entryValue = entry.getValue();
      if (entryKey == null || entryValue == null)
        return false; 
      return (this.parent.isEqualKey(entryKey, this.key) && this.parent.isEqualValue(entryValue, getValue()));
    }
    
    public int hashCode() {
      return this.parent.hashEntry(getKey(), getValue());
    }
    
    protected <T> Object toReference(AbstractReferenceMap.ReferenceStrength type, T referent, int hash) {
      if (type == AbstractReferenceMap.ReferenceStrength.HARD)
        return referent; 
      if (type == AbstractReferenceMap.ReferenceStrength.SOFT)
        return new AbstractReferenceMap.SoftRef<T>(hash, referent, this.parent.queue); 
      if (type == AbstractReferenceMap.ReferenceStrength.WEAK)
        return new AbstractReferenceMap.WeakRef<T>(hash, referent, this.parent.queue); 
      throw new Error();
    }
    
    boolean purge(Reference<?> ref) {
      boolean r = (this.parent.keyType != AbstractReferenceMap.ReferenceStrength.HARD && this.key == ref);
      r = (r || (this.parent.valueType != AbstractReferenceMap.ReferenceStrength.HARD && this.value == ref));
      if (r) {
        if (this.parent.keyType != AbstractReferenceMap.ReferenceStrength.HARD)
          ((Reference)this.key).clear(); 
        if (this.parent.valueType != AbstractReferenceMap.ReferenceStrength.HARD) {
          ((Reference)this.value).clear();
        } else if (this.parent.purgeValues) {
          this.value = null;
        } 
      } 
      return r;
    }
    
    protected ReferenceEntry<K, V> next() {
      return (ReferenceEntry<K, V>)this.next;
    }
  }
  
  static class ReferenceBaseIterator<K, V> {
    final AbstractReferenceMap<K, V> parent;
    
    int index;
    
    AbstractReferenceMap.ReferenceEntry<K, V> entry;
    
    AbstractReferenceMap.ReferenceEntry<K, V> previous;
    
    K currentKey;
    
    K nextKey;
    
    V currentValue;
    
    V nextValue;
    
    int expectedModCount;
    
    public ReferenceBaseIterator(AbstractReferenceMap<K, V> parent) {
      this.parent = parent;
      this.index = (parent.size() != 0) ? parent.data.length : 0;
      this.expectedModCount = parent.modCount;
    }
    
    public boolean hasNext() {
      checkMod();
      while (nextNull()) {
        AbstractReferenceMap.ReferenceEntry<K, V> e = this.entry;
        int i = this.index;
        while (e == null && i > 0) {
          i--;
          e = (AbstractReferenceMap.ReferenceEntry<K, V>)this.parent.data[i];
        } 
        this.entry = e;
        this.index = i;
        if (e == null) {
          this.currentKey = null;
          this.currentValue = null;
          return false;
        } 
        this.nextKey = e.getKey();
        this.nextValue = e.getValue();
        if (nextNull())
          this.entry = this.entry.next(); 
      } 
      return true;
    }
    
    private void checkMod() {
      if (this.parent.modCount != this.expectedModCount)
        throw new ConcurrentModificationException(); 
    }
    
    private boolean nextNull() {
      return (this.nextKey == null || this.nextValue == null);
    }
    
    protected AbstractReferenceMap.ReferenceEntry<K, V> nextEntry() {
      checkMod();
      if (nextNull() && !hasNext())
        throw new NoSuchElementException(); 
      this.previous = this.entry;
      this.entry = this.entry.next();
      this.currentKey = this.nextKey;
      this.currentValue = this.nextValue;
      this.nextKey = null;
      this.nextValue = null;
      return this.previous;
    }
    
    protected AbstractReferenceMap.ReferenceEntry<K, V> currentEntry() {
      checkMod();
      return this.previous;
    }
    
    public void remove() {
      checkMod();
      if (this.previous == null)
        throw new IllegalStateException(); 
      this.parent.remove(this.currentKey);
      this.previous = null;
      this.currentKey = null;
      this.currentValue = null;
      this.expectedModCount = this.parent.modCount;
    }
  }
  
  static class ReferenceEntrySetIterator<K, V> extends ReferenceBaseIterator<K, V> implements Iterator<Map.Entry<K, V>> {
    public ReferenceEntrySetIterator(AbstractReferenceMap<K, V> parent) {
      super(parent);
    }
    
    public Map.Entry<K, V> next() {
      return nextEntry();
    }
  }
  
  static class ReferenceKeySetIterator<K> extends ReferenceBaseIterator<K, Object> implements Iterator<K> {
    ReferenceKeySetIterator(AbstractReferenceMap<K, ?> parent) {
      super((AbstractReferenceMap)parent);
    }
    
    public K next() {
      return nextEntry().getKey();
    }
  }
  
  static class ReferenceValuesIterator<V> extends ReferenceBaseIterator<Object, V> implements Iterator<V> {
    ReferenceValuesIterator(AbstractReferenceMap<?, V> parent) {
      super((AbstractReferenceMap)parent);
    }
    
    public V next() {
      return nextEntry().getValue();
    }
  }
  
  static class ReferenceMapIterator<K, V> extends ReferenceBaseIterator<K, V> implements MapIterator<K, V> {
    protected ReferenceMapIterator(AbstractReferenceMap<K, V> parent) {
      super(parent);
    }
    
    public K next() {
      return nextEntry().getKey();
    }
    
    public K getKey() {
      AbstractHashedMap.HashEntry<K, V> current = currentEntry();
      if (current == null)
        throw new IllegalStateException("getKey() can only be called after next() and before remove()"); 
      return current.getKey();
    }
    
    public V getValue() {
      AbstractHashedMap.HashEntry<K, V> current = currentEntry();
      if (current == null)
        throw new IllegalStateException("getValue() can only be called after next() and before remove()"); 
      return current.getValue();
    }
    
    public V setValue(V value) {
      AbstractHashedMap.HashEntry<K, V> current = currentEntry();
      if (current == null)
        throw new IllegalStateException("setValue() can only be called after next() and before remove()"); 
      return current.setValue(value);
    }
  }
  
  static class SoftRef<T> extends SoftReference<T> {
    private final int hash;
    
    public SoftRef(int hash, T r, ReferenceQueue<? super T> q) {
      super(r, q);
      this.hash = hash;
    }
    
    public int hashCode() {
      return this.hash;
    }
  }
  
  static class WeakRef<T> extends WeakReference<T> {
    private final int hash;
    
    public WeakRef(int hash, T r, ReferenceQueue<? super T> q) {
      super(r, q);
      this.hash = hash;
    }
    
    public int hashCode() {
      return this.hash;
    }
  }
  
  protected void doWriteObject(ObjectOutputStream out) throws IOException {
    out.writeInt(this.keyType.value);
    out.writeInt(this.valueType.value);
    out.writeBoolean(this.purgeValues);
    out.writeFloat(this.loadFactor);
    out.writeInt(this.data.length);
    for (MapIterator<K, V> it = mapIterator(); it.hasNext(); ) {
      out.writeObject(it.next());
      out.writeObject(it.getValue());
    } 
    out.writeObject(null);
  }
  
  protected void doReadObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.keyType = ReferenceStrength.resolve(in.readInt());
    this.valueType = ReferenceStrength.resolve(in.readInt());
    this.purgeValues = in.readBoolean();
    this.loadFactor = in.readFloat();
    int capacity = in.readInt();
    init();
    this.data = (AbstractHashedMap.HashEntry<K, V>[])new AbstractHashedMap.HashEntry[capacity];
    while (true) {
      K key = (K)in.readObject();
      if (key == null)
        break; 
      V value = (V)in.readObject();
      put(key, value);
    } 
    this.threshold = calculateThreshold(this.data.length, this.loadFactor);
  }
  
  protected boolean isKeyType(ReferenceStrength type) {
    return (this.keyType == type);
  }
}

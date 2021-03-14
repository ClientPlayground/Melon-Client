package org.apache.commons.collections4.bidimap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.collection.AbstractCollectionDecorator;
import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.keyvalue.AbstractMapEntryDecorator;

public abstract class AbstractDualBidiMap<K, V> implements BidiMap<K, V> {
  transient Map<K, V> normalMap;
  
  transient Map<V, K> reverseMap;
  
  transient BidiMap<V, K> inverseBidiMap = null;
  
  transient Set<K> keySet = null;
  
  transient Set<V> values = null;
  
  transient Set<Map.Entry<K, V>> entrySet = null;
  
  protected AbstractDualBidiMap(Map<K, V> normalMap, Map<V, K> reverseMap) {
    this.normalMap = normalMap;
    this.reverseMap = reverseMap;
  }
  
  protected AbstractDualBidiMap(Map<K, V> normalMap, Map<V, K> reverseMap, BidiMap<V, K> inverseBidiMap) {
    this.normalMap = normalMap;
    this.reverseMap = reverseMap;
    this.inverseBidiMap = inverseBidiMap;
  }
  
  public V get(Object key) {
    return this.normalMap.get(key);
  }
  
  public int size() {
    return this.normalMap.size();
  }
  
  public boolean isEmpty() {
    return this.normalMap.isEmpty();
  }
  
  public boolean containsKey(Object key) {
    return this.normalMap.containsKey(key);
  }
  
  public boolean equals(Object obj) {
    return this.normalMap.equals(obj);
  }
  
  public int hashCode() {
    return this.normalMap.hashCode();
  }
  
  public String toString() {
    return this.normalMap.toString();
  }
  
  public V put(K key, V value) {
    if (this.normalMap.containsKey(key))
      this.reverseMap.remove(this.normalMap.get(key)); 
    if (this.reverseMap.containsKey(value))
      this.normalMap.remove(this.reverseMap.get(value)); 
    V obj = this.normalMap.put(key, value);
    this.reverseMap.put(value, key);
    return obj;
  }
  
  public void putAll(Map<? extends K, ? extends V> map) {
    for (Map.Entry<? extends K, ? extends V> entry : map.entrySet())
      put(entry.getKey(), entry.getValue()); 
  }
  
  public V remove(Object key) {
    V value = null;
    if (this.normalMap.containsKey(key)) {
      value = this.normalMap.remove(key);
      this.reverseMap.remove(value);
    } 
    return value;
  }
  
  public void clear() {
    this.normalMap.clear();
    this.reverseMap.clear();
  }
  
  public boolean containsValue(Object value) {
    return this.reverseMap.containsKey(value);
  }
  
  public MapIterator<K, V> mapIterator() {
    return new BidiMapIterator<K, V>(this);
  }
  
  public K getKey(Object value) {
    return this.reverseMap.get(value);
  }
  
  public K removeValue(Object value) {
    K key = null;
    if (this.reverseMap.containsKey(value)) {
      key = this.reverseMap.remove(value);
      this.normalMap.remove(key);
    } 
    return key;
  }
  
  public BidiMap<V, K> inverseBidiMap() {
    if (this.inverseBidiMap == null)
      this.inverseBidiMap = createBidiMap(this.reverseMap, this.normalMap, this); 
    return this.inverseBidiMap;
  }
  
  public Set<K> keySet() {
    if (this.keySet == null)
      this.keySet = new KeySet<K>(this); 
    return this.keySet;
  }
  
  protected Iterator<K> createKeySetIterator(Iterator<K> iterator) {
    return (Iterator<K>)new KeySetIterator<K>(iterator, this);
  }
  
  public Set<V> values() {
    if (this.values == null)
      this.values = new Values<V>(this); 
    return this.values;
  }
  
  protected Iterator<V> createValuesIterator(Iterator<V> iterator) {
    return (Iterator<V>)new ValuesIterator<V>(iterator, this);
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    if (this.entrySet == null)
      this.entrySet = new EntrySet<K, V>(this); 
    return this.entrySet;
  }
  
  protected Iterator<Map.Entry<K, V>> createEntrySetIterator(Iterator<Map.Entry<K, V>> iterator) {
    return (Iterator)new EntrySetIterator<K, V>(iterator, this);
  }
  
  protected AbstractDualBidiMap() {}
  
  protected abstract BidiMap<V, K> createBidiMap(Map<V, K> paramMap, Map<K, V> paramMap1, BidiMap<K, V> paramBidiMap);
  
  protected static abstract class View<K, V, E> extends AbstractCollectionDecorator<E> {
    private static final long serialVersionUID = 4621510560119690639L;
    
    protected final AbstractDualBidiMap<K, V> parent;
    
    protected View(Collection<E> coll, AbstractDualBidiMap<K, V> parent) {
      super(coll);
      this.parent = parent;
    }
    
    public boolean removeAll(Collection<?> coll) {
      if (this.parent.isEmpty() || coll.isEmpty())
        return false; 
      boolean modified = false;
      Iterator<?> it = coll.iterator();
      while (it.hasNext())
        modified |= remove(it.next()); 
      return modified;
    }
    
    public boolean retainAll(Collection<?> coll) {
      if (this.parent.isEmpty())
        return false; 
      if (coll.isEmpty()) {
        this.parent.clear();
        return true;
      } 
      boolean modified = false;
      Iterator<E> it = iterator();
      while (it.hasNext()) {
        if (!coll.contains(it.next())) {
          it.remove();
          modified = true;
        } 
      } 
      return modified;
    }
    
    public void clear() {
      this.parent.clear();
    }
  }
  
  protected static class KeySet<K> extends View<K, Object, K> implements Set<K> {
    private static final long serialVersionUID = -7107935777385040694L;
    
    protected KeySet(AbstractDualBidiMap<K, ?> parent) {
      super(parent.normalMap.keySet(), (AbstractDualBidiMap)parent);
    }
    
    public Iterator<K> iterator() {
      return this.parent.createKeySetIterator(super.iterator());
    }
    
    public boolean contains(Object key) {
      return this.parent.normalMap.containsKey(key);
    }
    
    public boolean remove(Object key) {
      if (this.parent.normalMap.containsKey(key)) {
        Object value = this.parent.normalMap.remove(key);
        this.parent.reverseMap.remove(value);
        return true;
      } 
      return false;
    }
  }
  
  protected static class KeySetIterator<K> extends AbstractIteratorDecorator<K> {
    protected final AbstractDualBidiMap<K, ?> parent;
    
    protected K lastKey = null;
    
    protected boolean canRemove = false;
    
    protected KeySetIterator(Iterator<K> iterator, AbstractDualBidiMap<K, ?> parent) {
      super(iterator);
      this.parent = parent;
    }
    
    public K next() {
      this.lastKey = (K)super.next();
      this.canRemove = true;
      return this.lastKey;
    }
    
    public void remove() {
      if (!this.canRemove)
        throw new IllegalStateException("Iterator remove() can only be called once after next()"); 
      Object value = this.parent.normalMap.get(this.lastKey);
      super.remove();
      this.parent.reverseMap.remove(value);
      this.lastKey = null;
      this.canRemove = false;
    }
  }
  
  protected static class Values<V> extends View<Object, V, V> implements Set<V> {
    private static final long serialVersionUID = 4023777119829639864L;
    
    protected Values(AbstractDualBidiMap<?, V> parent) {
      super(parent.normalMap.values(), (AbstractDualBidiMap)parent);
    }
    
    public Iterator<V> iterator() {
      return this.parent.createValuesIterator(super.iterator());
    }
    
    public boolean contains(Object value) {
      return this.parent.reverseMap.containsKey(value);
    }
    
    public boolean remove(Object value) {
      if (this.parent.reverseMap.containsKey(value)) {
        Object key = this.parent.reverseMap.remove(value);
        this.parent.normalMap.remove(key);
        return true;
      } 
      return false;
    }
  }
  
  protected static class ValuesIterator<V> extends AbstractIteratorDecorator<V> {
    protected final AbstractDualBidiMap<Object, V> parent;
    
    protected V lastValue = null;
    
    protected boolean canRemove = false;
    
    protected ValuesIterator(Iterator<V> iterator, AbstractDualBidiMap<?, V> parent) {
      super(iterator);
      this.parent = (AbstractDualBidiMap)parent;
    }
    
    public V next() {
      this.lastValue = (V)super.next();
      this.canRemove = true;
      return this.lastValue;
    }
    
    public void remove() {
      if (!this.canRemove)
        throw new IllegalStateException("Iterator remove() can only be called once after next()"); 
      super.remove();
      this.parent.reverseMap.remove(this.lastValue);
      this.lastValue = null;
      this.canRemove = false;
    }
  }
  
  protected static class EntrySet<K, V> extends View<K, V, Map.Entry<K, V>> implements Set<Map.Entry<K, V>> {
    private static final long serialVersionUID = 4040410962603292348L;
    
    protected EntrySet(AbstractDualBidiMap<K, V> parent) {
      super(parent.normalMap.entrySet(), parent);
    }
    
    public Iterator<Map.Entry<K, V>> iterator() {
      return this.parent.createEntrySetIterator(super.iterator());
    }
    
    public boolean remove(Object obj) {
      if (!(obj instanceof Map.Entry))
        return false; 
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
      Object key = entry.getKey();
      if (this.parent.containsKey(key)) {
        V value = this.parent.normalMap.get(key);
        if ((value == null) ? (entry.getValue() == null) : value.equals(entry.getValue())) {
          this.parent.normalMap.remove(key);
          this.parent.reverseMap.remove(value);
          return true;
        } 
      } 
      return false;
    }
  }
  
  protected static class EntrySetIterator<K, V> extends AbstractIteratorDecorator<Map.Entry<K, V>> {
    protected final AbstractDualBidiMap<K, V> parent;
    
    protected Map.Entry<K, V> last = null;
    
    protected boolean canRemove = false;
    
    protected EntrySetIterator(Iterator<Map.Entry<K, V>> iterator, AbstractDualBidiMap<K, V> parent) {
      super(iterator);
      this.parent = parent;
    }
    
    public Map.Entry<K, V> next() {
      this.last = (Map.Entry<K, V>)new AbstractDualBidiMap.MapEntry<K, V>((Map.Entry<K, V>)super.next(), this.parent);
      this.canRemove = true;
      return this.last;
    }
    
    public void remove() {
      if (!this.canRemove)
        throw new IllegalStateException("Iterator remove() can only be called once after next()"); 
      Object value = this.last.getValue();
      super.remove();
      this.parent.reverseMap.remove(value);
      this.last = null;
      this.canRemove = false;
    }
  }
  
  protected static class MapEntry<K, V> extends AbstractMapEntryDecorator<K, V> {
    protected final AbstractDualBidiMap<K, V> parent;
    
    protected MapEntry(Map.Entry<K, V> entry, AbstractDualBidiMap<K, V> parent) {
      super(entry);
      this.parent = parent;
    }
    
    public V setValue(V value) {
      K key = (K)getKey();
      if (this.parent.reverseMap.containsKey(value) && this.parent.reverseMap.get(value) != key)
        throw new IllegalArgumentException("Cannot use setValue() when the object being set is already in the map"); 
      this.parent.put(key, value);
      return (V)super.setValue(value);
    }
  }
  
  protected static class BidiMapIterator<K, V> implements MapIterator<K, V>, ResettableIterator<K> {
    protected final AbstractDualBidiMap<K, V> parent;
    
    protected Iterator<Map.Entry<K, V>> iterator;
    
    protected Map.Entry<K, V> last = null;
    
    protected boolean canRemove = false;
    
    protected BidiMapIterator(AbstractDualBidiMap<K, V> parent) {
      this.parent = parent;
      this.iterator = parent.normalMap.entrySet().iterator();
    }
    
    public boolean hasNext() {
      return this.iterator.hasNext();
    }
    
    public K next() {
      this.last = this.iterator.next();
      this.canRemove = true;
      return this.last.getKey();
    }
    
    public void remove() {
      if (!this.canRemove)
        throw new IllegalStateException("Iterator remove() can only be called once after next()"); 
      V value = this.last.getValue();
      this.iterator.remove();
      this.parent.reverseMap.remove(value);
      this.last = null;
      this.canRemove = false;
    }
    
    public K getKey() {
      if (this.last == null)
        throw new IllegalStateException("Iterator getKey() can only be called after next() and before remove()"); 
      return this.last.getKey();
    }
    
    public V getValue() {
      if (this.last == null)
        throw new IllegalStateException("Iterator getValue() can only be called after next() and before remove()"); 
      return this.last.getValue();
    }
    
    public V setValue(V value) {
      if (this.last == null)
        throw new IllegalStateException("Iterator setValue() can only be called after next() and before remove()"); 
      if (this.parent.reverseMap.containsKey(value) && this.parent.reverseMap.get(value) != this.last.getKey())
        throw new IllegalArgumentException("Cannot use setValue() when the object being set is already in the map"); 
      return this.parent.put(this.last.getKey(), value);
    }
    
    public void reset() {
      this.iterator = this.parent.normalMap.entrySet().iterator();
      this.last = null;
      this.canRemove = false;
    }
    
    public String toString() {
      if (this.last != null)
        return "MapIterator[" + getKey() + "=" + getValue() + "]"; 
      return "MapIterator[]";
    }
  }
}

package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.AbstractUntypedIteratorDecorator;
import org.apache.commons.collections4.keyvalue.AbstractMapEntry;
import org.apache.commons.collections4.list.UnmodifiableList;

public class ListOrderedMap<K, V> extends AbstractMapDecorator<K, V> implements OrderedMap<K, V>, Serializable {
  private static final long serialVersionUID = 2728177751851003750L;
  
  private final List<K> insertOrder = new ArrayList<K>();
  
  public static <K, V> ListOrderedMap<K, V> listOrderedMap(Map<K, V> map) {
    return new ListOrderedMap<K, V>(map);
  }
  
  public ListOrderedMap() {
    this(new HashMap<K, V>());
  }
  
  protected ListOrderedMap(Map<K, V> map) {
    super(map);
    this.insertOrder.addAll(decorated().keySet());
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
    return new ListOrderedMapIterator<K, V>(this);
  }
  
  public K firstKey() {
    if (size() == 0)
      throw new NoSuchElementException("Map is empty"); 
    return this.insertOrder.get(0);
  }
  
  public K lastKey() {
    if (size() == 0)
      throw new NoSuchElementException("Map is empty"); 
    return this.insertOrder.get(size() - 1);
  }
  
  public K nextKey(Object key) {
    int index = this.insertOrder.indexOf(key);
    if (index >= 0 && index < size() - 1)
      return this.insertOrder.get(index + 1); 
    return null;
  }
  
  public K previousKey(Object key) {
    int index = this.insertOrder.indexOf(key);
    if (index > 0)
      return this.insertOrder.get(index - 1); 
    return null;
  }
  
  public V put(K key, V value) {
    if (decorated().containsKey(key))
      return decorated().put(key, value); 
    V result = decorated().put(key, value);
    this.insertOrder.add(key);
    return result;
  }
  
  public void putAll(Map<? extends K, ? extends V> map) {
    for (Map.Entry<? extends K, ? extends V> entry : map.entrySet())
      put(entry.getKey(), entry.getValue()); 
  }
  
  public void putAll(int index, Map<? extends K, ? extends V> map) {
    if (index < 0 || index > this.insertOrder.size())
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.insertOrder.size()); 
    for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
      K key = entry.getKey();
      boolean contains = containsKey(key);
      put(index, entry.getKey(), entry.getValue());
      if (!contains) {
        index++;
        continue;
      } 
      index = indexOf(entry.getKey()) + 1;
    } 
  }
  
  public V remove(Object key) {
    V result = null;
    if (decorated().containsKey(key)) {
      result = decorated().remove(key);
      this.insertOrder.remove(key);
    } 
    return result;
  }
  
  public void clear() {
    decorated().clear();
    this.insertOrder.clear();
  }
  
  public Set<K> keySet() {
    return new KeySetView<K>(this);
  }
  
  public List<K> keyList() {
    return UnmodifiableList.unmodifiableList(this.insertOrder);
  }
  
  public Collection<V> values() {
    return new ValuesView<V>(this);
  }
  
  public List<V> valueList() {
    return new ValuesView<V>(this);
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    return new EntrySetView<K, V>(this, this.insertOrder);
  }
  
  public String toString() {
    if (isEmpty())
      return "{}"; 
    StringBuilder buf = new StringBuilder();
    buf.append('{');
    boolean first = true;
    for (Map.Entry<K, V> entry : entrySet()) {
      K key = entry.getKey();
      V value = entry.getValue();
      if (first) {
        first = false;
      } else {
        buf.append(", ");
      } 
      buf.append((key == this) ? "(this Map)" : key);
      buf.append('=');
      buf.append((value == this) ? "(this Map)" : value);
    } 
    buf.append('}');
    return buf.toString();
  }
  
  public K get(int index) {
    return this.insertOrder.get(index);
  }
  
  public V getValue(int index) {
    return get(this.insertOrder.get(index));
  }
  
  public int indexOf(Object key) {
    return this.insertOrder.indexOf(key);
  }
  
  public V setValue(int index, V value) {
    K key = this.insertOrder.get(index);
    return put(key, value);
  }
  
  public V put(int index, K key, V value) {
    if (index < 0 || index > this.insertOrder.size())
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.insertOrder.size()); 
    Map<K, V> m = decorated();
    if (m.containsKey(key)) {
      V result = m.remove(key);
      int pos = this.insertOrder.indexOf(key);
      this.insertOrder.remove(pos);
      if (pos < index)
        index--; 
      this.insertOrder.add(index, key);
      m.put(key, value);
      return result;
    } 
    this.insertOrder.add(index, key);
    m.put(key, value);
    return null;
  }
  
  public V remove(int index) {
    return remove(get(index));
  }
  
  public List<K> asList() {
    return keyList();
  }
  
  static class ValuesView<V> extends AbstractList<V> {
    private final ListOrderedMap<Object, V> parent;
    
    ValuesView(ListOrderedMap<?, V> parent) {
      this.parent = (ListOrderedMap)parent;
    }
    
    public int size() {
      return this.parent.size();
    }
    
    public boolean contains(Object value) {
      return this.parent.containsValue(value);
    }
    
    public void clear() {
      this.parent.clear();
    }
    
    public Iterator<V> iterator() {
      return (Iterator)new AbstractUntypedIteratorDecorator<Map.Entry<Object, V>, V>(this.parent.entrySet().iterator()) {
          public V next() {
            return (V)((Map.Entry)getIterator().next()).getValue();
          }
        };
    }
    
    public V get(int index) {
      return this.parent.getValue(index);
    }
    
    public V set(int index, V value) {
      return this.parent.setValue(index, value);
    }
    
    public V remove(int index) {
      return this.parent.remove(index);
    }
  }
  
  static class KeySetView<K> extends AbstractSet<K> {
    private final ListOrderedMap<K, Object> parent;
    
    KeySetView(ListOrderedMap<K, ?> parent) {
      this.parent = (ListOrderedMap)parent;
    }
    
    public int size() {
      return this.parent.size();
    }
    
    public boolean contains(Object value) {
      return this.parent.containsKey(value);
    }
    
    public void clear() {
      this.parent.clear();
    }
    
    public Iterator<K> iterator() {
      return (Iterator)new AbstractUntypedIteratorDecorator<Map.Entry<K, Object>, K>(this.parent.entrySet().iterator()) {
          public K next() {
            return (K)((Map.Entry)getIterator().next()).getKey();
          }
        };
    }
  }
  
  static class EntrySetView<K, V> extends AbstractSet<Map.Entry<K, V>> {
    private final ListOrderedMap<K, V> parent;
    
    private final List<K> insertOrder;
    
    private Set<Map.Entry<K, V>> entrySet;
    
    public EntrySetView(ListOrderedMap<K, V> parent, List<K> insertOrder) {
      this.parent = parent;
      this.insertOrder = insertOrder;
    }
    
    private Set<Map.Entry<K, V>> getEntrySet() {
      if (this.entrySet == null)
        this.entrySet = this.parent.decorated().entrySet(); 
      return this.entrySet;
    }
    
    public int size() {
      return this.parent.size();
    }
    
    public boolean isEmpty() {
      return this.parent.isEmpty();
    }
    
    public boolean contains(Object obj) {
      return getEntrySet().contains(obj);
    }
    
    public boolean containsAll(Collection<?> coll) {
      return getEntrySet().containsAll(coll);
    }
    
    public boolean remove(Object obj) {
      if (!(obj instanceof Map.Entry))
        return false; 
      if (getEntrySet().contains(obj)) {
        Object key = ((Map.Entry)obj).getKey();
        this.parent.remove(key);
        return true;
      } 
      return false;
    }
    
    public void clear() {
      this.parent.clear();
    }
    
    public boolean equals(Object obj) {
      if (obj == this)
        return true; 
      return getEntrySet().equals(obj);
    }
    
    public int hashCode() {
      return getEntrySet().hashCode();
    }
    
    public String toString() {
      return getEntrySet().toString();
    }
    
    public Iterator<Map.Entry<K, V>> iterator() {
      return (Iterator)new ListOrderedMap.ListOrderedIterator<K, V>(this.parent, this.insertOrder);
    }
  }
  
  static class ListOrderedIterator<K, V> extends AbstractUntypedIteratorDecorator<K, Map.Entry<K, V>> {
    private final ListOrderedMap<K, V> parent;
    
    private K last = null;
    
    ListOrderedIterator(ListOrderedMap<K, V> parent, List<K> insertOrder) {
      super(insertOrder.iterator());
      this.parent = parent;
    }
    
    public Map.Entry<K, V> next() {
      this.last = getIterator().next();
      return (Map.Entry<K, V>)new ListOrderedMap.ListOrderedMapEntry<K, V>(this.parent, this.last);
    }
    
    public void remove() {
      super.remove();
      this.parent.decorated().remove(this.last);
    }
  }
  
  static class ListOrderedMapEntry<K, V> extends AbstractMapEntry<K, V> {
    private final ListOrderedMap<K, V> parent;
    
    ListOrderedMapEntry(ListOrderedMap<K, V> parent, K key) {
      super(key, null);
      this.parent = parent;
    }
    
    public V getValue() {
      return this.parent.get(getKey());
    }
    
    public V setValue(V value) {
      return this.parent.decorated().put(getKey(), value);
    }
  }
  
  static class ListOrderedMapIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {
    private final ListOrderedMap<K, V> parent;
    
    private ListIterator<K> iterator;
    
    private K last = null;
    
    private boolean readable = false;
    
    ListOrderedMapIterator(ListOrderedMap<K, V> parent) {
      this.parent = parent;
      this.iterator = parent.insertOrder.listIterator();
    }
    
    public boolean hasNext() {
      return this.iterator.hasNext();
    }
    
    public K next() {
      this.last = this.iterator.next();
      this.readable = true;
      return this.last;
    }
    
    public boolean hasPrevious() {
      return this.iterator.hasPrevious();
    }
    
    public K previous() {
      this.last = this.iterator.previous();
      this.readable = true;
      return this.last;
    }
    
    public void remove() {
      if (!this.readable)
        throw new IllegalStateException("remove() can only be called once after next()"); 
      this.iterator.remove();
      this.parent.map.remove(this.last);
      this.readable = false;
    }
    
    public K getKey() {
      if (!this.readable)
        throw new IllegalStateException("getKey() can only be called after next() and before remove()"); 
      return this.last;
    }
    
    public V getValue() {
      if (!this.readable)
        throw new IllegalStateException("getValue() can only be called after next() and before remove()"); 
      return this.parent.get(this.last);
    }
    
    public V setValue(V value) {
      if (!this.readable)
        throw new IllegalStateException("setValue() can only be called after next() and before remove()"); 
      return this.parent.map.put(this.last, value);
    }
    
    public void reset() {
      this.iterator = this.parent.insertOrder.listIterator();
      this.last = null;
      this.readable = false;
    }
    
    public String toString() {
      if (this.readable == true)
        return "Iterator[" + getKey() + "=" + getValue() + "]"; 
      return "Iterator[]";
    }
  }
}

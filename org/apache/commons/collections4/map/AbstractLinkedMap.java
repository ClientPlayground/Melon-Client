package org.apache.commons.collections4.map;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedIterator;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.EmptyOrderedIterator;
import org.apache.commons.collections4.iterators.EmptyOrderedMapIterator;

public abstract class AbstractLinkedMap<K, V> extends AbstractHashedMap<K, V> implements OrderedMap<K, V> {
  transient LinkEntry<K, V> header;
  
  protected AbstractLinkedMap() {}
  
  protected AbstractLinkedMap(int initialCapacity, float loadFactor, int threshold) {
    super(initialCapacity, loadFactor, threshold);
  }
  
  protected AbstractLinkedMap(int initialCapacity) {
    super(initialCapacity);
  }
  
  protected AbstractLinkedMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }
  
  protected AbstractLinkedMap(Map<? extends K, ? extends V> map) {
    super(map);
  }
  
  protected void init() {
    this.header = createEntry((AbstractHashedMap.HashEntry<K, V>)null, -1, (K)null, (V)null);
    this.header.before = this.header.after = this.header;
  }
  
  public boolean containsValue(Object value) {
    if (value == null) {
      for (LinkEntry<K, V> entry = this.header.after; entry != this.header; entry = entry.after) {
        if (entry.getValue() == null)
          return true; 
      } 
    } else {
      for (LinkEntry<K, V> entry = this.header.after; entry != this.header; entry = entry.after) {
        if (isEqualValue(value, entry.getValue()))
          return true; 
      } 
    } 
    return false;
  }
  
  public void clear() {
    super.clear();
    this.header.before = this.header.after = this.header;
  }
  
  public K firstKey() {
    if (this.size == 0)
      throw new NoSuchElementException("Map is empty"); 
    return this.header.after.getKey();
  }
  
  public K lastKey() {
    if (this.size == 0)
      throw new NoSuchElementException("Map is empty"); 
    return this.header.before.getKey();
  }
  
  public K nextKey(Object key) {
    LinkEntry<K, V> entry = getEntry(key);
    return (entry == null || entry.after == this.header) ? null : entry.after.getKey();
  }
  
  protected LinkEntry<K, V> getEntry(Object key) {
    return (LinkEntry<K, V>)super.getEntry(key);
  }
  
  public K previousKey(Object key) {
    LinkEntry<K, V> entry = getEntry(key);
    return (entry == null || entry.before == this.header) ? null : entry.before.getKey();
  }
  
  protected LinkEntry<K, V> getEntry(int index) {
    LinkEntry<K, V> entry;
    if (index < 0)
      throw new IndexOutOfBoundsException("Index " + index + " is less than zero"); 
    if (index >= this.size)
      throw new IndexOutOfBoundsException("Index " + index + " is invalid for size " + this.size); 
    if (index < this.size / 2) {
      entry = this.header.after;
      for (int currentIndex = 0; currentIndex < index; currentIndex++)
        entry = entry.after; 
    } else {
      entry = this.header;
      for (int currentIndex = this.size; currentIndex > index; currentIndex--)
        entry = entry.before; 
    } 
    return entry;
  }
  
  protected void addEntry(AbstractHashedMap.HashEntry<K, V> entry, int hashIndex) {
    LinkEntry<K, V> link = (LinkEntry<K, V>)entry;
    link.after = this.header;
    link.before = this.header.before;
    this.header.before.after = link;
    this.header.before = link;
    this.data[hashIndex] = link;
  }
  
  protected LinkEntry<K, V> createEntry(AbstractHashedMap.HashEntry<K, V> next, int hashCode, K key, V value) {
    return new LinkEntry<K, V>(next, hashCode, convertKey(key), value);
  }
  
  protected void removeEntry(AbstractHashedMap.HashEntry<K, V> entry, int hashIndex, AbstractHashedMap.HashEntry<K, V> previous) {
    LinkEntry<K, V> link = (LinkEntry<K, V>)entry;
    link.before.after = link.after;
    link.after.before = link.before;
    link.after = null;
    link.before = null;
    super.removeEntry(entry, hashIndex, previous);
  }
  
  protected LinkEntry<K, V> entryBefore(LinkEntry<K, V> entry) {
    return entry.before;
  }
  
  protected LinkEntry<K, V> entryAfter(LinkEntry<K, V> entry) {
    return entry.after;
  }
  
  public OrderedMapIterator<K, V> mapIterator() {
    if (this.size == 0)
      return EmptyOrderedMapIterator.emptyOrderedMapIterator(); 
    return new LinkMapIterator<K, V>(this);
  }
  
  protected static class LinkMapIterator<K, V> extends LinkIterator<K, V> implements OrderedMapIterator<K, V>, ResettableIterator<K> {
    protected LinkMapIterator(AbstractLinkedMap<K, V> parent) {
      super(parent);
    }
    
    public K next() {
      return nextEntry().getKey();
    }
    
    public K previous() {
      return previousEntry().getKey();
    }
    
    public K getKey() {
      AbstractLinkedMap.LinkEntry<K, V> current = currentEntry();
      if (current == null)
        throw new IllegalStateException("getKey() can only be called after next() and before remove()"); 
      return current.getKey();
    }
    
    public V getValue() {
      AbstractLinkedMap.LinkEntry<K, V> current = currentEntry();
      if (current == null)
        throw new IllegalStateException("getValue() can only be called after next() and before remove()"); 
      return current.getValue();
    }
    
    public V setValue(V value) {
      AbstractLinkedMap.LinkEntry<K, V> current = currentEntry();
      if (current == null)
        throw new IllegalStateException("setValue() can only be called after next() and before remove()"); 
      return current.setValue(value);
    }
  }
  
  protected Iterator<Map.Entry<K, V>> createEntrySetIterator() {
    if (size() == 0)
      return (Iterator<Map.Entry<K, V>>)EmptyOrderedIterator.emptyOrderedIterator(); 
    return (Iterator)new EntrySetIterator<K, V>(this);
  }
  
  protected static class EntrySetIterator<K, V> extends LinkIterator<K, V> implements OrderedIterator<Map.Entry<K, V>>, ResettableIterator<Map.Entry<K, V>> {
    protected EntrySetIterator(AbstractLinkedMap<K, V> parent) {
      super(parent);
    }
    
    public Map.Entry<K, V> next() {
      return nextEntry();
    }
    
    public Map.Entry<K, V> previous() {
      return previousEntry();
    }
  }
  
  protected Iterator<K> createKeySetIterator() {
    if (size() == 0)
      return (Iterator<K>)EmptyOrderedIterator.emptyOrderedIterator(); 
    return (Iterator<K>)new KeySetIterator<K>(this);
  }
  
  protected static class KeySetIterator<K> extends LinkIterator<K, Object> implements OrderedIterator<K>, ResettableIterator<K> {
    protected KeySetIterator(AbstractLinkedMap<K, ?> parent) {
      super((AbstractLinkedMap)parent);
    }
    
    public K next() {
      return nextEntry().getKey();
    }
    
    public K previous() {
      return previousEntry().getKey();
    }
  }
  
  protected Iterator<V> createValuesIterator() {
    if (size() == 0)
      return (Iterator<V>)EmptyOrderedIterator.emptyOrderedIterator(); 
    return (Iterator<V>)new ValuesIterator<V>(this);
  }
  
  protected static class ValuesIterator<V> extends LinkIterator<Object, V> implements OrderedIterator<V>, ResettableIterator<V> {
    protected ValuesIterator(AbstractLinkedMap<?, V> parent) {
      super((AbstractLinkedMap)parent);
    }
    
    public V next() {
      return nextEntry().getValue();
    }
    
    public V previous() {
      return previousEntry().getValue();
    }
  }
  
  protected static class LinkEntry<K, V> extends AbstractHashedMap.HashEntry<K, V> {
    protected LinkEntry<K, V> before;
    
    protected LinkEntry<K, V> after;
    
    protected LinkEntry(AbstractHashedMap.HashEntry<K, V> next, int hashCode, Object key, V value) {
      super(next, hashCode, key, value);
    }
  }
  
  protected static abstract class LinkIterator<K, V> {
    protected final AbstractLinkedMap<K, V> parent;
    
    protected AbstractLinkedMap.LinkEntry<K, V> last;
    
    protected AbstractLinkedMap.LinkEntry<K, V> next;
    
    protected int expectedModCount;
    
    protected LinkIterator(AbstractLinkedMap<K, V> parent) {
      this.parent = parent;
      this.next = parent.header.after;
      this.expectedModCount = parent.modCount;
    }
    
    public boolean hasNext() {
      return (this.next != this.parent.header);
    }
    
    public boolean hasPrevious() {
      return (this.next.before != this.parent.header);
    }
    
    protected AbstractLinkedMap.LinkEntry<K, V> nextEntry() {
      if (this.parent.modCount != this.expectedModCount)
        throw new ConcurrentModificationException(); 
      if (this.next == this.parent.header)
        throw new NoSuchElementException("No next() entry in the iteration"); 
      this.last = this.next;
      this.next = this.next.after;
      return this.last;
    }
    
    protected AbstractLinkedMap.LinkEntry<K, V> previousEntry() {
      if (this.parent.modCount != this.expectedModCount)
        throw new ConcurrentModificationException(); 
      AbstractLinkedMap.LinkEntry<K, V> previous = this.next.before;
      if (previous == this.parent.header)
        throw new NoSuchElementException("No previous() entry in the iteration"); 
      this.next = previous;
      this.last = previous;
      return this.last;
    }
    
    protected AbstractLinkedMap.LinkEntry<K, V> currentEntry() {
      return this.last;
    }
    
    public void remove() {
      if (this.last == null)
        throw new IllegalStateException("remove() can only be called once after next()"); 
      if (this.parent.modCount != this.expectedModCount)
        throw new ConcurrentModificationException(); 
      this.parent.remove(this.last.getKey());
      this.last = null;
      this.expectedModCount = this.parent.modCount;
    }
    
    public void reset() {
      this.last = null;
      this.next = this.parent.header.after;
    }
    
    public String toString() {
      if (this.last != null)
        return "Iterator[" + this.last.getKey() + "=" + this.last.getValue() + "]"; 
      return "Iterator[]";
    }
  }
}

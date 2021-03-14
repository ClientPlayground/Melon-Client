package org.apache.commons.collections4.trie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedMapIterator;

abstract class AbstractPatriciaTrie<K, V> extends AbstractBitwiseTrie<K, V> {
  private static final long serialVersionUID = 5155253417231339498L;
  
  private transient TrieEntry<K, V> root = new TrieEntry<K, V>(null, null, -1);
  
  private volatile transient Set<K> keySet;
  
  private volatile transient Collection<V> values;
  
  private volatile transient Set<Map.Entry<K, V>> entrySet;
  
  private transient int size = 0;
  
  protected transient int modCount = 0;
  
  protected AbstractPatriciaTrie(KeyAnalyzer<? super K> keyAnalyzer) {
    super(keyAnalyzer);
  }
  
  protected AbstractPatriciaTrie(KeyAnalyzer<? super K> keyAnalyzer, Map<? extends K, ? extends V> map) {
    super(keyAnalyzer);
    putAll(map);
  }
  
  public void clear() {
    this.root.key = null;
    this.root.bitIndex = -1;
    this.root.value = null;
    this.root.parent = null;
    this.root.left = this.root;
    this.root.right = null;
    this.root.predecessor = this.root;
    this.size = 0;
    incrementModCount();
  }
  
  public int size() {
    return this.size;
  }
  
  void incrementSize() {
    this.size++;
    incrementModCount();
  }
  
  void decrementSize() {
    this.size--;
    incrementModCount();
  }
  
  private void incrementModCount() {
    this.modCount++;
  }
  
  public V put(K key, V value) {
    if (key == null)
      throw new NullPointerException("Key cannot be null"); 
    int lengthInBits = lengthInBits(key);
    if (lengthInBits == 0) {
      if (this.root.isEmpty()) {
        incrementSize();
      } else {
        incrementModCount();
      } 
      return this.root.setKeyValue(key, value);
    } 
    TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
    if (compareKeys(key, found.key)) {
      if (found.isEmpty()) {
        incrementSize();
      } else {
        incrementModCount();
      } 
      return found.setKeyValue(key, value);
    } 
    int bitIndex = bitIndex(key, found.key);
    if (!KeyAnalyzer.isOutOfBoundsIndex(bitIndex)) {
      if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
        TrieEntry<K, V> t = new TrieEntry<K, V>(key, value, bitIndex);
        addEntry(t, lengthInBits);
        incrementSize();
        return null;
      } 
      if (KeyAnalyzer.isNullBitKey(bitIndex)) {
        if (this.root.isEmpty()) {
          incrementSize();
        } else {
          incrementModCount();
        } 
        return this.root.setKeyValue(key, value);
      } 
      if (KeyAnalyzer.isEqualBitKey(bitIndex))
        if (found != this.root) {
          incrementModCount();
          return found.setKeyValue(key, value);
        }  
    } 
    throw new IllegalArgumentException("Failed to put: " + key + " -> " + value + ", " + bitIndex);
  }
  
  TrieEntry<K, V> addEntry(TrieEntry<K, V> entry, int lengthInBits) {
    TrieEntry<K, V> current = this.root.left;
    TrieEntry<K, V> path = this.root;
    while (true) {
      if (current.bitIndex >= entry.bitIndex || current.bitIndex <= path.bitIndex) {
        entry.predecessor = entry;
        if (!isBitSet(entry.key, entry.bitIndex, lengthInBits)) {
          entry.left = entry;
          entry.right = current;
        } else {
          entry.left = current;
          entry.right = entry;
        } 
        entry.parent = path;
        if (current.bitIndex >= entry.bitIndex)
          current.parent = entry; 
        if (current.bitIndex <= path.bitIndex)
          current.predecessor = entry; 
        if (path == this.root || !isBitSet(entry.key, path.bitIndex, lengthInBits)) {
          path.left = entry;
        } else {
          path.right = entry;
        } 
        return entry;
      } 
      path = current;
      if (!isBitSet(entry.key, current.bitIndex, lengthInBits)) {
        current = current.left;
        continue;
      } 
      current = current.right;
    } 
  }
  
  public V get(Object k) {
    TrieEntry<K, V> entry = getEntry(k);
    return (entry != null) ? entry.getValue() : null;
  }
  
  TrieEntry<K, V> getEntry(Object k) {
    K key = castKey(k);
    if (key == null)
      return null; 
    int lengthInBits = lengthInBits(key);
    TrieEntry<K, V> entry = getNearestEntryForKey(key, lengthInBits);
    return (!entry.isEmpty() && compareKeys(key, entry.key)) ? entry : null;
  }
  
  public Map.Entry<K, V> select(K key) {
    int lengthInBits = lengthInBits(key);
    Reference<Map.Entry<K, V>> reference = new Reference<Map.Entry<K, V>>();
    if (!selectR(this.root.left, -1, key, lengthInBits, reference))
      return reference.get(); 
    return null;
  }
  
  public K selectKey(K key) {
    Map.Entry<K, V> entry = select(key);
    if (entry == null)
      return null; 
    return entry.getKey();
  }
  
  public V selectValue(K key) {
    Map.Entry<K, V> entry = select(key);
    if (entry == null)
      return null; 
    return entry.getValue();
  }
  
  private boolean selectR(TrieEntry<K, V> h, int bitIndex, K key, int lengthInBits, Reference<Map.Entry<K, V>> reference) {
    if (h.bitIndex <= bitIndex) {
      if (!h.isEmpty()) {
        reference.set(h);
        return false;
      } 
      return true;
    } 
    if (!isBitSet(key, h.bitIndex, lengthInBits)) {
      if (selectR(h.left, h.bitIndex, key, lengthInBits, reference))
        return selectR(h.right, h.bitIndex, key, lengthInBits, reference); 
    } else if (selectR(h.right, h.bitIndex, key, lengthInBits, reference)) {
      return selectR(h.left, h.bitIndex, key, lengthInBits, reference);
    } 
    return false;
  }
  
  public boolean containsKey(Object k) {
    if (k == null)
      return false; 
    K key = castKey(k);
    int lengthInBits = lengthInBits(key);
    TrieEntry<K, V> entry = getNearestEntryForKey(key, lengthInBits);
    return (!entry.isEmpty() && compareKeys(key, entry.key));
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    if (this.entrySet == null)
      this.entrySet = new EntrySet(); 
    return this.entrySet;
  }
  
  public Set<K> keySet() {
    if (this.keySet == null)
      this.keySet = new KeySet(); 
    return this.keySet;
  }
  
  public Collection<V> values() {
    if (this.values == null)
      this.values = new Values(); 
    return this.values;
  }
  
  public V remove(Object k) {
    if (k == null)
      return null; 
    K key = castKey(k);
    int lengthInBits = lengthInBits(key);
    TrieEntry<K, V> current = this.root.left;
    TrieEntry<K, V> path = this.root;
    while (true) {
      if (current.bitIndex <= path.bitIndex) {
        if (!current.isEmpty() && compareKeys(key, current.key))
          return removeEntry(current); 
        return null;
      } 
      path = current;
      if (!isBitSet(key, current.bitIndex, lengthInBits)) {
        current = current.left;
        continue;
      } 
      current = current.right;
    } 
  }
  
  TrieEntry<K, V> getNearestEntryForKey(K key, int lengthInBits) {
    TrieEntry<K, V> current = this.root.left;
    TrieEntry<K, V> path = this.root;
    while (true) {
      if (current.bitIndex <= path.bitIndex)
        return current; 
      path = current;
      if (!isBitSet(key, current.bitIndex, lengthInBits)) {
        current = current.left;
        continue;
      } 
      current = current.right;
    } 
  }
  
  V removeEntry(TrieEntry<K, V> h) {
    if (h != this.root)
      if (h.isInternalNode()) {
        removeInternalEntry(h);
      } else {
        removeExternalEntry(h);
      }  
    decrementSize();
    return h.setKeyValue(null, null);
  }
  
  private void removeExternalEntry(TrieEntry<K, V> h) {
    if (h == this.root)
      throw new IllegalArgumentException("Cannot delete root Entry!"); 
    if (!h.isExternalNode())
      throw new IllegalArgumentException(h + " is not an external Entry!"); 
    TrieEntry<K, V> parent = h.parent;
    TrieEntry<K, V> child = (h.left == h) ? h.right : h.left;
    if (parent.left == h) {
      parent.left = child;
    } else {
      parent.right = child;
    } 
    if (child.bitIndex > parent.bitIndex) {
      child.parent = parent;
    } else {
      child.predecessor = parent;
    } 
  }
  
  private void removeInternalEntry(TrieEntry<K, V> h) {
    if (h == this.root)
      throw new IllegalArgumentException("Cannot delete root Entry!"); 
    if (!h.isInternalNode())
      throw new IllegalArgumentException(h + " is not an internal Entry!"); 
    TrieEntry<K, V> p = h.predecessor;
    p.bitIndex = h.bitIndex;
    TrieEntry<K, V> parent = p.parent;
    TrieEntry<K, V> child = (p.left == h) ? p.right : p.left;
    if (p.predecessor == p && p.parent != h)
      p.predecessor = p.parent; 
    if (parent.left == p) {
      parent.left = child;
    } else {
      parent.right = child;
    } 
    if (child.bitIndex > parent.bitIndex)
      child.parent = parent; 
    if (h.left.parent == h)
      h.left.parent = p; 
    if (h.right.parent == h)
      h.right.parent = p; 
    if (h.parent.left == h) {
      h.parent.left = p;
    } else {
      h.parent.right = p;
    } 
    p.parent = h.parent;
    p.left = h.left;
    p.right = h.right;
    if (isValidUplink(p.left, p))
      p.left.predecessor = p; 
    if (isValidUplink(p.right, p))
      p.right.predecessor = p; 
  }
  
  TrieEntry<K, V> nextEntry(TrieEntry<K, V> node) {
    if (node == null)
      return firstEntry(); 
    return nextEntryImpl(node.predecessor, node, (TrieEntry<K, V>)null);
  }
  
  TrieEntry<K, V> nextEntryImpl(TrieEntry<K, V> start, TrieEntry<K, V> previous, TrieEntry<K, V> tree) {
    TrieEntry<K, V> current = start;
    if (previous == null || start != previous.predecessor)
      while (!current.left.isEmpty()) {
        if (previous == current.left)
          break; 
        if (isValidUplink(current.left, current))
          return current.left; 
        current = current.left;
      }  
    if (current.isEmpty())
      return null; 
    if (current.right == null)
      return null; 
    if (previous != current.right) {
      if (isValidUplink(current.right, current))
        return current.right; 
      return nextEntryImpl(current.right, previous, tree);
    } 
    while (current == current.parent.right) {
      if (current == tree)
        return null; 
      current = current.parent;
    } 
    if (current == tree)
      return null; 
    if (current.parent.right == null)
      return null; 
    if (previous != current.parent.right && isValidUplink(current.parent.right, current.parent))
      return current.parent.right; 
    if (current.parent.right == current.parent)
      return null; 
    return nextEntryImpl(current.parent.right, previous, tree);
  }
  
  TrieEntry<K, V> firstEntry() {
    if (isEmpty())
      return null; 
    return followLeft(this.root);
  }
  
  TrieEntry<K, V> followLeft(TrieEntry<K, V> node) {
    while (true) {
      TrieEntry<K, V> child = node.left;
      if (child.isEmpty())
        child = node.right; 
      if (child.bitIndex <= node.bitIndex)
        return child; 
      node = child;
    } 
  }
  
  public Comparator<? super K> comparator() {
    return getKeyAnalyzer();
  }
  
  public K firstKey() {
    if (size() == 0)
      throw new NoSuchElementException(); 
    return firstEntry().getKey();
  }
  
  public K lastKey() {
    TrieEntry<K, V> entry = lastEntry();
    if (entry != null)
      return entry.getKey(); 
    throw new NoSuchElementException();
  }
  
  public K nextKey(K key) {
    if (key == null)
      throw new NullPointerException(); 
    TrieEntry<K, V> entry = getEntry(key);
    if (entry != null) {
      TrieEntry<K, V> nextEntry = nextEntry(entry);
      return (nextEntry != null) ? nextEntry.getKey() : null;
    } 
    return null;
  }
  
  public K previousKey(K key) {
    if (key == null)
      throw new NullPointerException(); 
    TrieEntry<K, V> entry = getEntry(key);
    if (entry != null) {
      TrieEntry<K, V> prevEntry = previousEntry(entry);
      return (prevEntry != null) ? prevEntry.getKey() : null;
    } 
    return null;
  }
  
  public OrderedMapIterator<K, V> mapIterator() {
    return new TrieMapIterator();
  }
  
  public SortedMap<K, V> prefixMap(K key) {
    return getPrefixMapByBits(key, 0, lengthInBits(key));
  }
  
  private SortedMap<K, V> getPrefixMapByBits(K key, int offsetInBits, int lengthInBits) {
    int offsetLength = offsetInBits + lengthInBits;
    if (offsetLength > lengthInBits(key))
      throw new IllegalArgumentException(offsetInBits + " + " + lengthInBits + " > " + lengthInBits(key)); 
    if (offsetLength == 0)
      return (SortedMap<K, V>)this; 
    return new PrefixRangeMap(key, offsetInBits, lengthInBits);
  }
  
  public SortedMap<K, V> headMap(K toKey) {
    return new RangeEntryMap(null, toKey);
  }
  
  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    return new RangeEntryMap(fromKey, toKey);
  }
  
  public SortedMap<K, V> tailMap(K fromKey) {
    return new RangeEntryMap(fromKey, null);
  }
  
  TrieEntry<K, V> higherEntry(K key) {
    int lengthInBits = lengthInBits(key);
    if (lengthInBits == 0) {
      if (!this.root.isEmpty()) {
        if (size() > 1)
          return nextEntry(this.root); 
        return null;
      } 
      return firstEntry();
    } 
    TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
    if (compareKeys(key, found.key))
      return nextEntry(found); 
    int bitIndex = bitIndex(key, found.key);
    if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
      TrieEntry<K, V> added = new TrieEntry<K, V>(key, null, bitIndex);
      addEntry(added, lengthInBits);
      incrementSize();
      TrieEntry<K, V> ceil = nextEntry(added);
      removeEntry(added);
      this.modCount -= 2;
      return ceil;
    } 
    if (KeyAnalyzer.isNullBitKey(bitIndex)) {
      if (!this.root.isEmpty())
        return firstEntry(); 
      if (size() > 1)
        return nextEntry(firstEntry()); 
      return null;
    } 
    if (KeyAnalyzer.isEqualBitKey(bitIndex))
      return nextEntry(found); 
    throw new IllegalStateException("invalid lookup: " + key);
  }
  
  TrieEntry<K, V> ceilingEntry(K key) {
    int lengthInBits = lengthInBits(key);
    if (lengthInBits == 0) {
      if (!this.root.isEmpty())
        return this.root; 
      return firstEntry();
    } 
    TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
    if (compareKeys(key, found.key))
      return found; 
    int bitIndex = bitIndex(key, found.key);
    if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
      TrieEntry<K, V> added = new TrieEntry<K, V>(key, null, bitIndex);
      addEntry(added, lengthInBits);
      incrementSize();
      TrieEntry<K, V> ceil = nextEntry(added);
      removeEntry(added);
      this.modCount -= 2;
      return ceil;
    } 
    if (KeyAnalyzer.isNullBitKey(bitIndex)) {
      if (!this.root.isEmpty())
        return this.root; 
      return firstEntry();
    } 
    if (KeyAnalyzer.isEqualBitKey(bitIndex))
      return found; 
    throw new IllegalStateException("invalid lookup: " + key);
  }
  
  TrieEntry<K, V> lowerEntry(K key) {
    int lengthInBits = lengthInBits(key);
    if (lengthInBits == 0)
      return null; 
    TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
    if (compareKeys(key, found.key))
      return previousEntry(found); 
    int bitIndex = bitIndex(key, found.key);
    if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
      TrieEntry<K, V> added = new TrieEntry<K, V>(key, null, bitIndex);
      addEntry(added, lengthInBits);
      incrementSize();
      TrieEntry<K, V> prior = previousEntry(added);
      removeEntry(added);
      this.modCount -= 2;
      return prior;
    } 
    if (KeyAnalyzer.isNullBitKey(bitIndex))
      return null; 
    if (KeyAnalyzer.isEqualBitKey(bitIndex))
      return previousEntry(found); 
    throw new IllegalStateException("invalid lookup: " + key);
  }
  
  TrieEntry<K, V> floorEntry(K key) {
    int lengthInBits = lengthInBits(key);
    if (lengthInBits == 0) {
      if (!this.root.isEmpty())
        return this.root; 
      return null;
    } 
    TrieEntry<K, V> found = getNearestEntryForKey(key, lengthInBits);
    if (compareKeys(key, found.key))
      return found; 
    int bitIndex = bitIndex(key, found.key);
    if (KeyAnalyzer.isValidBitIndex(bitIndex)) {
      TrieEntry<K, V> added = new TrieEntry<K, V>(key, null, bitIndex);
      addEntry(added, lengthInBits);
      incrementSize();
      TrieEntry<K, V> floor = previousEntry(added);
      removeEntry(added);
      this.modCount -= 2;
      return floor;
    } 
    if (KeyAnalyzer.isNullBitKey(bitIndex)) {
      if (!this.root.isEmpty())
        return this.root; 
      return null;
    } 
    if (KeyAnalyzer.isEqualBitKey(bitIndex))
      return found; 
    throw new IllegalStateException("invalid lookup: " + key);
  }
  
  TrieEntry<K, V> subtree(K prefix, int offsetInBits, int lengthInBits) {
    TrieEntry<K, V> current = this.root.left;
    TrieEntry<K, V> path = this.root;
    while (current.bitIndex > path.bitIndex && lengthInBits >= current.bitIndex) {
      path = current;
      if (!isBitSet(prefix, offsetInBits + current.bitIndex, offsetInBits + lengthInBits)) {
        current = current.left;
        continue;
      } 
      current = current.right;
    } 
    TrieEntry<K, V> entry = current.isEmpty() ? path : current;
    if (entry.isEmpty())
      return null; 
    int endIndexInBits = offsetInBits + lengthInBits;
    if (entry == this.root && lengthInBits(entry.getKey()) < endIndexInBits)
      return null; 
    if (isBitSet(prefix, endIndexInBits, endIndexInBits) != isBitSet(entry.key, lengthInBits, lengthInBits(entry.key)))
      return null; 
    int bitIndex = getKeyAnalyzer().bitIndex(prefix, offsetInBits, lengthInBits, entry.key, 0, lengthInBits(entry.getKey()));
    if (bitIndex >= 0 && bitIndex < lengthInBits)
      return null; 
    return entry;
  }
  
  TrieEntry<K, V> lastEntry() {
    return followRight(this.root.left);
  }
  
  TrieEntry<K, V> followRight(TrieEntry<K, V> node) {
    if (node.right == null)
      return null; 
    while (node.right.bitIndex > node.bitIndex)
      node = node.right; 
    return node.right;
  }
  
  TrieEntry<K, V> previousEntry(TrieEntry<K, V> start) {
    if (start.predecessor == null)
      throw new IllegalArgumentException("must have come from somewhere!"); 
    if (start.predecessor.right == start) {
      if (isValidUplink(start.predecessor.left, start.predecessor))
        return start.predecessor.left; 
      return followRight(start.predecessor.left);
    } 
    TrieEntry<K, V> node = start.predecessor;
    while (node.parent != null && node == node.parent.left)
      node = node.parent; 
    if (node.parent == null)
      return null; 
    if (isValidUplink(node.parent.left, node.parent)) {
      if (node.parent.left == this.root) {
        if (this.root.isEmpty())
          return null; 
        return this.root;
      } 
      return node.parent.left;
    } 
    return followRight(node.parent.left);
  }
  
  TrieEntry<K, V> nextEntryInSubtree(TrieEntry<K, V> node, TrieEntry<K, V> parentOfSubtree) {
    if (node == null)
      return firstEntry(); 
    return nextEntryImpl(node.predecessor, node, parentOfSubtree);
  }
  
  static boolean isValidUplink(TrieEntry<?, ?> next, TrieEntry<?, ?> from) {
    return (next != null && next.bitIndex <= from.bitIndex && !next.isEmpty());
  }
  
  private static class Reference<E> {
    private E item;
    
    private Reference() {}
    
    public void set(E item) {
      this.item = item;
    }
    
    public E get() {
      return this.item;
    }
  }
  
  protected static class TrieEntry<K, V> extends AbstractBitwiseTrie.BasicEntry<K, V> {
    private static final long serialVersionUID = 4596023148184140013L;
    
    protected int bitIndex;
    
    protected TrieEntry<K, V> parent;
    
    protected TrieEntry<K, V> left;
    
    protected TrieEntry<K, V> right;
    
    protected TrieEntry<K, V> predecessor;
    
    public TrieEntry(K key, V value, int bitIndex) {
      super(key, value);
      this.bitIndex = bitIndex;
      this.parent = null;
      this.left = this;
      this.right = null;
      this.predecessor = this;
    }
    
    public boolean isEmpty() {
      return (this.key == null);
    }
    
    public boolean isInternalNode() {
      return (this.left != this && this.right != this);
    }
    
    public boolean isExternalNode() {
      return !isInternalNode();
    }
    
    public String toString() {
      StringBuilder buffer = new StringBuilder();
      if (this.bitIndex == -1) {
        buffer.append("RootEntry(");
      } else {
        buffer.append("Entry(");
      } 
      buffer.append("key=").append(getKey()).append(" [").append(this.bitIndex).append("], ");
      buffer.append("value=").append(getValue()).append(", ");
      if (this.parent != null) {
        if (this.parent.bitIndex == -1) {
          buffer.append("parent=").append("ROOT");
        } else {
          buffer.append("parent=").append(this.parent.getKey()).append(" [").append(this.parent.bitIndex).append("]");
        } 
      } else {
        buffer.append("parent=").append("null");
      } 
      buffer.append(", ");
      if (this.left != null) {
        if (this.left.bitIndex == -1) {
          buffer.append("left=").append("ROOT");
        } else {
          buffer.append("left=").append(this.left.getKey()).append(" [").append(this.left.bitIndex).append("]");
        } 
      } else {
        buffer.append("left=").append("null");
      } 
      buffer.append(", ");
      if (this.right != null) {
        if (this.right.bitIndex == -1) {
          buffer.append("right=").append("ROOT");
        } else {
          buffer.append("right=").append(this.right.getKey()).append(" [").append(this.right.bitIndex).append("]");
        } 
      } else {
        buffer.append("right=").append("null");
      } 
      buffer.append(", ");
      if (this.predecessor != null)
        if (this.predecessor.bitIndex == -1) {
          buffer.append("predecessor=").append("ROOT");
        } else {
          buffer.append("predecessor=").append(this.predecessor.getKey()).append(" [").append(this.predecessor.bitIndex).append("]");
        }  
      buffer.append(")");
      return buffer.toString();
    }
  }
  
  private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
    private EntrySet() {}
    
    public Iterator<Map.Entry<K, V>> iterator() {
      return new EntryIterator();
    }
    
    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry))
        return false; 
      AbstractPatriciaTrie.TrieEntry<K, V> candidate = AbstractPatriciaTrie.this.getEntry(((Map.Entry)o).getKey());
      return (candidate != null && candidate.equals(o));
    }
    
    public boolean remove(Object obj) {
      if (!(obj instanceof Map.Entry))
        return false; 
      if (!contains(obj))
        return false; 
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
      AbstractPatriciaTrie.this.remove(entry.getKey());
      return true;
    }
    
    public int size() {
      return AbstractPatriciaTrie.this.size();
    }
    
    public void clear() {
      AbstractPatriciaTrie.this.clear();
    }
    
    private class EntryIterator extends AbstractPatriciaTrie<K, V>.TrieIterator<Map.Entry<K, V>> {
      private EntryIterator() {}
      
      public Map.Entry<K, V> next() {
        return nextEntry();
      }
    }
  }
  
  private class KeySet extends AbstractSet<K> {
    private KeySet() {}
    
    public Iterator<K> iterator() {
      return new KeyIterator();
    }
    
    public int size() {
      return AbstractPatriciaTrie.this.size();
    }
    
    public boolean contains(Object o) {
      return AbstractPatriciaTrie.this.containsKey(o);
    }
    
    public boolean remove(Object o) {
      int size = size();
      AbstractPatriciaTrie.this.remove(o);
      return (size != size());
    }
    
    public void clear() {
      AbstractPatriciaTrie.this.clear();
    }
    
    private class KeyIterator extends AbstractPatriciaTrie<K, V>.TrieIterator<K> {
      private KeyIterator() {}
      
      public K next() {
        return (K)nextEntry().getKey();
      }
    }
  }
  
  private class Values extends AbstractCollection<V> {
    private Values() {}
    
    public Iterator<V> iterator() {
      return new ValueIterator();
    }
    
    public int size() {
      return AbstractPatriciaTrie.this.size();
    }
    
    public boolean contains(Object o) {
      return AbstractPatriciaTrie.this.containsValue(o);
    }
    
    public void clear() {
      AbstractPatriciaTrie.this.clear();
    }
    
    public boolean remove(Object o) {
      for (Iterator<V> it = iterator(); it.hasNext(); ) {
        V value = it.next();
        if (AbstractBitwiseTrie.compare(value, o)) {
          it.remove();
          return true;
        } 
      } 
      return false;
    }
    
    private class ValueIterator extends AbstractPatriciaTrie<K, V>.TrieIterator<V> {
      private ValueIterator() {}
      
      public V next() {
        return (V)nextEntry().getValue();
      }
    }
  }
  
  abstract class TrieIterator<E> implements Iterator<E> {
    protected int expectedModCount = AbstractPatriciaTrie.this.modCount;
    
    protected AbstractPatriciaTrie.TrieEntry<K, V> next;
    
    protected AbstractPatriciaTrie.TrieEntry<K, V> current;
    
    protected TrieIterator() {
      this.next = AbstractPatriciaTrie.this.nextEntry((AbstractPatriciaTrie.TrieEntry<K, V>)null);
    }
    
    protected TrieIterator(AbstractPatriciaTrie.TrieEntry<K, V> firstEntry) {
      this.next = firstEntry;
    }
    
    protected AbstractPatriciaTrie.TrieEntry<K, V> nextEntry() {
      if (this.expectedModCount != AbstractPatriciaTrie.this.modCount)
        throw new ConcurrentModificationException(); 
      AbstractPatriciaTrie.TrieEntry<K, V> e = this.next;
      if (e == null)
        throw new NoSuchElementException(); 
      this.next = findNext(e);
      this.current = e;
      return e;
    }
    
    protected AbstractPatriciaTrie.TrieEntry<K, V> findNext(AbstractPatriciaTrie.TrieEntry<K, V> prior) {
      return AbstractPatriciaTrie.this.nextEntry(prior);
    }
    
    public boolean hasNext() {
      return (this.next != null);
    }
    
    public void remove() {
      if (this.current == null)
        throw new IllegalStateException(); 
      if (this.expectedModCount != AbstractPatriciaTrie.this.modCount)
        throw new ConcurrentModificationException(); 
      AbstractPatriciaTrie.TrieEntry<K, V> node = this.current;
      this.current = null;
      AbstractPatriciaTrie.this.removeEntry(node);
      this.expectedModCount = AbstractPatriciaTrie.this.modCount;
    }
  }
  
  private class TrieMapIterator extends TrieIterator<K> implements OrderedMapIterator<K, V> {
    protected AbstractPatriciaTrie.TrieEntry<K, V> previous;
    
    private TrieMapIterator() {}
    
    public K next() {
      return nextEntry().getKey();
    }
    
    public K getKey() {
      if (this.current == null)
        throw new IllegalStateException(); 
      return (K)this.current.getKey();
    }
    
    public V getValue() {
      if (this.current == null)
        throw new IllegalStateException(); 
      return (V)this.current.getValue();
    }
    
    public V setValue(V value) {
      if (this.current == null)
        throw new IllegalStateException(); 
      return (V)this.current.setValue(value);
    }
    
    public boolean hasPrevious() {
      return (this.previous != null);
    }
    
    public K previous() {
      return previousEntry().getKey();
    }
    
    protected AbstractPatriciaTrie.TrieEntry<K, V> nextEntry() {
      AbstractPatriciaTrie.TrieEntry<K, V> nextEntry = super.nextEntry();
      this.previous = nextEntry;
      return nextEntry;
    }
    
    protected AbstractPatriciaTrie.TrieEntry<K, V> previousEntry() {
      if (this.expectedModCount != AbstractPatriciaTrie.this.modCount)
        throw new ConcurrentModificationException(); 
      AbstractPatriciaTrie.TrieEntry<K, V> e = this.previous;
      if (e == null)
        throw new NoSuchElementException(); 
      this.previous = AbstractPatriciaTrie.this.previousEntry(e);
      this.next = this.current;
      this.current = e;
      return this.current;
    }
  }
  
  private abstract class RangeMap extends AbstractMap<K, V> implements SortedMap<K, V> {
    private volatile transient Set<Map.Entry<K, V>> entrySet;
    
    private RangeMap() {}
    
    public Comparator<? super K> comparator() {
      return AbstractPatriciaTrie.this.comparator();
    }
    
    public boolean containsKey(Object key) {
      if (!inRange((K)AbstractPatriciaTrie.this.castKey(key)))
        return false; 
      return AbstractPatriciaTrie.this.containsKey(key);
    }
    
    public V remove(Object key) {
      if (!inRange((K)AbstractPatriciaTrie.this.castKey(key)))
        return null; 
      return (V)AbstractPatriciaTrie.this.remove(key);
    }
    
    public V get(Object key) {
      if (!inRange((K)AbstractPatriciaTrie.this.castKey(key)))
        return null; 
      return (V)AbstractPatriciaTrie.this.get(key);
    }
    
    public V put(K key, V value) {
      if (!inRange(key))
        throw new IllegalArgumentException("Key is out of range: " + key); 
      return AbstractPatriciaTrie.this.put(key, value);
    }
    
    public Set<Map.Entry<K, V>> entrySet() {
      if (this.entrySet == null)
        this.entrySet = createEntrySet(); 
      return this.entrySet;
    }
    
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
      if (!inRange2(fromKey))
        throw new IllegalArgumentException("FromKey is out of range: " + fromKey); 
      if (!inRange2(toKey))
        throw new IllegalArgumentException("ToKey is out of range: " + toKey); 
      return createRangeMap(fromKey, isFromInclusive(), toKey, isToInclusive());
    }
    
    public SortedMap<K, V> headMap(K toKey) {
      if (!inRange2(toKey))
        throw new IllegalArgumentException("ToKey is out of range: " + toKey); 
      return createRangeMap(getFromKey(), isFromInclusive(), toKey, isToInclusive());
    }
    
    public SortedMap<K, V> tailMap(K fromKey) {
      if (!inRange2(fromKey))
        throw new IllegalArgumentException("FromKey is out of range: " + fromKey); 
      return createRangeMap(fromKey, isFromInclusive(), getToKey(), isToInclusive());
    }
    
    protected boolean inRange(K key) {
      K fromKey = getFromKey();
      K toKey = getToKey();
      return ((fromKey == null || inFromRange(key, false)) && (toKey == null || inToRange(key, false)));
    }
    
    protected boolean inRange2(K key) {
      K fromKey = getFromKey();
      K toKey = getToKey();
      return ((fromKey == null || inFromRange(key, false)) && (toKey == null || inToRange(key, true)));
    }
    
    protected boolean inFromRange(K key, boolean forceInclusive) {
      K fromKey = getFromKey();
      boolean fromInclusive = isFromInclusive();
      int ret = AbstractPatriciaTrie.this.getKeyAnalyzer().compare(key, fromKey);
      if (fromInclusive || forceInclusive)
        return (ret >= 0); 
      return (ret > 0);
    }
    
    protected boolean inToRange(K key, boolean forceInclusive) {
      K toKey = getToKey();
      boolean toInclusive = isToInclusive();
      int ret = AbstractPatriciaTrie.this.getKeyAnalyzer().compare(key, toKey);
      if (toInclusive || forceInclusive)
        return (ret <= 0); 
      return (ret < 0);
    }
    
    protected abstract Set<Map.Entry<K, V>> createEntrySet();
    
    protected abstract K getFromKey();
    
    protected abstract boolean isFromInclusive();
    
    protected abstract K getToKey();
    
    protected abstract boolean isToInclusive();
    
    protected abstract SortedMap<K, V> createRangeMap(K param1K1, boolean param1Boolean1, K param1K2, boolean param1Boolean2);
  }
  
  private class RangeEntryMap extends RangeMap {
    private final K fromKey;
    
    private final K toKey;
    
    private final boolean fromInclusive;
    
    private final boolean toInclusive;
    
    protected RangeEntryMap(K fromKey, K toKey) {
      this(fromKey, true, toKey, false);
    }
    
    protected RangeEntryMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      if (fromKey == null && toKey == null)
        throw new IllegalArgumentException("must have a from or to!"); 
      if (fromKey != null && toKey != null && AbstractPatriciaTrie.this.getKeyAnalyzer().compare(fromKey, toKey) > 0)
        throw new IllegalArgumentException("fromKey > toKey"); 
      this.fromKey = fromKey;
      this.fromInclusive = fromInclusive;
      this.toKey = toKey;
      this.toInclusive = toInclusive;
    }
    
    public K firstKey() {
      Map.Entry<K, V> e = null;
      if (this.fromKey == null) {
        e = AbstractPatriciaTrie.this.firstEntry();
      } else if (this.fromInclusive) {
        e = AbstractPatriciaTrie.this.ceilingEntry(this.fromKey);
      } else {
        e = AbstractPatriciaTrie.this.higherEntry(this.fromKey);
      } 
      K first = (e != null) ? e.getKey() : null;
      if (e == null || (this.toKey != null && !inToRange(first, false)))
        throw new NoSuchElementException(); 
      return first;
    }
    
    public K lastKey() {
      Map.Entry<K, V> e;
      if (this.toKey == null) {
        e = AbstractPatriciaTrie.this.lastEntry();
      } else if (this.toInclusive) {
        e = AbstractPatriciaTrie.this.floorEntry(this.toKey);
      } else {
        e = AbstractPatriciaTrie.this.lowerEntry(this.toKey);
      } 
      K last = (e != null) ? e.getKey() : null;
      if (e == null || (this.fromKey != null && !inFromRange(last, false)))
        throw new NoSuchElementException(); 
      return last;
    }
    
    protected Set<Map.Entry<K, V>> createEntrySet() {
      return new AbstractPatriciaTrie.RangeEntrySet(this);
    }
    
    public K getFromKey() {
      return this.fromKey;
    }
    
    public K getToKey() {
      return this.toKey;
    }
    
    public boolean isFromInclusive() {
      return this.fromInclusive;
    }
    
    public boolean isToInclusive() {
      return this.toInclusive;
    }
    
    protected SortedMap<K, V> createRangeMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      return new RangeEntryMap(fromKey, fromInclusive, toKey, toInclusive);
    }
  }
  
  private class RangeEntrySet extends AbstractSet<Map.Entry<K, V>> {
    private final AbstractPatriciaTrie<K, V>.RangeMap delegate;
    
    private transient int size = -1;
    
    private transient int expectedModCount;
    
    public RangeEntrySet(AbstractPatriciaTrie<K, V>.RangeMap delegate) {
      if (delegate == null)
        throw new NullPointerException("delegate"); 
      this.delegate = delegate;
    }
    
    public Iterator<Map.Entry<K, V>> iterator() {
      K fromKey = this.delegate.getFromKey();
      K toKey = this.delegate.getToKey();
      AbstractPatriciaTrie.TrieEntry<K, V> first = null;
      if (fromKey == null) {
        first = AbstractPatriciaTrie.this.firstEntry();
      } else {
        first = AbstractPatriciaTrie.this.ceilingEntry(fromKey);
      } 
      AbstractPatriciaTrie.TrieEntry<K, V> last = null;
      if (toKey != null)
        last = AbstractPatriciaTrie.this.ceilingEntry(toKey); 
      return new EntryIterator(first, last);
    }
    
    public int size() {
      if (this.size == -1 || this.expectedModCount != AbstractPatriciaTrie.this.modCount) {
        this.size = 0;
        for (Iterator<?> it = iterator(); it.hasNext(); it.next())
          this.size++; 
        this.expectedModCount = AbstractPatriciaTrie.this.modCount;
      } 
      return this.size;
    }
    
    public boolean isEmpty() {
      return !iterator().hasNext();
    }
    
    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry))
        return false; 
      Map.Entry<K, V> entry = (Map.Entry<K, V>)o;
      K key = entry.getKey();
      if (!this.delegate.inRange(key))
        return false; 
      AbstractPatriciaTrie.TrieEntry<K, V> node = AbstractPatriciaTrie.this.getEntry(key);
      return (node != null && AbstractBitwiseTrie.compare(node.getValue(), entry.getValue()));
    }
    
    public boolean remove(Object o) {
      if (!(o instanceof Map.Entry))
        return false; 
      Map.Entry<K, V> entry = (Map.Entry<K, V>)o;
      K key = entry.getKey();
      if (!this.delegate.inRange(key))
        return false; 
      AbstractPatriciaTrie.TrieEntry<K, V> node = AbstractPatriciaTrie.this.getEntry(key);
      if (node != null && AbstractBitwiseTrie.compare(node.getValue(), entry.getValue())) {
        AbstractPatriciaTrie.this.removeEntry(node);
        return true;
      } 
      return false;
    }
    
    private final class EntryIterator extends AbstractPatriciaTrie<K, V>.TrieIterator<Map.Entry<K, V>> {
      private final K excludedKey;
      
      private EntryIterator(AbstractPatriciaTrie.TrieEntry<K, V> first, AbstractPatriciaTrie.TrieEntry<K, V> last) {
        super(first);
        this.excludedKey = (last != null) ? last.getKey() : null;
      }
      
      public boolean hasNext() {
        return (this.next != null && !AbstractBitwiseTrie.compare(this.next.key, this.excludedKey));
      }
      
      public Map.Entry<K, V> next() {
        if (this.next == null || AbstractBitwiseTrie.compare(this.next.key, this.excludedKey))
          throw new NoSuchElementException(); 
        return nextEntry();
      }
    }
  }
  
  private class PrefixRangeMap extends RangeMap {
    private final K prefix;
    
    private final int offsetInBits;
    
    private final int lengthInBits;
    
    private K fromKey = null;
    
    private K toKey = null;
    
    private transient int expectedModCount = 0;
    
    private int size = -1;
    
    private PrefixRangeMap(K prefix, int offsetInBits, int lengthInBits) {
      this.prefix = prefix;
      this.offsetInBits = offsetInBits;
      this.lengthInBits = lengthInBits;
    }
    
    private int fixup() {
      if (this.size == -1 || AbstractPatriciaTrie.this.modCount != this.expectedModCount) {
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        this.size = 0;
        Map.Entry<K, V> entry = null;
        if (it.hasNext()) {
          entry = it.next();
          this.size = 1;
        } 
        this.fromKey = (entry == null) ? null : entry.getKey();
        if (this.fromKey != null) {
          AbstractPatriciaTrie.TrieEntry<K, V> prior = AbstractPatriciaTrie.this.previousEntry((AbstractPatriciaTrie.TrieEntry<K, V>)entry);
          this.fromKey = (prior == null) ? null : prior.getKey();
        } 
        this.toKey = this.fromKey;
        while (it.hasNext()) {
          this.size++;
          entry = it.next();
        } 
        this.toKey = (entry == null) ? null : entry.getKey();
        if (this.toKey != null) {
          entry = AbstractPatriciaTrie.this.nextEntry((AbstractPatriciaTrie.TrieEntry<K, V>)entry);
          this.toKey = (entry == null) ? null : entry.getKey();
        } 
        this.expectedModCount = AbstractPatriciaTrie.this.modCount;
      } 
      return this.size;
    }
    
    public K firstKey() {
      fixup();
      Map.Entry<K, V> e = null;
      if (this.fromKey == null) {
        e = AbstractPatriciaTrie.this.firstEntry();
      } else {
        e = AbstractPatriciaTrie.this.higherEntry(this.fromKey);
      } 
      K first = (e != null) ? e.getKey() : null;
      if (e == null || !AbstractPatriciaTrie.this.getKeyAnalyzer().isPrefix(this.prefix, this.offsetInBits, this.lengthInBits, first))
        throw new NoSuchElementException(); 
      return first;
    }
    
    public K lastKey() {
      fixup();
      Map.Entry<K, V> e = null;
      if (this.toKey == null) {
        e = AbstractPatriciaTrie.this.lastEntry();
      } else {
        e = AbstractPatriciaTrie.this.lowerEntry(this.toKey);
      } 
      K last = (e != null) ? e.getKey() : null;
      if (e == null || !AbstractPatriciaTrie.this.getKeyAnalyzer().isPrefix(this.prefix, this.offsetInBits, this.lengthInBits, last))
        throw new NoSuchElementException(); 
      return last;
    }
    
    protected boolean inRange(K key) {
      return AbstractPatriciaTrie.this.getKeyAnalyzer().isPrefix(this.prefix, this.offsetInBits, this.lengthInBits, key);
    }
    
    protected boolean inRange2(K key) {
      return inRange(key);
    }
    
    protected boolean inFromRange(K key, boolean forceInclusive) {
      return AbstractPatriciaTrie.this.getKeyAnalyzer().isPrefix(this.prefix, this.offsetInBits, this.lengthInBits, key);
    }
    
    protected boolean inToRange(K key, boolean forceInclusive) {
      return AbstractPatriciaTrie.this.getKeyAnalyzer().isPrefix(this.prefix, this.offsetInBits, this.lengthInBits, key);
    }
    
    protected Set<Map.Entry<K, V>> createEntrySet() {
      return new AbstractPatriciaTrie.PrefixRangeEntrySet(this);
    }
    
    public K getFromKey() {
      return this.fromKey;
    }
    
    public K getToKey() {
      return this.toKey;
    }
    
    public boolean isFromInclusive() {
      return false;
    }
    
    public boolean isToInclusive() {
      return false;
    }
    
    protected SortedMap<K, V> createRangeMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
      return new AbstractPatriciaTrie.RangeEntryMap(fromKey, fromInclusive, toKey, toInclusive);
    }
  }
  
  private final class PrefixRangeEntrySet extends RangeEntrySet {
    private final AbstractPatriciaTrie<K, V>.PrefixRangeMap delegate;
    
    private AbstractPatriciaTrie.TrieEntry<K, V> prefixStart;
    
    private int expectedModCount = 0;
    
    public PrefixRangeEntrySet(AbstractPatriciaTrie<K, V>.PrefixRangeMap delegate) {
      super(delegate);
      this.delegate = delegate;
    }
    
    public int size() {
      return this.delegate.fixup();
    }
    
    public Iterator<Map.Entry<K, V>> iterator() {
      if (AbstractPatriciaTrie.this.modCount != this.expectedModCount) {
        this.prefixStart = AbstractPatriciaTrie.this.subtree(this.delegate.prefix, this.delegate.offsetInBits, this.delegate.lengthInBits);
        this.expectedModCount = AbstractPatriciaTrie.this.modCount;
      } 
      if (this.prefixStart == null) {
        Set<Map.Entry<K, V>> empty = Collections.emptySet();
        return empty.iterator();
      } 
      if (this.delegate.lengthInBits >= this.prefixStart.bitIndex)
        return new SingletonIterator(this.prefixStart); 
      return new EntryIterator(this.prefixStart, this.delegate.prefix, this.delegate.offsetInBits, this.delegate.lengthInBits);
    }
    
    private final class SingletonIterator implements Iterator<Map.Entry<K, V>> {
      private final AbstractPatriciaTrie.TrieEntry<K, V> entry;
      
      private int hit = 0;
      
      public SingletonIterator(AbstractPatriciaTrie.TrieEntry<K, V> entry) {
        this.entry = entry;
      }
      
      public boolean hasNext() {
        return (this.hit == 0);
      }
      
      public Map.Entry<K, V> next() {
        if (this.hit != 0)
          throw new NoSuchElementException(); 
        this.hit++;
        return this.entry;
      }
      
      public void remove() {
        if (this.hit != 1)
          throw new IllegalStateException(); 
        this.hit++;
        AbstractPatriciaTrie.this.removeEntry(this.entry);
      }
    }
    
    private final class EntryIterator extends AbstractPatriciaTrie<K, V>.TrieIterator<Map.Entry<K, V>> {
      private final K prefix;
      
      private final int offset;
      
      private final int lengthInBits;
      
      private boolean lastOne;
      
      private AbstractPatriciaTrie.TrieEntry<K, V> subtree;
      
      EntryIterator(AbstractPatriciaTrie.TrieEntry<K, V> startScan, K prefix, int offset, int lengthInBits) {
        this.subtree = startScan;
        this.next = AbstractPatriciaTrie.this.followLeft(startScan);
        this.prefix = prefix;
        this.offset = offset;
        this.lengthInBits = lengthInBits;
      }
      
      public Map.Entry<K, V> next() {
        Map.Entry<K, V> entry = nextEntry();
        if (this.lastOne)
          this.next = null; 
        return entry;
      }
      
      protected AbstractPatriciaTrie.TrieEntry<K, V> findNext(AbstractPatriciaTrie.TrieEntry<K, V> prior) {
        return AbstractPatriciaTrie.this.nextEntryInSubtree(prior, this.subtree);
      }
      
      public void remove() {
        boolean needsFixing = false;
        int bitIdx = this.subtree.bitIndex;
        if (this.current == this.subtree)
          needsFixing = true; 
        super.remove();
        if (bitIdx != this.subtree.bitIndex || needsFixing)
          this.subtree = AbstractPatriciaTrie.this.subtree(this.prefix, this.offset, this.lengthInBits); 
        if (this.lengthInBits >= this.subtree.bitIndex)
          this.lastOne = true; 
      }
    }
  }
  
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.root = new TrieEntry<K, V>(null, null, -1);
    int size = stream.readInt();
    for (int i = 0; i < size; i++) {
      K k = (K)stream.readObject();
      V v = (V)stream.readObject();
      put(k, v);
    } 
  }
  
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeInt(size());
    for (Map.Entry<K, V> entry : entrySet()) {
      stream.writeObject(entry.getKey());
      stream.writeObject(entry.getValue());
    } 
  }
}

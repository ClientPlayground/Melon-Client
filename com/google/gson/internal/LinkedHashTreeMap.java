package com.google.gson.internal;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public final class LinkedHashTreeMap<K, V> extends AbstractMap<K, V> implements Serializable {
  private static final Comparator<Comparable> NATURAL_ORDER = new Comparator<Comparable>() {
      public int compare(Comparable<Comparable> a, Comparable b) {
        return a.compareTo(b);
      }
    };
  
  Comparator<? super K> comparator;
  
  Node<K, V>[] table;
  
  final Node<K, V> header;
  
  int size = 0;
  
  int modCount = 0;
  
  int threshold;
  
  private EntrySet entrySet;
  
  private KeySet keySet;
  
  public LinkedHashTreeMap() {
    this((Comparator)NATURAL_ORDER);
  }
  
  public LinkedHashTreeMap(Comparator<? super K> comparator) {
    this.comparator = (comparator != null) ? comparator : (Comparator)NATURAL_ORDER;
    this.header = new Node<K, V>();
    this.table = (Node<K, V>[])new Node[16];
    this.threshold = this.table.length / 2 + this.table.length / 4;
  }
  
  public int size() {
    return this.size;
  }
  
  public V get(Object key) {
    Node<K, V> node = findByObject(key);
    return (node != null) ? node.value : null;
  }
  
  public boolean containsKey(Object key) {
    return (findByObject(key) != null);
  }
  
  public V put(K key, V value) {
    if (key == null)
      throw new NullPointerException("key == null"); 
    Node<K, V> created = find(key, true);
    V result = created.value;
    created.value = value;
    return result;
  }
  
  public void clear() {
    Arrays.fill((Object[])this.table, (Object)null);
    this.size = 0;
    this.modCount++;
    Node<K, V> header = this.header;
    for (Node<K, V> e = header.next; e != header; ) {
      Node<K, V> next = e.next;
      e.next = e.prev = null;
      e = next;
    } 
    header.next = header.prev = header;
  }
  
  public V remove(Object key) {
    Node<K, V> node = removeInternalByKey(key);
    return (node != null) ? node.value : null;
  }
  
  Node<K, V> find(K key, boolean create) {
    Node<K, V> created;
    Comparator<? super K> comparator = this.comparator;
    Node<K, V>[] table = this.table;
    int hash = secondaryHash(key.hashCode());
    int index = hash & table.length - 1;
    Node<K, V> nearest = table[index];
    int comparison = 0;
    if (nearest != null) {
      Comparable<Object> comparableKey = (comparator == NATURAL_ORDER) ? (Comparable<Object>)key : null;
      while (true) {
        comparison = (comparableKey != null) ? comparableKey.compareTo(nearest.key) : comparator.compare(key, nearest.key);
        if (comparison == 0)
          return nearest; 
        Node<K, V> child = (comparison < 0) ? nearest.left : nearest.right;
        if (child == null)
          break; 
        nearest = child;
      } 
    } 
    if (!create)
      return null; 
    Node<K, V> header = this.header;
    if (nearest == null) {
      if (comparator == NATURAL_ORDER && !(key instanceof Comparable))
        throw new ClassCastException(key.getClass().getName() + " is not Comparable"); 
      created = new Node<K, V>(nearest, key, hash, header, header.prev);
      table[index] = created;
    } else {
      created = new Node<K, V>(nearest, key, hash, header, header.prev);
      if (comparison < 0) {
        nearest.left = created;
      } else {
        nearest.right = created;
      } 
      rebalance(nearest, true);
    } 
    if (this.size++ > this.threshold)
      doubleCapacity(); 
    this.modCount++;
    return created;
  }
  
  Node<K, V> findByObject(Object key) {
    try {
      return (key != null) ? find((K)key, false) : null;
    } catch (ClassCastException e) {
      return null;
    } 
  }
  
  Node<K, V> findByEntry(Map.Entry<?, ?> entry) {
    Node<K, V> mine = findByObject(entry.getKey());
    boolean valuesEqual = (mine != null && equal(mine.value, entry.getValue()));
    return valuesEqual ? mine : null;
  }
  
  private boolean equal(Object a, Object b) {
    return (a == b || (a != null && a.equals(b)));
  }
  
  private static int secondaryHash(int h) {
    h ^= h >>> 20 ^ h >>> 12;
    return h ^ h >>> 7 ^ h >>> 4;
  }
  
  void removeInternal(Node<K, V> node, boolean unlink) {
    if (unlink) {
      node.prev.next = node.next;
      node.next.prev = node.prev;
      node.next = node.prev = null;
    } 
    Node<K, V> left = node.left;
    Node<K, V> right = node.right;
    Node<K, V> originalParent = node.parent;
    if (left != null && right != null) {
      Node<K, V> adjacent = (left.height > right.height) ? left.last() : right.first();
      removeInternal(adjacent, false);
      int leftHeight = 0;
      left = node.left;
      if (left != null) {
        leftHeight = left.height;
        adjacent.left = left;
        left.parent = adjacent;
        node.left = null;
      } 
      int rightHeight = 0;
      right = node.right;
      if (right != null) {
        rightHeight = right.height;
        adjacent.right = right;
        right.parent = adjacent;
        node.right = null;
      } 
      adjacent.height = Math.max(leftHeight, rightHeight) + 1;
      replaceInParent(node, adjacent);
      return;
    } 
    if (left != null) {
      replaceInParent(node, left);
      node.left = null;
    } else if (right != null) {
      replaceInParent(node, right);
      node.right = null;
    } else {
      replaceInParent(node, null);
    } 
    rebalance(originalParent, false);
    this.size--;
    this.modCount++;
  }
  
  Node<K, V> removeInternalByKey(Object key) {
    Node<K, V> node = findByObject(key);
    if (node != null)
      removeInternal(node, true); 
    return node;
  }
  
  private void replaceInParent(Node<K, V> node, Node<K, V> replacement) {
    Node<K, V> parent = node.parent;
    node.parent = null;
    if (replacement != null)
      replacement.parent = parent; 
    if (parent != null) {
      if (parent.left == node) {
        parent.left = replacement;
      } else {
        assert parent.right == node;
        parent.right = replacement;
      } 
    } else {
      int index = node.hash & this.table.length - 1;
      this.table[index] = replacement;
    } 
  }
  
  private void rebalance(Node<K, V> unbalanced, boolean insert) {
    for (Node<K, V> node = unbalanced; node != null; node = node.parent) {
      Node<K, V> left = node.left;
      Node<K, V> right = node.right;
      int leftHeight = (left != null) ? left.height : 0;
      int rightHeight = (right != null) ? right.height : 0;
      int delta = leftHeight - rightHeight;
      if (delta == -2) {
        Node<K, V> rightLeft = right.left;
        Node<K, V> rightRight = right.right;
        int rightRightHeight = (rightRight != null) ? rightRight.height : 0;
        int rightLeftHeight = (rightLeft != null) ? rightLeft.height : 0;
        int rightDelta = rightLeftHeight - rightRightHeight;
        if (rightDelta == -1 || (rightDelta == 0 && !insert)) {
          rotateLeft(node);
        } else {
          assert rightDelta == 1;
          rotateRight(right);
          rotateLeft(node);
        } 
        if (insert)
          break; 
      } else if (delta == 2) {
        Node<K, V> leftLeft = left.left;
        Node<K, V> leftRight = left.right;
        int leftRightHeight = (leftRight != null) ? leftRight.height : 0;
        int leftLeftHeight = (leftLeft != null) ? leftLeft.height : 0;
        int leftDelta = leftLeftHeight - leftRightHeight;
        if (leftDelta == 1 || (leftDelta == 0 && !insert)) {
          rotateRight(node);
        } else {
          assert leftDelta == -1;
          rotateLeft(left);
          rotateRight(node);
        } 
        if (insert)
          break; 
      } else if (delta == 0) {
        node.height = leftHeight + 1;
        if (insert)
          break; 
      } else {
        assert delta == -1 || delta == 1;
        node.height = Math.max(leftHeight, rightHeight) + 1;
        if (!insert)
          break; 
      } 
    } 
  }
  
  private void rotateLeft(Node<K, V> root) {
    Node<K, V> left = root.left;
    Node<K, V> pivot = root.right;
    Node<K, V> pivotLeft = pivot.left;
    Node<K, V> pivotRight = pivot.right;
    root.right = pivotLeft;
    if (pivotLeft != null)
      pivotLeft.parent = root; 
    replaceInParent(root, pivot);
    pivot.left = root;
    root.parent = pivot;
    root.height = Math.max((left != null) ? left.height : 0, (pivotLeft != null) ? pivotLeft.height : 0) + 1;
    pivot.height = Math.max(root.height, (pivotRight != null) ? pivotRight.height : 0) + 1;
  }
  
  private void rotateRight(Node<K, V> root) {
    Node<K, V> pivot = root.left;
    Node<K, V> right = root.right;
    Node<K, V> pivotLeft = pivot.left;
    Node<K, V> pivotRight = pivot.right;
    root.left = pivotRight;
    if (pivotRight != null)
      pivotRight.parent = root; 
    replaceInParent(root, pivot);
    pivot.right = root;
    root.parent = pivot;
    root.height = Math.max((right != null) ? right.height : 0, (pivotRight != null) ? pivotRight.height : 0) + 1;
    pivot.height = Math.max(root.height, (pivotLeft != null) ? pivotLeft.height : 0) + 1;
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    EntrySet result = this.entrySet;
    return (result != null) ? result : (this.entrySet = new EntrySet());
  }
  
  public Set<K> keySet() {
    KeySet result = this.keySet;
    return (result != null) ? result : (this.keySet = new KeySet());
  }
  
  static final class Node<K, V> implements Map.Entry<K, V> {
    Node<K, V> parent;
    
    Node<K, V> left;
    
    Node<K, V> right;
    
    Node<K, V> next;
    
    Node<K, V> prev;
    
    final K key;
    
    final int hash;
    
    V value;
    
    int height;
    
    Node() {
      this.key = null;
      this.hash = -1;
      this.next = this.prev = this;
    }
    
    Node(Node<K, V> parent, K key, int hash, Node<K, V> next, Node<K, V> prev) {
      this.parent = parent;
      this.key = key;
      this.hash = hash;
      this.height = 1;
      this.next = next;
      this.prev = prev;
      prev.next = this;
      next.prev = this;
    }
    
    public K getKey() {
      return this.key;
    }
    
    public V getValue() {
      return this.value;
    }
    
    public V setValue(V value) {
      V oldValue = this.value;
      this.value = value;
      return oldValue;
    }
    
    public boolean equals(Object o) {
      if (o instanceof Map.Entry) {
        Map.Entry other = (Map.Entry)o;
        return (((this.key == null) ? (other.getKey() == null) : this.key.equals(other.getKey())) && ((this.value == null) ? (other.getValue() == null) : this.value.equals(other.getValue())));
      } 
      return false;
    }
    
    public int hashCode() {
      return ((this.key == null) ? 0 : this.key.hashCode()) ^ ((this.value == null) ? 0 : this.value.hashCode());
    }
    
    public String toString() {
      return (new StringBuilder()).append(this.key).append("=").append(this.value).toString();
    }
    
    public Node<K, V> first() {
      Node<K, V> node = this;
      Node<K, V> child = node.left;
      while (child != null) {
        node = child;
        child = node.left;
      } 
      return node;
    }
    
    public Node<K, V> last() {
      Node<K, V> node = this;
      Node<K, V> child = node.right;
      while (child != null) {
        node = child;
        child = node.right;
      } 
      return node;
    }
  }
  
  private void doubleCapacity() {
    this.table = doubleCapacity(this.table);
    this.threshold = this.table.length / 2 + this.table.length / 4;
  }
  
  static <K, V> Node<K, V>[] doubleCapacity(Node<K, V>[] oldTable) {
    int oldCapacity = oldTable.length;
    Node[] arrayOfNode = new Node[oldCapacity * 2];
    AvlIterator<K, V> iterator = new AvlIterator<K, V>();
    AvlBuilder<K, V> leftBuilder = new AvlBuilder<K, V>();
    AvlBuilder<K, V> rightBuilder = new AvlBuilder<K, V>();
    for (int i = 0; i < oldCapacity; i++) {
      Node<K, V> root = oldTable[i];
      if (root != null) {
        iterator.reset(root);
        int leftSize = 0;
        int rightSize = 0;
        Node<K, V> node;
        while ((node = iterator.next()) != null) {
          if ((node.hash & oldCapacity) == 0) {
            leftSize++;
            continue;
          } 
          rightSize++;
        } 
        leftBuilder.reset(leftSize);
        rightBuilder.reset(rightSize);
        iterator.reset(root);
        while ((node = iterator.next()) != null) {
          if ((node.hash & oldCapacity) == 0) {
            leftBuilder.add(node);
            continue;
          } 
          rightBuilder.add(node);
        } 
        arrayOfNode[i] = (leftSize > 0) ? leftBuilder.root() : null;
        arrayOfNode[i + oldCapacity] = (rightSize > 0) ? rightBuilder.root() : null;
      } 
    } 
    return (Node<K, V>[])arrayOfNode;
  }
  
  static class AvlIterator<K, V> {
    private LinkedHashTreeMap.Node<K, V> stackTop;
    
    void reset(LinkedHashTreeMap.Node<K, V> root) {
      LinkedHashTreeMap.Node<K, V> stackTop = null;
      for (LinkedHashTreeMap.Node<K, V> n = root; n != null; n = n.left) {
        n.parent = stackTop;
        stackTop = n;
      } 
      this.stackTop = stackTop;
    }
    
    public LinkedHashTreeMap.Node<K, V> next() {
      LinkedHashTreeMap.Node<K, V> stackTop = this.stackTop;
      if (stackTop == null)
        return null; 
      LinkedHashTreeMap.Node<K, V> result = stackTop;
      stackTop = result.parent;
      result.parent = null;
      for (LinkedHashTreeMap.Node<K, V> n = result.right; n != null; n = n.left) {
        n.parent = stackTop;
        stackTop = n;
      } 
      this.stackTop = stackTop;
      return result;
    }
  }
  
  static final class AvlBuilder<K, V> {
    private LinkedHashTreeMap.Node<K, V> stack;
    
    private int leavesToSkip;
    
    private int leavesSkipped;
    
    private int size;
    
    void reset(int targetSize) {
      int treeCapacity = Integer.highestOneBit(targetSize) * 2 - 1;
      this.leavesToSkip = treeCapacity - targetSize;
      this.size = 0;
      this.leavesSkipped = 0;
      this.stack = null;
    }
    
    void add(LinkedHashTreeMap.Node<K, V> node) {
      node.left = node.parent = node.right = null;
      node.height = 1;
      if (this.leavesToSkip > 0 && (this.size & 0x1) == 0) {
        this.size++;
        this.leavesToSkip--;
        this.leavesSkipped++;
      } 
      node.parent = this.stack;
      this.stack = node;
      this.size++;
      if (this.leavesToSkip > 0 && (this.size & 0x1) == 0) {
        this.size++;
        this.leavesToSkip--;
        this.leavesSkipped++;
      } 
      for (int scale = 4; (this.size & scale - 1) == scale - 1; scale *= 2) {
        if (this.leavesSkipped == 0) {
          LinkedHashTreeMap.Node<K, V> right = this.stack;
          LinkedHashTreeMap.Node<K, V> center = right.parent;
          LinkedHashTreeMap.Node<K, V> left = center.parent;
          center.parent = left.parent;
          this.stack = center;
          center.left = left;
          center.right = right;
          right.height++;
          left.parent = center;
          right.parent = center;
        } else if (this.leavesSkipped == 1) {
          LinkedHashTreeMap.Node<K, V> right = this.stack;
          LinkedHashTreeMap.Node<K, V> center = right.parent;
          this.stack = center;
          center.right = right;
          right.height++;
          right.parent = center;
          this.leavesSkipped = 0;
        } else if (this.leavesSkipped == 2) {
          this.leavesSkipped = 0;
        } 
      } 
    }
    
    LinkedHashTreeMap.Node<K, V> root() {
      LinkedHashTreeMap.Node<K, V> stackTop = this.stack;
      if (stackTop.parent != null)
        throw new IllegalStateException(); 
      return stackTop;
    }
  }
  
  private abstract class LinkedTreeMapIterator<T> implements Iterator<T> {
    LinkedHashTreeMap.Node<K, V> next = LinkedHashTreeMap.this.header.next;
    
    LinkedHashTreeMap.Node<K, V> lastReturned = null;
    
    int expectedModCount = LinkedHashTreeMap.this.modCount;
    
    public final boolean hasNext() {
      return (this.next != LinkedHashTreeMap.this.header);
    }
    
    final LinkedHashTreeMap.Node<K, V> nextNode() {
      LinkedHashTreeMap.Node<K, V> e = this.next;
      if (e == LinkedHashTreeMap.this.header)
        throw new NoSuchElementException(); 
      if (LinkedHashTreeMap.this.modCount != this.expectedModCount)
        throw new ConcurrentModificationException(); 
      this.next = e.next;
      return this.lastReturned = e;
    }
    
    public final void remove() {
      if (this.lastReturned == null)
        throw new IllegalStateException(); 
      LinkedHashTreeMap.this.removeInternal(this.lastReturned, true);
      this.lastReturned = null;
      this.expectedModCount = LinkedHashTreeMap.this.modCount;
    }
    
    private LinkedTreeMapIterator() {}
  }
  
  final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
    public int size() {
      return LinkedHashTreeMap.this.size;
    }
    
    public Iterator<Map.Entry<K, V>> iterator() {
      return new LinkedHashTreeMap<K, V>.LinkedTreeMapIterator<Map.Entry<K, V>>() {
          public Map.Entry<K, V> next() {
            return nextNode();
          }
        };
    }
    
    public boolean contains(Object o) {
      return (o instanceof Map.Entry && LinkedHashTreeMap.this.findByEntry((Map.Entry<?, ?>)o) != null);
    }
    
    public boolean remove(Object o) {
      if (!(o instanceof Map.Entry))
        return false; 
      LinkedHashTreeMap.Node<K, V> node = LinkedHashTreeMap.this.findByEntry((Map.Entry<?, ?>)o);
      if (node == null)
        return false; 
      LinkedHashTreeMap.this.removeInternal(node, true);
      return true;
    }
    
    public void clear() {
      LinkedHashTreeMap.this.clear();
    }
  }
  
  final class KeySet extends AbstractSet<K> {
    public int size() {
      return LinkedHashTreeMap.this.size;
    }
    
    public Iterator<K> iterator() {
      return new LinkedHashTreeMap<K, V>.LinkedTreeMapIterator<K>() {
          public K next() {
            return (nextNode()).key;
          }
        };
    }
    
    public boolean contains(Object o) {
      return LinkedHashTreeMap.this.containsKey(o);
    }
    
    public boolean remove(Object key) {
      return (LinkedHashTreeMap.this.removeInternalByKey(key) != null);
    }
    
    public void clear() {
      LinkedHashTreeMap.this.clear();
    }
  }
  
  private Object writeReplace() throws ObjectStreamException {
    return new LinkedHashMap<K, V>(this);
  }
}

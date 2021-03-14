package org.apache.commons.collections4.bidimap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.OrderedBidiMap;
import org.apache.commons.collections4.OrderedIterator;
import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.iterators.EmptyOrderedMapIterator;
import org.apache.commons.collections4.keyvalue.UnmodifiableMapEntry;

public class TreeBidiMap<K extends Comparable<K>, V extends Comparable<V>> implements OrderedBidiMap<K, V>, Serializable {
  private static final long serialVersionUID = 721969328361807L;
  
  private transient Node<K, V>[] rootNode;
  
  enum DataElement {
    KEY("key"),
    VALUE("value");
    
    private final String description;
    
    DataElement(String description) {
      this.description = description;
    }
    
    public String toString() {
      return this.description;
    }
  }
  
  private transient int nodeCount = 0;
  
  private transient int modifications = 0;
  
  private transient Set<K> keySet;
  
  private transient Set<V> valuesSet;
  
  private transient Set<Map.Entry<K, V>> entrySet;
  
  private transient Inverse inverse = null;
  
  public TreeBidiMap() {
    this.rootNode = (Node<K, V>[])new Node[2];
  }
  
  public TreeBidiMap(Map<? extends K, ? extends V> map) {
    this();
    putAll(map);
  }
  
  public int size() {
    return this.nodeCount;
  }
  
  public boolean isEmpty() {
    return (this.nodeCount == 0);
  }
  
  public boolean containsKey(Object key) {
    checkKey(key);
    return (lookupKey(key) != null);
  }
  
  public boolean containsValue(Object value) {
    checkValue(value);
    return (lookupValue(value) != null);
  }
  
  public V get(Object key) {
    checkKey(key);
    Node<K, V> node = lookupKey(key);
    return (node == null) ? null : node.getValue();
  }
  
  public V put(K key, V value) {
    V result = get(key);
    doPut(key, value);
    return result;
  }
  
  public void putAll(Map<? extends K, ? extends V> map) {
    for (Map.Entry<? extends K, ? extends V> e : map.entrySet())
      put(e.getKey(), e.getValue()); 
  }
  
  public V remove(Object key) {
    return doRemoveKey(key);
  }
  
  public void clear() {
    modify();
    this.nodeCount = 0;
    this.rootNode[DataElement.KEY.ordinal()] = null;
    this.rootNode[DataElement.VALUE.ordinal()] = null;
  }
  
  public K getKey(Object value) {
    checkValue(value);
    Node<K, V> node = lookupValue(value);
    return (node == null) ? null : node.getKey();
  }
  
  public K removeValue(Object value) {
    return doRemoveValue(value);
  }
  
  public K firstKey() {
    if (this.nodeCount == 0)
      throw new NoSuchElementException("Map is empty"); 
    return leastNode(this.rootNode[DataElement.KEY.ordinal()], DataElement.KEY).getKey();
  }
  
  public K lastKey() {
    if (this.nodeCount == 0)
      throw new NoSuchElementException("Map is empty"); 
    return greatestNode(this.rootNode[DataElement.KEY.ordinal()], DataElement.KEY).getKey();
  }
  
  public K nextKey(K key) {
    checkKey(key);
    Node<K, V> node = nextGreater(lookupKey(key), DataElement.KEY);
    return (node == null) ? null : node.getKey();
  }
  
  public K previousKey(K key) {
    checkKey(key);
    Node<K, V> node = nextSmaller(lookupKey(key), DataElement.KEY);
    return (node == null) ? null : node.getKey();
  }
  
  public Set<K> keySet() {
    if (this.keySet == null)
      this.keySet = new KeyView(DataElement.KEY); 
    return this.keySet;
  }
  
  public Set<V> values() {
    if (this.valuesSet == null)
      this.valuesSet = new ValueView(DataElement.KEY); 
    return this.valuesSet;
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    if (this.entrySet == null)
      this.entrySet = new EntryView(); 
    return this.entrySet;
  }
  
  public OrderedMapIterator<K, V> mapIterator() {
    if (isEmpty())
      return EmptyOrderedMapIterator.emptyOrderedMapIterator(); 
    return new ViewMapIterator(DataElement.KEY);
  }
  
  public OrderedBidiMap<V, K> inverseBidiMap() {
    if (this.inverse == null)
      this.inverse = new Inverse(); 
    return this.inverse;
  }
  
  public boolean equals(Object obj) {
    return doEquals(obj, DataElement.KEY);
  }
  
  public int hashCode() {
    return doHashCode(DataElement.KEY);
  }
  
  public String toString() {
    return doToString(DataElement.KEY);
  }
  
  private void doPut(K key, V value) {
    checkKeyAndValue(key, value);
    doRemoveKey(key);
    doRemoveValue(value);
    Node<K, V> node = this.rootNode[DataElement.KEY.ordinal()];
    if (node == null) {
      Node<K, V> root = new Node<K, V>(key, value);
      this.rootNode[DataElement.KEY.ordinal()] = root;
      this.rootNode[DataElement.VALUE.ordinal()] = root;
      grow();
    } else {
      while (true) {
        int cmp = compare(key, node.getKey());
        if (cmp == 0)
          throw new IllegalArgumentException("Cannot store a duplicate key (\"" + key + "\") in this Map"); 
        if (cmp < 0) {
          if (node.getLeft(DataElement.KEY) != null) {
            node = node.getLeft(DataElement.KEY);
            continue;
          } 
          Node<K, V> node1 = new Node<K, V>(key, value);
          insertValue(node1);
          node.setLeft(node1, DataElement.KEY);
          node1.setParent(node, DataElement.KEY);
          doRedBlackInsert(node1, DataElement.KEY);
          grow();
          break;
        } 
        if (node.getRight(DataElement.KEY) != null) {
          node = node.getRight(DataElement.KEY);
          continue;
        } 
        Node<K, V> newNode = new Node<K, V>(key, value);
        insertValue(newNode);
        node.setRight(newNode, DataElement.KEY);
        newNode.setParent(node, DataElement.KEY);
        doRedBlackInsert(newNode, DataElement.KEY);
        grow();
        break;
      } 
    } 
  }
  
  private V doRemoveKey(Object key) {
    Node<K, V> node = lookupKey(key);
    if (node == null)
      return null; 
    doRedBlackDelete(node);
    return node.getValue();
  }
  
  private K doRemoveValue(Object value) {
    Node<K, V> node = lookupValue(value);
    if (node == null)
      return null; 
    doRedBlackDelete(node);
    return node.getKey();
  }
  
  private <T extends Comparable<T>> Node<K, V> lookup(Object data, DataElement dataElement) {
    Node<K, V> rval = null;
    Node<K, V> node = this.rootNode[dataElement.ordinal()];
    while (node != null) {
      int cmp = compare((Comparable)data, (Comparable)node.getData(dataElement));
      if (cmp == 0) {
        rval = node;
        break;
      } 
      node = (cmp < 0) ? node.getLeft(dataElement) : node.getRight(dataElement);
    } 
    return rval;
  }
  
  private Node<K, V> lookupKey(Object key) {
    return lookup(key, DataElement.KEY);
  }
  
  private Node<K, V> lookupValue(Object value) {
    return lookup(value, DataElement.VALUE);
  }
  
  private Node<K, V> nextGreater(Node<K, V> node, DataElement dataElement) {
    Node<K, V> rval;
    if (node == null) {
      rval = null;
    } else if (node.getRight(dataElement) != null) {
      rval = leastNode(node.getRight(dataElement), dataElement);
    } else {
      Node<K, V> parent = node.getParent(dataElement);
      Node<K, V> child = node;
      while (parent != null && child == parent.getRight(dataElement)) {
        child = parent;
        parent = parent.getParent(dataElement);
      } 
      rval = parent;
    } 
    return rval;
  }
  
  private Node<K, V> nextSmaller(Node<K, V> node, DataElement dataElement) {
    Node<K, V> rval;
    if (node == null) {
      rval = null;
    } else if (node.getLeft(dataElement) != null) {
      rval = greatestNode(node.getLeft(dataElement), dataElement);
    } else {
      Node<K, V> parent = node.getParent(dataElement);
      Node<K, V> child = node;
      while (parent != null && child == parent.getLeft(dataElement)) {
        child = parent;
        parent = parent.getParent(dataElement);
      } 
      rval = parent;
    } 
    return rval;
  }
  
  private static <T extends Comparable<T>> int compare(T o1, T o2) {
    return o1.compareTo(o2);
  }
  
  private Node<K, V> leastNode(Node<K, V> node, DataElement dataElement) {
    Node<K, V> rval = node;
    if (rval != null)
      while (rval.getLeft(dataElement) != null)
        rval = rval.getLeft(dataElement);  
    return rval;
  }
  
  private Node<K, V> greatestNode(Node<K, V> node, DataElement dataElement) {
    Node<K, V> rval = node;
    if (rval != null)
      while (rval.getRight(dataElement) != null)
        rval = rval.getRight(dataElement);  
    return rval;
  }
  
  private void copyColor(Node<K, V> from, Node<K, V> to, DataElement dataElement) {
    if (to != null)
      if (from == null) {
        to.setBlack(dataElement);
      } else {
        to.copyColor(from, dataElement);
      }  
  }
  
  private static boolean isRed(Node<?, ?> node, DataElement dataElement) {
    return (node != null && node.isRed(dataElement));
  }
  
  private static boolean isBlack(Node<?, ?> node, DataElement dataElement) {
    return (node == null || node.isBlack(dataElement));
  }
  
  private static void makeRed(Node<?, ?> node, DataElement dataElement) {
    if (node != null)
      node.setRed(dataElement); 
  }
  
  private static void makeBlack(Node<?, ?> node, DataElement dataElement) {
    if (node != null)
      node.setBlack(dataElement); 
  }
  
  private Node<K, V> getGrandParent(Node<K, V> node, DataElement dataElement) {
    return getParent(getParent(node, dataElement), dataElement);
  }
  
  private Node<K, V> getParent(Node<K, V> node, DataElement dataElement) {
    return (node == null) ? null : node.getParent(dataElement);
  }
  
  private Node<K, V> getRightChild(Node<K, V> node, DataElement dataElement) {
    return (node == null) ? null : node.getRight(dataElement);
  }
  
  private Node<K, V> getLeftChild(Node<K, V> node, DataElement dataElement) {
    return (node == null) ? null : node.getLeft(dataElement);
  }
  
  private void rotateLeft(Node<K, V> node, DataElement dataElement) {
    Node<K, V> rightChild = node.getRight(dataElement);
    node.setRight(rightChild.getLeft(dataElement), dataElement);
    if (rightChild.getLeft(dataElement) != null)
      rightChild.getLeft(dataElement).setParent(node, dataElement); 
    rightChild.setParent(node.getParent(dataElement), dataElement);
    if (node.getParent(dataElement) == null) {
      this.rootNode[dataElement.ordinal()] = rightChild;
    } else if (node.getParent(dataElement).getLeft(dataElement) == node) {
      node.getParent(dataElement).setLeft(rightChild, dataElement);
    } else {
      node.getParent(dataElement).setRight(rightChild, dataElement);
    } 
    rightChild.setLeft(node, dataElement);
    node.setParent(rightChild, dataElement);
  }
  
  private void rotateRight(Node<K, V> node, DataElement dataElement) {
    Node<K, V> leftChild = node.getLeft(dataElement);
    node.setLeft(leftChild.getRight(dataElement), dataElement);
    if (leftChild.getRight(dataElement) != null)
      leftChild.getRight(dataElement).setParent(node, dataElement); 
    leftChild.setParent(node.getParent(dataElement), dataElement);
    if (node.getParent(dataElement) == null) {
      this.rootNode[dataElement.ordinal()] = leftChild;
    } else if (node.getParent(dataElement).getRight(dataElement) == node) {
      node.getParent(dataElement).setRight(leftChild, dataElement);
    } else {
      node.getParent(dataElement).setLeft(leftChild, dataElement);
    } 
    leftChild.setRight(node, dataElement);
    node.setParent(leftChild, dataElement);
  }
  
  private void doRedBlackInsert(Node<K, V> insertedNode, DataElement dataElement) {
    Node<K, V> currentNode = insertedNode;
    makeRed(currentNode, dataElement);
    while (currentNode != null && currentNode != this.rootNode[dataElement.ordinal()] && isRed(currentNode.getParent(dataElement), dataElement)) {
      if (currentNode.isLeftChild(dataElement)) {
        Node<K, V> node = getRightChild(getGrandParent(currentNode, dataElement), dataElement);
        if (isRed(node, dataElement)) {
          makeBlack(getParent(currentNode, dataElement), dataElement);
          makeBlack(node, dataElement);
          makeRed(getGrandParent(currentNode, dataElement), dataElement);
          currentNode = getGrandParent(currentNode, dataElement);
          continue;
        } 
        if (currentNode.isRightChild(dataElement)) {
          currentNode = getParent(currentNode, dataElement);
          rotateLeft(currentNode, dataElement);
        } 
        makeBlack(getParent(currentNode, dataElement), dataElement);
        makeRed(getGrandParent(currentNode, dataElement), dataElement);
        if (getGrandParent(currentNode, dataElement) != null)
          rotateRight(getGrandParent(currentNode, dataElement), dataElement); 
        continue;
      } 
      Node<K, V> y = getLeftChild(getGrandParent(currentNode, dataElement), dataElement);
      if (isRed(y, dataElement)) {
        makeBlack(getParent(currentNode, dataElement), dataElement);
        makeBlack(y, dataElement);
        makeRed(getGrandParent(currentNode, dataElement), dataElement);
        currentNode = getGrandParent(currentNode, dataElement);
        continue;
      } 
      if (currentNode.isLeftChild(dataElement)) {
        currentNode = getParent(currentNode, dataElement);
        rotateRight(currentNode, dataElement);
      } 
      makeBlack(getParent(currentNode, dataElement), dataElement);
      makeRed(getGrandParent(currentNode, dataElement), dataElement);
      if (getGrandParent(currentNode, dataElement) != null)
        rotateLeft(getGrandParent(currentNode, dataElement), dataElement); 
    } 
    makeBlack(this.rootNode[dataElement.ordinal()], dataElement);
  }
  
  private void doRedBlackDelete(Node<K, V> deletedNode) {
    for (DataElement dataElement : DataElement.values()) {
      if (deletedNode.getLeft(dataElement) != null && deletedNode.getRight(dataElement) != null)
        swapPosition(nextGreater(deletedNode, dataElement), deletedNode, dataElement); 
      Node<K, V> replacement = (deletedNode.getLeft(dataElement) != null) ? deletedNode.getLeft(dataElement) : deletedNode.getRight(dataElement);
      if (replacement != null) {
        replacement.setParent(deletedNode.getParent(dataElement), dataElement);
        if (deletedNode.getParent(dataElement) == null) {
          this.rootNode[dataElement.ordinal()] = replacement;
        } else if (deletedNode == deletedNode.getParent(dataElement).getLeft(dataElement)) {
          deletedNode.getParent(dataElement).setLeft(replacement, dataElement);
        } else {
          deletedNode.getParent(dataElement).setRight(replacement, dataElement);
        } 
        deletedNode.setLeft(null, dataElement);
        deletedNode.setRight(null, dataElement);
        deletedNode.setParent(null, dataElement);
        if (isBlack(deletedNode, dataElement))
          doRedBlackDeleteFixup(replacement, dataElement); 
      } else if (deletedNode.getParent(dataElement) == null) {
        this.rootNode[dataElement.ordinal()] = null;
      } else {
        if (isBlack(deletedNode, dataElement))
          doRedBlackDeleteFixup(deletedNode, dataElement); 
        if (deletedNode.getParent(dataElement) != null) {
          if (deletedNode == deletedNode.getParent(dataElement).getLeft(dataElement)) {
            deletedNode.getParent(dataElement).setLeft(null, dataElement);
          } else {
            deletedNode.getParent(dataElement).setRight(null, dataElement);
          } 
          deletedNode.setParent(null, dataElement);
        } 
      } 
    } 
    shrink();
  }
  
  private void doRedBlackDeleteFixup(Node<K, V> replacementNode, DataElement dataElement) {
    Node<K, V> currentNode = replacementNode;
    while (currentNode != this.rootNode[dataElement.ordinal()] && isBlack(currentNode, dataElement)) {
      if (currentNode.isLeftChild(dataElement)) {
        Node<K, V> node = getRightChild(getParent(currentNode, dataElement), dataElement);
        if (isRed(node, dataElement)) {
          makeBlack(node, dataElement);
          makeRed(getParent(currentNode, dataElement), dataElement);
          rotateLeft(getParent(currentNode, dataElement), dataElement);
          node = getRightChild(getParent(currentNode, dataElement), dataElement);
        } 
        if (isBlack(getLeftChild(node, dataElement), dataElement) && isBlack(getRightChild(node, dataElement), dataElement)) {
          makeRed(node, dataElement);
          currentNode = getParent(currentNode, dataElement);
          continue;
        } 
        if (isBlack(getRightChild(node, dataElement), dataElement)) {
          makeBlack(getLeftChild(node, dataElement), dataElement);
          makeRed(node, dataElement);
          rotateRight(node, dataElement);
          node = getRightChild(getParent(currentNode, dataElement), dataElement);
        } 
        copyColor(getParent(currentNode, dataElement), node, dataElement);
        makeBlack(getParent(currentNode, dataElement), dataElement);
        makeBlack(getRightChild(node, dataElement), dataElement);
        rotateLeft(getParent(currentNode, dataElement), dataElement);
        currentNode = this.rootNode[dataElement.ordinal()];
        continue;
      } 
      Node<K, V> siblingNode = getLeftChild(getParent(currentNode, dataElement), dataElement);
      if (isRed(siblingNode, dataElement)) {
        makeBlack(siblingNode, dataElement);
        makeRed(getParent(currentNode, dataElement), dataElement);
        rotateRight(getParent(currentNode, dataElement), dataElement);
        siblingNode = getLeftChild(getParent(currentNode, dataElement), dataElement);
      } 
      if (isBlack(getRightChild(siblingNode, dataElement), dataElement) && isBlack(getLeftChild(siblingNode, dataElement), dataElement)) {
        makeRed(siblingNode, dataElement);
        currentNode = getParent(currentNode, dataElement);
        continue;
      } 
      if (isBlack(getLeftChild(siblingNode, dataElement), dataElement)) {
        makeBlack(getRightChild(siblingNode, dataElement), dataElement);
        makeRed(siblingNode, dataElement);
        rotateLeft(siblingNode, dataElement);
        siblingNode = getLeftChild(getParent(currentNode, dataElement), dataElement);
      } 
      copyColor(getParent(currentNode, dataElement), siblingNode, dataElement);
      makeBlack(getParent(currentNode, dataElement), dataElement);
      makeBlack(getLeftChild(siblingNode, dataElement), dataElement);
      rotateRight(getParent(currentNode, dataElement), dataElement);
      currentNode = this.rootNode[dataElement.ordinal()];
    } 
    makeBlack(currentNode, dataElement);
  }
  
  private void swapPosition(Node<K, V> x, Node<K, V> y, DataElement dataElement) {
    Node<K, V> xFormerParent = x.getParent(dataElement);
    Node<K, V> xFormerLeftChild = x.getLeft(dataElement);
    Node<K, V> xFormerRightChild = x.getRight(dataElement);
    Node<K, V> yFormerParent = y.getParent(dataElement);
    Node<K, V> yFormerLeftChild = y.getLeft(dataElement);
    Node<K, V> yFormerRightChild = y.getRight(dataElement);
    boolean xWasLeftChild = (x.getParent(dataElement) != null && x == x.getParent(dataElement).getLeft(dataElement));
    boolean yWasLeftChild = (y.getParent(dataElement) != null && y == y.getParent(dataElement).getLeft(dataElement));
    if (x == yFormerParent) {
      x.setParent(y, dataElement);
      if (yWasLeftChild) {
        y.setLeft(x, dataElement);
        y.setRight(xFormerRightChild, dataElement);
      } else {
        y.setRight(x, dataElement);
        y.setLeft(xFormerLeftChild, dataElement);
      } 
    } else {
      x.setParent(yFormerParent, dataElement);
      if (yFormerParent != null)
        if (yWasLeftChild) {
          yFormerParent.setLeft(x, dataElement);
        } else {
          yFormerParent.setRight(x, dataElement);
        }  
      y.setLeft(xFormerLeftChild, dataElement);
      y.setRight(xFormerRightChild, dataElement);
    } 
    if (y == xFormerParent) {
      y.setParent(x, dataElement);
      if (xWasLeftChild) {
        x.setLeft(y, dataElement);
        x.setRight(yFormerRightChild, dataElement);
      } else {
        x.setRight(y, dataElement);
        x.setLeft(yFormerLeftChild, dataElement);
      } 
    } else {
      y.setParent(xFormerParent, dataElement);
      if (xFormerParent != null)
        if (xWasLeftChild) {
          xFormerParent.setLeft(y, dataElement);
        } else {
          xFormerParent.setRight(y, dataElement);
        }  
      x.setLeft(yFormerLeftChild, dataElement);
      x.setRight(yFormerRightChild, dataElement);
    } 
    if (x.getLeft(dataElement) != null)
      x.getLeft(dataElement).setParent(x, dataElement); 
    if (x.getRight(dataElement) != null)
      x.getRight(dataElement).setParent(x, dataElement); 
    if (y.getLeft(dataElement) != null)
      y.getLeft(dataElement).setParent(y, dataElement); 
    if (y.getRight(dataElement) != null)
      y.getRight(dataElement).setParent(y, dataElement); 
    x.swapColors(y, dataElement);
    if (this.rootNode[dataElement.ordinal()] == x) {
      this.rootNode[dataElement.ordinal()] = y;
    } else if (this.rootNode[dataElement.ordinal()] == y) {
      this.rootNode[dataElement.ordinal()] = x;
    } 
  }
  
  private static void checkNonNullComparable(Object o, DataElement dataElement) {
    if (o == null)
      throw new NullPointerException(dataElement + " cannot be null"); 
    if (!(o instanceof Comparable))
      throw new ClassCastException(dataElement + " must be Comparable"); 
  }
  
  private static void checkKey(Object key) {
    checkNonNullComparable(key, DataElement.KEY);
  }
  
  private static void checkValue(Object value) {
    checkNonNullComparable(value, DataElement.VALUE);
  }
  
  private static void checkKeyAndValue(Object key, Object value) {
    checkKey(key);
    checkValue(value);
  }
  
  private void modify() {
    this.modifications++;
  }
  
  private void grow() {
    modify();
    this.nodeCount++;
  }
  
  private void shrink() {
    modify();
    this.nodeCount--;
  }
  
  private void insertValue(Node<K, V> newNode) throws IllegalArgumentException {
    Node<K, V> node = this.rootNode[DataElement.VALUE.ordinal()];
    while (true) {
      int cmp = compare((Comparable)newNode.getValue(), (Comparable)node.getValue());
      if (cmp == 0)
        throw new IllegalArgumentException("Cannot store a duplicate value (\"" + newNode.getData(DataElement.VALUE) + "\") in this Map"); 
      if (cmp < 0) {
        if (node.getLeft(DataElement.VALUE) != null) {
          node = node.getLeft(DataElement.VALUE);
          continue;
        } 
        node.setLeft(newNode, DataElement.VALUE);
        newNode.setParent(node, DataElement.VALUE);
        doRedBlackInsert(newNode, DataElement.VALUE);
        break;
      } 
      if (node.getRight(DataElement.VALUE) != null) {
        node = node.getRight(DataElement.VALUE);
        continue;
      } 
      node.setRight(newNode, DataElement.VALUE);
      newNode.setParent(node, DataElement.VALUE);
      doRedBlackInsert(newNode, DataElement.VALUE);
      break;
    } 
  }
  
  private boolean doEquals(Object obj, DataElement dataElement) {
    if (obj == this)
      return true; 
    if (!(obj instanceof Map))
      return false; 
    Map<?, ?> other = (Map<?, ?>)obj;
    if (other.size() != size())
      return false; 
    if (this.nodeCount > 0)
      try {
        for (MapIterator<?, ?> it = getMapIterator(dataElement); it.hasNext(); ) {
          Object key = it.next();
          Object value = it.getValue();
          if (!value.equals(other.get(key)))
            return false; 
        } 
      } catch (ClassCastException ex) {
        return false;
      } catch (NullPointerException ex) {
        return false;
      }  
    return true;
  }
  
  private int doHashCode(DataElement dataElement) {
    int total = 0;
    if (this.nodeCount > 0)
      for (MapIterator<?, ?> it = getMapIterator(dataElement); it.hasNext(); ) {
        Object key = it.next();
        Object value = it.getValue();
        total += key.hashCode() ^ value.hashCode();
      }  
    return total;
  }
  
  private String doToString(DataElement dataElement) {
    if (this.nodeCount == 0)
      return "{}"; 
    StringBuilder buf = new StringBuilder(this.nodeCount * 32);
    buf.append('{');
    MapIterator<?, ?> it = getMapIterator(dataElement);
    boolean hasNext = it.hasNext();
    while (hasNext) {
      Object key = it.next();
      Object value = it.getValue();
      buf.append((key == this) ? "(this Map)" : key).append('=').append((value == this) ? "(this Map)" : value);
      hasNext = it.hasNext();
      if (hasNext)
        buf.append(", "); 
    } 
    buf.append('}');
    return buf.toString();
  }
  
  private MapIterator<?, ?> getMapIterator(DataElement dataElement) {
    switch (dataElement) {
      case KEY:
        return (MapIterator<?, ?>)new ViewMapIterator(DataElement.KEY);
      case VALUE:
        return (MapIterator<?, ?>)new InverseViewMapIterator(DataElement.VALUE);
    } 
    throw new IllegalArgumentException();
  }
  
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    this.rootNode = (Node<K, V>[])new Node[2];
    int size = stream.readInt();
    for (int i = 0; i < size; i++) {
      Comparable comparable1 = (Comparable)stream.readObject();
      Comparable comparable2 = (Comparable)stream.readObject();
      put((K)comparable1, (V)comparable2);
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
  
  abstract class View<E> extends AbstractSet<E> {
    final TreeBidiMap.DataElement orderType;
    
    View(TreeBidiMap.DataElement orderType) {
      this.orderType = orderType;
    }
    
    public int size() {
      return TreeBidiMap.this.size();
    }
    
    public void clear() {
      TreeBidiMap.this.clear();
    }
  }
  
  class KeyView extends View<K> {
    public KeyView(TreeBidiMap.DataElement orderType) {
      super(orderType);
    }
    
    public Iterator<K> iterator() {
      return (Iterator<K>)new TreeBidiMap.ViewMapIterator(this.orderType);
    }
    
    public boolean contains(Object obj) {
      TreeBidiMap.checkNonNullComparable(obj, TreeBidiMap.DataElement.KEY);
      return (TreeBidiMap.this.lookupKey(obj) != null);
    }
    
    public boolean remove(Object o) {
      return (TreeBidiMap.this.doRemoveKey(o) != null);
    }
  }
  
  class ValueView extends View<V> {
    public ValueView(TreeBidiMap.DataElement orderType) {
      super(orderType);
    }
    
    public Iterator<V> iterator() {
      return (Iterator<V>)new TreeBidiMap.InverseViewMapIterator(this.orderType);
    }
    
    public boolean contains(Object obj) {
      TreeBidiMap.checkNonNullComparable(obj, TreeBidiMap.DataElement.VALUE);
      return (TreeBidiMap.this.lookupValue(obj) != null);
    }
    
    public boolean remove(Object o) {
      return (TreeBidiMap.this.doRemoveValue(o) != null);
    }
  }
  
  class EntryView extends View<Map.Entry<K, V>> {
    EntryView() {
      super(TreeBidiMap.DataElement.KEY);
    }
    
    public boolean contains(Object obj) {
      if (!(obj instanceof Map.Entry))
        return false; 
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
      Object value = entry.getValue();
      TreeBidiMap.Node<K, V> node = TreeBidiMap.this.lookupKey(entry.getKey());
      return (node != null && node.getValue().equals(value));
    }
    
    public boolean remove(Object obj) {
      if (!(obj instanceof Map.Entry))
        return false; 
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
      Object value = entry.getValue();
      TreeBidiMap.Node<K, V> node = TreeBidiMap.this.lookupKey(entry.getKey());
      if (node != null && node.getValue().equals(value)) {
        TreeBidiMap.this.doRedBlackDelete(node);
        return true;
      } 
      return false;
    }
    
    public Iterator<Map.Entry<K, V>> iterator() {
      return (Iterator<Map.Entry<K, V>>)new TreeBidiMap.ViewMapEntryIterator();
    }
  }
  
  class InverseEntryView extends View<Map.Entry<V, K>> {
    InverseEntryView() {
      super(TreeBidiMap.DataElement.VALUE);
    }
    
    public boolean contains(Object obj) {
      if (!(obj instanceof Map.Entry))
        return false; 
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
      Object value = entry.getValue();
      TreeBidiMap.Node<K, V> node = TreeBidiMap.this.lookupValue(entry.getKey());
      return (node != null && node.getKey().equals(value));
    }
    
    public boolean remove(Object obj) {
      if (!(obj instanceof Map.Entry))
        return false; 
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
      Object value = entry.getValue();
      TreeBidiMap.Node<K, V> node = TreeBidiMap.this.lookupValue(entry.getKey());
      if (node != null && node.getKey().equals(value)) {
        TreeBidiMap.this.doRedBlackDelete(node);
        return true;
      } 
      return false;
    }
    
    public Iterator<Map.Entry<V, K>> iterator() {
      return (Iterator<Map.Entry<V, K>>)new TreeBidiMap.InverseViewMapEntryIterator();
    }
  }
  
  abstract class ViewIterator {
    private final TreeBidiMap.DataElement orderType;
    
    TreeBidiMap.Node<K, V> lastReturnedNode;
    
    private TreeBidiMap.Node<K, V> nextNode;
    
    private TreeBidiMap.Node<K, V> previousNode;
    
    private int expectedModifications;
    
    ViewIterator(TreeBidiMap.DataElement orderType) {
      this.orderType = orderType;
      this.expectedModifications = TreeBidiMap.this.modifications;
      this.nextNode = TreeBidiMap.this.leastNode(TreeBidiMap.this.rootNode[orderType.ordinal()], orderType);
      this.lastReturnedNode = null;
      this.previousNode = null;
    }
    
    public final boolean hasNext() {
      return (this.nextNode != null);
    }
    
    protected TreeBidiMap.Node<K, V> navigateNext() {
      if (this.nextNode == null)
        throw new NoSuchElementException(); 
      if (TreeBidiMap.this.modifications != this.expectedModifications)
        throw new ConcurrentModificationException(); 
      this.lastReturnedNode = this.nextNode;
      this.previousNode = this.nextNode;
      this.nextNode = TreeBidiMap.this.nextGreater(this.nextNode, this.orderType);
      return this.lastReturnedNode;
    }
    
    public boolean hasPrevious() {
      return (this.previousNode != null);
    }
    
    protected TreeBidiMap.Node<K, V> navigatePrevious() {
      if (this.previousNode == null)
        throw new NoSuchElementException(); 
      if (TreeBidiMap.this.modifications != this.expectedModifications)
        throw new ConcurrentModificationException(); 
      this.nextNode = this.lastReturnedNode;
      if (this.nextNode == null)
        this.nextNode = TreeBidiMap.this.nextGreater(this.previousNode, this.orderType); 
      this.lastReturnedNode = this.previousNode;
      this.previousNode = TreeBidiMap.this.nextSmaller(this.previousNode, this.orderType);
      return this.lastReturnedNode;
    }
    
    public final void remove() {
      if (this.lastReturnedNode == null)
        throw new IllegalStateException(); 
      if (TreeBidiMap.this.modifications != this.expectedModifications)
        throw new ConcurrentModificationException(); 
      TreeBidiMap.this.doRedBlackDelete(this.lastReturnedNode);
      this.expectedModifications++;
      this.lastReturnedNode = null;
      if (this.nextNode == null) {
        this.previousNode = TreeBidiMap.this.greatestNode(TreeBidiMap.this.rootNode[this.orderType.ordinal()], this.orderType);
      } else {
        this.previousNode = TreeBidiMap.this.nextSmaller(this.nextNode, this.orderType);
      } 
    }
  }
  
  class ViewMapIterator extends ViewIterator implements OrderedMapIterator<K, V> {
    ViewMapIterator(TreeBidiMap.DataElement orderType) {
      super(orderType);
    }
    
    public K getKey() {
      if (this.lastReturnedNode == null)
        throw new IllegalStateException("Iterator getKey() can only be called after next() and before remove()"); 
      return this.lastReturnedNode.getKey();
    }
    
    public V getValue() {
      if (this.lastReturnedNode == null)
        throw new IllegalStateException("Iterator getValue() can only be called after next() and before remove()"); 
      return this.lastReturnedNode.getValue();
    }
    
    public V setValue(V obj) {
      throw new UnsupportedOperationException();
    }
    
    public K next() {
      return navigateNext().getKey();
    }
    
    public K previous() {
      return navigatePrevious().getKey();
    }
  }
  
  class InverseViewMapIterator extends ViewIterator implements OrderedMapIterator<V, K> {
    public InverseViewMapIterator(TreeBidiMap.DataElement orderType) {
      super(orderType);
    }
    
    public V getKey() {
      if (this.lastReturnedNode == null)
        throw new IllegalStateException("Iterator getKey() can only be called after next() and before remove()"); 
      return this.lastReturnedNode.getValue();
    }
    
    public K getValue() {
      if (this.lastReturnedNode == null)
        throw new IllegalStateException("Iterator getValue() can only be called after next() and before remove()"); 
      return this.lastReturnedNode.getKey();
    }
    
    public K setValue(K obj) {
      throw new UnsupportedOperationException();
    }
    
    public V next() {
      return navigateNext().getValue();
    }
    
    public V previous() {
      return navigatePrevious().getValue();
    }
  }
  
  class ViewMapEntryIterator extends ViewIterator implements OrderedIterator<Map.Entry<K, V>> {
    ViewMapEntryIterator() {
      super(TreeBidiMap.DataElement.KEY);
    }
    
    public Map.Entry<K, V> next() {
      return navigateNext();
    }
    
    public Map.Entry<K, V> previous() {
      return navigatePrevious();
    }
  }
  
  class InverseViewMapEntryIterator extends ViewIterator implements OrderedIterator<Map.Entry<V, K>> {
    InverseViewMapEntryIterator() {
      super(TreeBidiMap.DataElement.VALUE);
    }
    
    public Map.Entry<V, K> next() {
      return createEntry(navigateNext());
    }
    
    public Map.Entry<V, K> previous() {
      return createEntry(navigatePrevious());
    }
    
    private Map.Entry<V, K> createEntry(TreeBidiMap.Node<K, V> node) {
      return (Map.Entry<V, K>)new UnmodifiableMapEntry(node.getValue(), node.getKey());
    }
  }
  
  static class Node<K extends Comparable<K>, V extends Comparable<V>> implements Map.Entry<K, V>, KeyValue<K, V> {
    private final K key;
    
    private final V value;
    
    private final Node<K, V>[] leftNode;
    
    private final Node<K, V>[] rightNode;
    
    private final Node<K, V>[] parentNode;
    
    private final boolean[] blackColor;
    
    private int hashcodeValue;
    
    private boolean calculatedHashCode;
    
    Node(K key, V value) {
      this.key = key;
      this.value = value;
      this.leftNode = (Node<K, V>[])new Node[2];
      this.rightNode = (Node<K, V>[])new Node[2];
      this.parentNode = (Node<K, V>[])new Node[2];
      this.blackColor = new boolean[] { true, true };
      this.calculatedHashCode = false;
    }
    
    private Object getData(TreeBidiMap.DataElement dataElement) {
      switch (dataElement) {
        case KEY:
          return getKey();
        case VALUE:
          return getValue();
      } 
      throw new IllegalArgumentException();
    }
    
    private void setLeft(Node<K, V> node, TreeBidiMap.DataElement dataElement) {
      this.leftNode[dataElement.ordinal()] = node;
    }
    
    private Node<K, V> getLeft(TreeBidiMap.DataElement dataElement) {
      return this.leftNode[dataElement.ordinal()];
    }
    
    private void setRight(Node<K, V> node, TreeBidiMap.DataElement dataElement) {
      this.rightNode[dataElement.ordinal()] = node;
    }
    
    private Node<K, V> getRight(TreeBidiMap.DataElement dataElement) {
      return this.rightNode[dataElement.ordinal()];
    }
    
    private void setParent(Node<K, V> node, TreeBidiMap.DataElement dataElement) {
      this.parentNode[dataElement.ordinal()] = node;
    }
    
    private Node<K, V> getParent(TreeBidiMap.DataElement dataElement) {
      return this.parentNode[dataElement.ordinal()];
    }
    
    private void swapColors(Node<K, V> node, TreeBidiMap.DataElement dataElement) {
      this.blackColor[dataElement.ordinal()] = this.blackColor[dataElement.ordinal()] ^ node.blackColor[dataElement.ordinal()];
      node.blackColor[dataElement.ordinal()] = node.blackColor[dataElement.ordinal()] ^ this.blackColor[dataElement.ordinal()];
      this.blackColor[dataElement.ordinal()] = this.blackColor[dataElement.ordinal()] ^ node.blackColor[dataElement.ordinal()];
    }
    
    private boolean isBlack(TreeBidiMap.DataElement dataElement) {
      return this.blackColor[dataElement.ordinal()];
    }
    
    private boolean isRed(TreeBidiMap.DataElement dataElement) {
      return !this.blackColor[dataElement.ordinal()];
    }
    
    private void setBlack(TreeBidiMap.DataElement dataElement) {
      this.blackColor[dataElement.ordinal()] = true;
    }
    
    private void setRed(TreeBidiMap.DataElement dataElement) {
      this.blackColor[dataElement.ordinal()] = false;
    }
    
    private void copyColor(Node<K, V> node, TreeBidiMap.DataElement dataElement) {
      this.blackColor[dataElement.ordinal()] = node.blackColor[dataElement.ordinal()];
    }
    
    private boolean isLeftChild(TreeBidiMap.DataElement dataElement) {
      return (this.parentNode[dataElement.ordinal()] != null && (this.parentNode[dataElement.ordinal()]).leftNode[dataElement.ordinal()] == this);
    }
    
    private boolean isRightChild(TreeBidiMap.DataElement dataElement) {
      return (this.parentNode[dataElement.ordinal()] != null && (this.parentNode[dataElement.ordinal()]).rightNode[dataElement.ordinal()] == this);
    }
    
    public K getKey() {
      return this.key;
    }
    
    public V getValue() {
      return this.value;
    }
    
    public V setValue(V ignored) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("Map.Entry.setValue is not supported");
    }
    
    public boolean equals(Object obj) {
      if (obj == this)
        return true; 
      if (!(obj instanceof Map.Entry))
        return false; 
      Map.Entry<?, ?> e = (Map.Entry<?, ?>)obj;
      return (getKey().equals(e.getKey()) && getValue().equals(e.getValue()));
    }
    
    public int hashCode() {
      if (!this.calculatedHashCode) {
        this.hashcodeValue = getKey().hashCode() ^ getValue().hashCode();
        this.calculatedHashCode = true;
      } 
      return this.hashcodeValue;
    }
  }
  
  class Inverse implements OrderedBidiMap<V, K> {
    private Set<V> inverseKeySet;
    
    private Set<K> inverseValuesSet;
    
    private Set<Map.Entry<V, K>> inverseEntrySet;
    
    public int size() {
      return TreeBidiMap.this.size();
    }
    
    public boolean isEmpty() {
      return TreeBidiMap.this.isEmpty();
    }
    
    public K get(Object key) {
      return (K)TreeBidiMap.this.getKey(key);
    }
    
    public V getKey(Object value) {
      return (V)TreeBidiMap.this.get(value);
    }
    
    public boolean containsKey(Object key) {
      return TreeBidiMap.this.containsValue(key);
    }
    
    public boolean containsValue(Object value) {
      return TreeBidiMap.this.containsKey(value);
    }
    
    public V firstKey() {
      if (TreeBidiMap.this.nodeCount == 0)
        throw new NoSuchElementException("Map is empty"); 
      return (V)TreeBidiMap.this.leastNode(TreeBidiMap.this.rootNode[TreeBidiMap.DataElement.VALUE.ordinal()], TreeBidiMap.DataElement.VALUE).getValue();
    }
    
    public V lastKey() {
      if (TreeBidiMap.this.nodeCount == 0)
        throw new NoSuchElementException("Map is empty"); 
      return (V)TreeBidiMap.this.greatestNode(TreeBidiMap.this.rootNode[TreeBidiMap.DataElement.VALUE.ordinal()], TreeBidiMap.DataElement.VALUE).getValue();
    }
    
    public V nextKey(V key) {
      TreeBidiMap.checkKey(key);
      TreeBidiMap.Node<K, V> node = TreeBidiMap.this.nextGreater(TreeBidiMap.this.lookup(key, TreeBidiMap.DataElement.VALUE), TreeBidiMap.DataElement.VALUE);
      return (node == null) ? null : node.getValue();
    }
    
    public V previousKey(V key) {
      TreeBidiMap.checkKey(key);
      TreeBidiMap.Node<K, V> node = TreeBidiMap.this.nextSmaller(TreeBidiMap.this.lookup(key, TreeBidiMap.DataElement.VALUE), TreeBidiMap.DataElement.VALUE);
      return (node == null) ? null : node.getValue();
    }
    
    public K put(V key, K value) {
      K result = get(key);
      TreeBidiMap.this.doPut(value, key);
      return result;
    }
    
    public void putAll(Map<? extends V, ? extends K> map) {
      for (Map.Entry<? extends V, ? extends K> e : map.entrySet())
        put(e.getKey(), e.getValue()); 
    }
    
    public K remove(Object key) {
      return (K)TreeBidiMap.this.removeValue(key);
    }
    
    public V removeValue(Object value) {
      return (V)TreeBidiMap.this.remove(value);
    }
    
    public void clear() {
      TreeBidiMap.this.clear();
    }
    
    public Set<V> keySet() {
      if (this.inverseKeySet == null)
        this.inverseKeySet = new TreeBidiMap.ValueView(TreeBidiMap.DataElement.VALUE); 
      return this.inverseKeySet;
    }
    
    public Set<K> values() {
      if (this.inverseValuesSet == null)
        this.inverseValuesSet = new TreeBidiMap.KeyView(TreeBidiMap.DataElement.VALUE); 
      return this.inverseValuesSet;
    }
    
    public Set<Map.Entry<V, K>> entrySet() {
      if (this.inverseEntrySet == null)
        this.inverseEntrySet = new TreeBidiMap.InverseEntryView(); 
      return this.inverseEntrySet;
    }
    
    public OrderedMapIterator<V, K> mapIterator() {
      if (isEmpty())
        return EmptyOrderedMapIterator.emptyOrderedMapIterator(); 
      return new TreeBidiMap.InverseViewMapIterator(TreeBidiMap.DataElement.VALUE);
    }
    
    public OrderedBidiMap<K, V> inverseBidiMap() {
      return TreeBidiMap.this;
    }
    
    public boolean equals(Object obj) {
      return TreeBidiMap.this.doEquals(obj, TreeBidiMap.DataElement.VALUE);
    }
    
    public int hashCode() {
      return TreeBidiMap.this.doHashCode(TreeBidiMap.DataElement.VALUE);
    }
    
    public String toString() {
      return TreeBidiMap.this.doToString(TreeBidiMap.DataElement.VALUE);
    }
  }
}

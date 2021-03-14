package org.apache.commons.collections4.collection;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.collections4.list.UnmodifiableList;

public class CompositeCollection<E> implements Collection<E>, Serializable {
  private static final long serialVersionUID = 8417515734108306801L;
  
  private CollectionMutator<E> mutator;
  
  private final List<Collection<E>> all = new ArrayList<Collection<E>>();
  
  public CompositeCollection() {}
  
  public CompositeCollection(Collection<E> compositeCollection) {
    addComposited(compositeCollection);
  }
  
  public CompositeCollection(Collection<E> compositeCollection1, Collection<E> compositeCollection2) {
    addComposited(compositeCollection1, compositeCollection2);
  }
  
  public CompositeCollection(Collection<E>... compositeCollections) {
    addComposited(compositeCollections);
  }
  
  public int size() {
    int size = 0;
    for (Collection<E> item : this.all)
      size += item.size(); 
    return size;
  }
  
  public boolean isEmpty() {
    for (Collection<E> item : this.all) {
      if (!item.isEmpty())
        return false; 
    } 
    return true;
  }
  
  public boolean contains(Object obj) {
    for (Collection<E> item : this.all) {
      if (item.contains(obj))
        return true; 
    } 
    return false;
  }
  
  public Iterator<E> iterator() {
    if (this.all.isEmpty())
      return EmptyIterator.emptyIterator(); 
    IteratorChain<E> chain = new IteratorChain();
    for (Collection<E> item : this.all)
      chain.addIterator(item.iterator()); 
    return (Iterator<E>)chain;
  }
  
  public Object[] toArray() {
    Object[] result = new Object[size()];
    int i = 0;
    for (Iterator<E> it = iterator(); it.hasNext(); i++)
      result[i] = it.next(); 
    return result;
  }
  
  public <T> T[] toArray(T[] array) {
    int size = size();
    Object[] result = null;
    if (array.length >= size) {
      T[] arrayOfT = array;
    } else {
      result = (Object[])Array.newInstance(array.getClass().getComponentType(), size);
    } 
    int offset = 0;
    for (Collection<E> item : this.all) {
      for (E e : item)
        result[offset++] = e; 
    } 
    if (result.length > size)
      result[size] = null; 
    return (T[])result;
  }
  
  public boolean add(E obj) {
    if (this.mutator == null)
      throw new UnsupportedOperationException("add() is not supported on CompositeCollection without a CollectionMutator strategy"); 
    return this.mutator.add(this, this.all, obj);
  }
  
  public boolean remove(Object obj) {
    if (this.mutator == null)
      throw new UnsupportedOperationException("remove() is not supported on CompositeCollection without a CollectionMutator strategy"); 
    return this.mutator.remove(this, this.all, obj);
  }
  
  public boolean containsAll(Collection<?> coll) {
    for (Object item : coll) {
      if (!contains(item))
        return false; 
    } 
    return true;
  }
  
  public boolean addAll(Collection<? extends E> coll) {
    if (this.mutator == null)
      throw new UnsupportedOperationException("addAll() is not supported on CompositeCollection without a CollectionMutator strategy"); 
    return this.mutator.addAll(this, this.all, coll);
  }
  
  public boolean removeAll(Collection<?> coll) {
    if (coll.size() == 0)
      return false; 
    boolean changed = false;
    for (Collection<E> item : this.all)
      changed |= item.removeAll(coll); 
    return changed;
  }
  
  public boolean retainAll(Collection<?> coll) {
    boolean changed = false;
    for (Collection<E> item : this.all)
      changed |= item.retainAll(coll); 
    return changed;
  }
  
  public void clear() {
    for (Collection<E> coll : this.all)
      coll.clear(); 
  }
  
  public void setMutator(CollectionMutator<E> mutator) {
    this.mutator = mutator;
  }
  
  public void addComposited(Collection<E> compositeCollection) {
    this.all.add(compositeCollection);
  }
  
  public void addComposited(Collection<E> compositeCollection1, Collection<E> compositeCollection2) {
    this.all.add(compositeCollection1);
    this.all.add(compositeCollection2);
  }
  
  public void addComposited(Collection<E>... compositeCollections) {
    this.all.addAll(Arrays.asList(compositeCollections));
  }
  
  public void removeComposited(Collection<E> coll) {
    this.all.remove(coll);
  }
  
  public Collection<E> toCollection() {
    return new ArrayList<E>(this);
  }
  
  public List<Collection<E>> getCollections() {
    return UnmodifiableList.unmodifiableList(this.all);
  }
  
  protected CollectionMutator<E> getMutator() {
    return this.mutator;
  }
  
  public static interface CollectionMutator<E> extends Serializable {
    boolean add(CompositeCollection<E> param1CompositeCollection, List<Collection<E>> param1List, E param1E);
    
    boolean addAll(CompositeCollection<E> param1CompositeCollection, List<Collection<E>> param1List, Collection<? extends E> param1Collection);
    
    boolean remove(CompositeCollection<E> param1CompositeCollection, List<Collection<E>> param1List, Object param1Object);
  }
}

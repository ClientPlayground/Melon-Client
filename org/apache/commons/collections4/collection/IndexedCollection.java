package org.apache.commons.collections4.collection;

import java.util.Collection;
import java.util.HashMap;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.map.MultiValueMap;

public class IndexedCollection<K, C> extends AbstractCollectionDecorator<C> {
  private static final long serialVersionUID = -5512610452568370038L;
  
  private final Transformer<C, K> keyTransformer;
  
  private final MultiMap<K, C> index;
  
  private final boolean uniqueIndex;
  
  public static <K, C> IndexedCollection<K, C> uniqueIndexedCollection(Collection<C> coll, Transformer<C, K> keyTransformer) {
    return new IndexedCollection<K, C>(coll, keyTransformer, (MultiMap<K, C>)MultiValueMap.multiValueMap(new HashMap<Object, Object>()), true);
  }
  
  public static <K, C> IndexedCollection<K, C> nonUniqueIndexedCollection(Collection<C> coll, Transformer<C, K> keyTransformer) {
    return new IndexedCollection<K, C>(coll, keyTransformer, (MultiMap<K, C>)MultiValueMap.multiValueMap(new HashMap<Object, Object>()), false);
  }
  
  public IndexedCollection(Collection<C> coll, Transformer<C, K> keyTransformer, MultiMap<K, C> map, boolean uniqueIndex) {
    super(coll);
    this.keyTransformer = keyTransformer;
    this.index = map;
    this.uniqueIndex = uniqueIndex;
    reindex();
  }
  
  public boolean add(C object) {
    boolean added = super.add(object);
    if (added)
      addToIndex(object); 
    return added;
  }
  
  public boolean addAll(Collection<? extends C> coll) {
    boolean changed = false;
    for (C c : coll)
      changed |= add(c); 
    return changed;
  }
  
  public void clear() {
    super.clear();
    this.index.clear();
  }
  
  public boolean contains(Object object) {
    return this.index.containsKey(this.keyTransformer.transform(object));
  }
  
  public boolean containsAll(Collection<?> coll) {
    for (Object o : coll) {
      if (!contains(o))
        return false; 
    } 
    return true;
  }
  
  public C get(K key) {
    Collection<C> coll = (Collection<C>)this.index.get(key);
    return (coll == null) ? null : coll.iterator().next();
  }
  
  public Collection<C> values(K key) {
    return (Collection<C>)this.index.get(key);
  }
  
  public void reindex() {
    this.index.clear();
    for (C c : decorated())
      addToIndex(c); 
  }
  
  public boolean remove(Object object) {
    boolean removed = super.remove(object);
    if (removed)
      removeFromIndex((C)object); 
    return removed;
  }
  
  public boolean removeAll(Collection<?> coll) {
    boolean changed = false;
    for (Object o : coll)
      changed |= remove(o); 
    return changed;
  }
  
  public boolean retainAll(Collection<?> coll) {
    boolean changed = super.retainAll(coll);
    if (changed)
      reindex(); 
    return changed;
  }
  
  private void addToIndex(C object) {
    K key = (K)this.keyTransformer.transform(object);
    if (this.uniqueIndex && this.index.containsKey(key))
      throw new IllegalArgumentException("Duplicate key in uniquely indexed collection."); 
    this.index.put(key, object);
  }
  
  private void removeFromIndex(C object) {
    this.index.remove(this.keyTransformer.transform(object));
  }
}

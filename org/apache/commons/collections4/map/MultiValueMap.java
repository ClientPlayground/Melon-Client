package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.FunctorException;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.collections4.iterators.LazyIteratorChain;
import org.apache.commons.collections4.iterators.TransformIterator;

public class MultiValueMap<K, V> extends AbstractMapDecorator<K, Object> implements MultiMap<K, V>, Serializable {
  private static final long serialVersionUID = -2214159910087182007L;
  
  private final Factory<? extends Collection<V>> collectionFactory;
  
  private transient Collection<V> valuesView;
  
  public static <K, V> MultiValueMap<K, V> multiValueMap(Map<K, ? super Collection<V>> map) {
    return multiValueMap(map, (Class)ArrayList.class);
  }
  
  public static <K, V, C extends Collection<V>> MultiValueMap<K, V> multiValueMap(Map<K, ? super C> map, Class<C> collectionClass) {
    return new MultiValueMap<K, V>(map, new ReflectionFactory<C>(collectionClass));
  }
  
  public static <K, V, C extends Collection<V>> MultiValueMap<K, V> multiValueMap(Map<K, ? super C> map, Factory<C> collectionFactory) {
    return new MultiValueMap<K, V>(map, collectionFactory);
  }
  
  public MultiValueMap() {
    this(new HashMap<K, Collection<V>>(), new ReflectionFactory<Collection<V>>((Class)ArrayList.class));
  }
  
  protected <C extends Collection<V>> MultiValueMap(Map<K, ? super C> map, Factory<C> collectionFactory) {
    super((Map)map);
    if (collectionFactory == null)
      throw new IllegalArgumentException("The factory must not be null"); 
    this.collectionFactory = collectionFactory;
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(this.map);
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    this.map = (Map<K, Object>)in.readObject();
  }
  
  public void clear() {
    decorated().clear();
  }
  
  public boolean removeMapping(Object key, Object value) {
    Collection<V> valuesForKey = getCollection(key);
    if (valuesForKey == null)
      return false; 
    boolean removed = valuesForKey.remove(value);
    if (!removed)
      return false; 
    if (valuesForKey.isEmpty())
      remove(key); 
    return true;
  }
  
  public boolean containsValue(Object value) {
    Set<Map.Entry<K, Object>> pairs = decorated().entrySet();
    if (pairs != null)
      for (Map.Entry<K, Object> entry : pairs) {
        if (((Collection)entry.getValue()).contains(value))
          return true; 
      }  
    return false;
  }
  
  public Object put(K key, Object value) {
    boolean result = false;
    Collection<V> coll = getCollection(key);
    if (coll == null) {
      coll = createCollection(1);
      coll.add((V)value);
      if (coll.size() > 0) {
        decorated().put(key, coll);
        result = true;
      } 
    } else {
      result = coll.add((V)value);
    } 
    return result ? value : null;
  }
  
  public void putAll(Map<? extends K, ?> map) {
    if (map instanceof MultiMap) {
      for (Map.Entry<? extends K, Object> entry : (Iterable<Map.Entry<? extends K, Object>>)((MultiMap)map).entrySet())
        putAll(entry.getKey(), (Collection<V>)entry.getValue()); 
    } else {
      for (Map.Entry<? extends K, ?> entry : map.entrySet())
        put(entry.getKey(), entry.getValue()); 
    } 
  }
  
  public Set<Map.Entry<K, Object>> entrySet() {
    return super.entrySet();
  }
  
  public Collection<Object> values() {
    Collection<V> vs = this.valuesView;
    return (vs != null) ? (Collection)vs : (Collection)(this.valuesView = new Values());
  }
  
  public boolean containsValue(Object key, Object value) {
    Collection<V> coll = getCollection(key);
    if (coll == null)
      return false; 
    return coll.contains(value);
  }
  
  public Collection<V> getCollection(Object key) {
    return (Collection<V>)decorated().get(key);
  }
  
  public int size(Object key) {
    Collection<V> coll = getCollection(key);
    if (coll == null)
      return 0; 
    return coll.size();
  }
  
  public boolean putAll(K key, Collection<V> values) {
    if (values == null || values.size() == 0)
      return false; 
    boolean result = false;
    Collection<V> coll = getCollection(key);
    if (coll == null) {
      coll = createCollection(values.size());
      coll.addAll(values);
      if (coll.size() > 0) {
        decorated().put(key, coll);
        result = true;
      } 
    } else {
      result = coll.addAll(values);
    } 
    return result;
  }
  
  public Iterator<V> iterator(Object key) {
    if (!containsKey(key))
      return EmptyIterator.emptyIterator(); 
    return new ValuesIterator(key);
  }
  
  public Iterator<Map.Entry<K, V>> iterator() {
    Collection<K> allKeys = new ArrayList<K>(keySet());
    final Iterator<K> keyIterator = allKeys.iterator();
    return (Iterator<Map.Entry<K, V>>)new LazyIteratorChain<Map.Entry<K, V>>() {
        protected Iterator<? extends Map.Entry<K, V>> nextIterator(int count) {
          if (!keyIterator.hasNext())
            return null; 
          final K key = keyIterator.next();
          Transformer<V, Map.Entry<K, V>> transformer = new Transformer<V, Map.Entry<K, V>>() {
              public Map.Entry<K, V> transform(final V input) {
                return new Map.Entry<K, V>() {
                    public K getKey() {
                      return (K)key;
                    }
                    
                    public V getValue() {
                      return (V)input;
                    }
                    
                    public V setValue(V value) {
                      throw new UnsupportedOperationException();
                    }
                  };
              }
            };
          return (Iterator<? extends Map.Entry<K, V>>)new TransformIterator(new MultiValueMap.ValuesIterator(key), transformer);
        }
      };
  }
  
  public int totalSize() {
    int total = 0;
    for (Object v : decorated().values())
      total += CollectionUtils.size(v); 
    return total;
  }
  
  protected Collection<V> createCollection(int size) {
    return (Collection<V>)this.collectionFactory.create();
  }
  
  private class Values extends AbstractCollection<V> {
    private Values() {}
    
    public Iterator<V> iterator() {
      IteratorChain<V> chain = new IteratorChain();
      for (K k : MultiValueMap.this.keySet())
        chain.addIterator(new MultiValueMap.ValuesIterator(k)); 
      return (Iterator<V>)chain;
    }
    
    public int size() {
      return MultiValueMap.this.totalSize();
    }
    
    public void clear() {
      MultiValueMap.this.clear();
    }
  }
  
  private class ValuesIterator implements Iterator<V> {
    private final Object key;
    
    private final Collection<V> values;
    
    private final Iterator<V> iterator;
    
    public ValuesIterator(Object key) {
      this.key = key;
      this.values = MultiValueMap.this.getCollection(key);
      this.iterator = this.values.iterator();
    }
    
    public void remove() {
      this.iterator.remove();
      if (this.values.isEmpty())
        MultiValueMap.this.remove(this.key); 
    }
    
    public boolean hasNext() {
      return this.iterator.hasNext();
    }
    
    public V next() {
      return this.iterator.next();
    }
  }
  
  private static class ReflectionFactory<T extends Collection<?>> implements Factory<T>, Serializable {
    private static final long serialVersionUID = 2986114157496788874L;
    
    private final Class<T> clazz;
    
    public ReflectionFactory(Class<T> clazz) {
      this.clazz = clazz;
    }
    
    public T create() {
      try {
        return this.clazz.newInstance();
      } catch (Exception ex) {
        throw new FunctorException("Cannot instantiate class: " + this.clazz, ex);
      } 
    }
  }
}

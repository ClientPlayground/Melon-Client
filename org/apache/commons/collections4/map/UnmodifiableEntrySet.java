package org.apache.commons.collections4.map;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.keyvalue.AbstractMapEntryDecorator;
import org.apache.commons.collections4.set.AbstractSetDecorator;

public final class UnmodifiableEntrySet<K, V> extends AbstractSetDecorator<Map.Entry<K, V>> implements Unmodifiable {
  private static final long serialVersionUID = 1678353579659253473L;
  
  public static <K, V> Set<Map.Entry<K, V>> unmodifiableEntrySet(Set<Map.Entry<K, V>> set) {
    if (set instanceof Unmodifiable)
      return set; 
    return (Set)new UnmodifiableEntrySet<K, V>(set);
  }
  
  private UnmodifiableEntrySet(Set<Map.Entry<K, V>> set) {
    super(set);
  }
  
  public boolean add(Map.Entry<K, V> object) {
    throw new UnsupportedOperationException();
  }
  
  public boolean addAll(Collection<? extends Map.Entry<K, V>> coll) {
    throw new UnsupportedOperationException();
  }
  
  public void clear() {
    throw new UnsupportedOperationException();
  }
  
  public boolean remove(Object object) {
    throw new UnsupportedOperationException();
  }
  
  public boolean removeAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }
  
  public boolean retainAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }
  
  public Iterator<Map.Entry<K, V>> iterator() {
    return (Iterator<Map.Entry<K, V>>)new UnmodifiableEntrySetIterator(decorated().iterator());
  }
  
  public Object[] toArray() {
    Object[] array = decorated().toArray();
    for (int i = 0; i < array.length; i++)
      array[i] = new UnmodifiableEntry((Map.Entry<K, V>)array[i]); 
    return array;
  }
  
  public <T> T[] toArray(T[] array) {
    T[] arrayOfT = array;
    if (array.length > 0)
      arrayOfT = (T[])Array.newInstance(array.getClass().getComponentType(), 0); 
    arrayOfT = decorated().toArray((Object[])arrayOfT);
    for (int i = 0; i < arrayOfT.length; i++)
      arrayOfT[i] = (T)new UnmodifiableEntry((Map.Entry<K, V>)arrayOfT[i]); 
    if (arrayOfT.length > array.length)
      return arrayOfT; 
    System.arraycopy(arrayOfT, 0, array, 0, arrayOfT.length);
    if (array.length > arrayOfT.length)
      array[arrayOfT.length] = null; 
    return array;
  }
  
  private class UnmodifiableEntrySetIterator extends AbstractIteratorDecorator<Map.Entry<K, V>> {
    protected UnmodifiableEntrySetIterator(Iterator<Map.Entry<K, V>> iterator) {
      super(iterator);
    }
    
    public Map.Entry<K, V> next() {
      return (Map.Entry<K, V>)new UnmodifiableEntrySet.UnmodifiableEntry(getIterator().next());
    }
    
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
  
  private class UnmodifiableEntry extends AbstractMapEntryDecorator<K, V> {
    protected UnmodifiableEntry(Map.Entry<K, V> entry) {
      super(entry);
    }
    
    public V setValue(V obj) {
      throw new UnsupportedOperationException();
    }
  }
}

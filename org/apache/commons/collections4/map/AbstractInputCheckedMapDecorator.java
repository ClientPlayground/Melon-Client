package org.apache.commons.collections4.map;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.keyvalue.AbstractMapEntryDecorator;
import org.apache.commons.collections4.set.AbstractSetDecorator;

abstract class AbstractInputCheckedMapDecorator<K, V> extends AbstractMapDecorator<K, V> {
  protected AbstractInputCheckedMapDecorator() {}
  
  protected AbstractInputCheckedMapDecorator(Map<K, V> map) {
    super(map);
  }
  
  protected abstract V checkSetValue(V paramV);
  
  protected boolean isSetValueChecking() {
    return true;
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    if (isSetValueChecking())
      return (Set<Map.Entry<K, V>>)new EntrySet(this.map.entrySet(), this); 
    return this.map.entrySet();
  }
  
  private class EntrySet extends AbstractSetDecorator<Map.Entry<K, V>> {
    private static final long serialVersionUID = 4354731610923110264L;
    
    private final AbstractInputCheckedMapDecorator<K, V> parent;
    
    protected EntrySet(Set<Map.Entry<K, V>> set, AbstractInputCheckedMapDecorator<K, V> parent) {
      super(set);
      this.parent = parent;
    }
    
    public Iterator<Map.Entry<K, V>> iterator() {
      return (Iterator<Map.Entry<K, V>>)new AbstractInputCheckedMapDecorator.EntrySetIterator(decorated().iterator(), this.parent);
    }
    
    public Object[] toArray() {
      Object[] array = decorated().toArray();
      for (int i = 0; i < array.length; i++)
        array[i] = new AbstractInputCheckedMapDecorator.MapEntry((Map.Entry<K, V>)array[i], this.parent); 
      return array;
    }
    
    public <T> T[] toArray(T[] array) {
      T[] arrayOfT = array;
      if (array.length > 0)
        arrayOfT = (T[])Array.newInstance(array.getClass().getComponentType(), 0); 
      arrayOfT = decorated().toArray((Object[])arrayOfT);
      for (int i = 0; i < arrayOfT.length; i++)
        arrayOfT[i] = (T)new AbstractInputCheckedMapDecorator.MapEntry((Map.Entry<K, V>)arrayOfT[i], this.parent); 
      if (arrayOfT.length > array.length)
        return arrayOfT; 
      System.arraycopy(arrayOfT, 0, array, 0, arrayOfT.length);
      if (array.length > arrayOfT.length)
        array[arrayOfT.length] = null; 
      return array;
    }
  }
  
  private class EntrySetIterator extends AbstractIteratorDecorator<Map.Entry<K, V>> {
    private final AbstractInputCheckedMapDecorator<K, V> parent;
    
    protected EntrySetIterator(Iterator<Map.Entry<K, V>> iterator, AbstractInputCheckedMapDecorator<K, V> parent) {
      super(iterator);
      this.parent = parent;
    }
    
    public Map.Entry<K, V> next() {
      Map.Entry<K, V> entry = getIterator().next();
      return (Map.Entry<K, V>)new AbstractInputCheckedMapDecorator.MapEntry(entry, this.parent);
    }
  }
  
  private class MapEntry extends AbstractMapEntryDecorator<K, V> {
    private final AbstractInputCheckedMapDecorator<K, V> parent;
    
    protected MapEntry(Map.Entry<K, V> entry, AbstractInputCheckedMapDecorator<K, V> parent) {
      super(entry);
      this.parent = parent;
    }
    
    public V setValue(V value) {
      value = this.parent.checkSetValue(value);
      return (V)getMapEntry().setValue(value);
    }
  }
}

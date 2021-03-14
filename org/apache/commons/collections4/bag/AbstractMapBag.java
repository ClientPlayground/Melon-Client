package org.apache.commons.collections4.bag;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.set.UnmodifiableSet;

public abstract class AbstractMapBag<E> implements Bag<E> {
  private transient Map<E, MutableInteger> map;
  
  private int size;
  
  private transient int modCount;
  
  private transient Set<E> uniqueSet;
  
  protected AbstractMapBag() {}
  
  protected AbstractMapBag(Map<E, MutableInteger> map) {
    this.map = map;
  }
  
  protected Map<E, MutableInteger> getMap() {
    return this.map;
  }
  
  public int size() {
    return this.size;
  }
  
  public boolean isEmpty() {
    return this.map.isEmpty();
  }
  
  public int getCount(Object object) {
    MutableInteger count = this.map.get(object);
    if (count != null)
      return count.value; 
    return 0;
  }
  
  public boolean contains(Object object) {
    return this.map.containsKey(object);
  }
  
  public boolean containsAll(Collection<?> coll) {
    if (coll instanceof Bag)
      return containsAll((Bag)coll); 
    return containsAll(new HashBag(coll));
  }
  
  boolean containsAll(Bag<?> other) {
    Iterator<?> it = other.uniqueSet().iterator();
    while (it.hasNext()) {
      Object current = it.next();
      if (getCount(current) < other.getCount(current))
        return false; 
    } 
    return true;
  }
  
  public Iterator<E> iterator() {
    return new BagIterator<E>(this);
  }
  
  static class BagIterator<E> implements Iterator<E> {
    private final AbstractMapBag<E> parent;
    
    private final Iterator<Map.Entry<E, AbstractMapBag.MutableInteger>> entryIterator;
    
    private Map.Entry<E, AbstractMapBag.MutableInteger> current;
    
    private int itemCount;
    
    private final int mods;
    
    private boolean canRemove;
    
    public BagIterator(AbstractMapBag<E> parent) {
      this.parent = parent;
      this.entryIterator = parent.map.entrySet().iterator();
      this.current = null;
      this.mods = parent.modCount;
      this.canRemove = false;
    }
    
    public boolean hasNext() {
      return (this.itemCount > 0 || this.entryIterator.hasNext());
    }
    
    public E next() {
      if (this.parent.modCount != this.mods)
        throw new ConcurrentModificationException(); 
      if (this.itemCount == 0) {
        this.current = this.entryIterator.next();
        this.itemCount = ((AbstractMapBag.MutableInteger)this.current.getValue()).value;
      } 
      this.canRemove = true;
      this.itemCount--;
      return this.current.getKey();
    }
    
    public void remove() {
      if (this.parent.modCount != this.mods)
        throw new ConcurrentModificationException(); 
      if (!this.canRemove)
        throw new IllegalStateException(); 
      AbstractMapBag.MutableInteger mut = this.current.getValue();
      if (mut.value > 1) {
        mut.value--;
      } else {
        this.entryIterator.remove();
      } 
      this.parent.size--;
      this.canRemove = false;
    }
  }
  
  public boolean add(E object) {
    return add(object, 1);
  }
  
  public boolean add(E object, int nCopies) {
    this.modCount++;
    if (nCopies > 0) {
      MutableInteger mut = this.map.get(object);
      this.size += nCopies;
      if (mut == null) {
        this.map.put(object, new MutableInteger(nCopies));
        return true;
      } 
      mut.value += nCopies;
      return false;
    } 
    return false;
  }
  
  public boolean addAll(Collection<? extends E> coll) {
    boolean changed = false;
    Iterator<? extends E> i = coll.iterator();
    while (i.hasNext()) {
      boolean added = add(i.next());
      changed = (changed || added);
    } 
    return changed;
  }
  
  public void clear() {
    this.modCount++;
    this.map.clear();
    this.size = 0;
  }
  
  public boolean remove(Object object) {
    MutableInteger mut = this.map.get(object);
    if (mut == null)
      return false; 
    this.modCount++;
    this.map.remove(object);
    this.size -= mut.value;
    return true;
  }
  
  public boolean remove(Object object, int nCopies) {
    MutableInteger mut = this.map.get(object);
    if (mut == null)
      return false; 
    if (nCopies <= 0)
      return false; 
    this.modCount++;
    if (nCopies < mut.value) {
      mut.value -= nCopies;
      this.size -= nCopies;
    } else {
      this.map.remove(object);
      this.size -= mut.value;
    } 
    return true;
  }
  
  public boolean removeAll(Collection<?> coll) {
    boolean result = false;
    if (coll != null) {
      Iterator<?> i = coll.iterator();
      while (i.hasNext()) {
        boolean changed = remove(i.next(), 1);
        result = (result || changed);
      } 
    } 
    return result;
  }
  
  public boolean retainAll(Collection<?> coll) {
    if (coll instanceof Bag)
      return retainAll((Bag)coll); 
    return retainAll(new HashBag(coll));
  }
  
  boolean retainAll(Bag<?> other) {
    boolean result = false;
    Bag<E> excess = new HashBag<E>();
    Iterator<E> i = uniqueSet().iterator();
    while (i.hasNext()) {
      E current = i.next();
      int myCount = getCount(current);
      int otherCount = other.getCount(current);
      if (1 <= otherCount && otherCount <= myCount) {
        excess.add(current, myCount - otherCount);
        continue;
      } 
      excess.add(current, myCount);
    } 
    if (!excess.isEmpty())
      result = removeAll((Collection<?>)excess); 
    return result;
  }
  
  protected static class MutableInteger {
    protected int value;
    
    MutableInteger(int value) {
      this.value = value;
    }
    
    public boolean equals(Object obj) {
      if (!(obj instanceof MutableInteger))
        return false; 
      return (((MutableInteger)obj).value == this.value);
    }
    
    public int hashCode() {
      return this.value;
    }
  }
  
  public Object[] toArray() {
    Object[] result = new Object[size()];
    int i = 0;
    Iterator<E> it = this.map.keySet().iterator();
    while (it.hasNext()) {
      E current = it.next();
      for (int index = getCount(current); index > 0; index--)
        result[i++] = current; 
    } 
    return result;
  }
  
  public <T> T[] toArray(T[] array) {
    int size = size();
    if (array.length < size) {
      T[] unchecked = (T[])Array.newInstance(array.getClass().getComponentType(), size);
      array = unchecked;
    } 
    int i = 0;
    Iterator<E> it = this.map.keySet().iterator();
    while (it.hasNext()) {
      E current = it.next();
      for (int index = getCount(current); index > 0; index--) {
        E e = current;
        array[i++] = (T)e;
      } 
    } 
    while (i < array.length)
      array[i++] = null; 
    return array;
  }
  
  public Set<E> uniqueSet() {
    if (this.uniqueSet == null)
      this.uniqueSet = UnmodifiableSet.unmodifiableSet(this.map.keySet()); 
    return this.uniqueSet;
  }
  
  protected void doWriteObject(ObjectOutputStream out) throws IOException {
    out.writeInt(this.map.size());
    for (Map.Entry<E, MutableInteger> entry : this.map.entrySet()) {
      out.writeObject(entry.getKey());
      out.writeInt(((MutableInteger)entry.getValue()).value);
    } 
  }
  
  protected void doReadObject(Map<E, MutableInteger> map, ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.map = map;
    int entrySize = in.readInt();
    for (int i = 0; i < entrySize; i++) {
      E obj = (E)in.readObject();
      int count = in.readInt();
      map.put(obj, new MutableInteger(count));
      this.size += count;
    } 
  }
  
  public boolean equals(Object object) {
    if (object == this)
      return true; 
    if (!(object instanceof Bag))
      return false; 
    Bag<?> other = (Bag)object;
    if (other.size() != size())
      return false; 
    for (E element : this.map.keySet()) {
      if (other.getCount(element) != getCount(element))
        return false; 
    } 
    return true;
  }
  
  public int hashCode() {
    int total = 0;
    for (Map.Entry<E, MutableInteger> entry : this.map.entrySet()) {
      E element = entry.getKey();
      MutableInteger count = entry.getValue();
      total += ((element == null) ? 0 : element.hashCode()) ^ count.value;
    } 
    return total;
  }
  
  public String toString() {
    if (size() == 0)
      return "[]"; 
    StringBuilder buf = new StringBuilder();
    buf.append('[');
    Iterator<E> it = uniqueSet().iterator();
    while (it.hasNext()) {
      Object current = it.next();
      int count = getCount(current);
      buf.append(count);
      buf.append(':');
      buf.append(current);
      if (it.hasNext())
        buf.append(','); 
    } 
    buf.append(']');
    return buf.toString();
  }
}

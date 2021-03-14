package org.apache.commons.collections4.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.iterators.AbstractListIteratorDecorator;
import org.apache.commons.collections4.set.UnmodifiableSet;

public class SetUniqueList<E> extends AbstractSerializableListDecorator<E> {
  private static final long serialVersionUID = 7196982186153478694L;
  
  private final Set<E> set;
  
  public static <E> SetUniqueList<E> setUniqueList(List<E> list) {
    if (list == null)
      throw new IllegalArgumentException("List must not be null"); 
    if (list.isEmpty())
      return new SetUniqueList<E>(list, new HashSet<E>()); 
    List<E> temp = new ArrayList<E>(list);
    list.clear();
    SetUniqueList<E> sl = new SetUniqueList<E>(list, new HashSet<E>());
    sl.addAll(temp);
    return sl;
  }
  
  protected SetUniqueList(List<E> list, Set<E> set) {
    super(list);
    if (set == null)
      throw new IllegalArgumentException("Set must not be null"); 
    this.set = set;
  }
  
  public Set<E> asSet() {
    return UnmodifiableSet.unmodifiableSet(this.set);
  }
  
  public boolean add(E object) {
    int sizeBefore = size();
    add(size(), object);
    return (sizeBefore != size());
  }
  
  public void add(int index, E object) {
    if (!this.set.contains(object)) {
      super.add(index, object);
      this.set.add(object);
    } 
  }
  
  public boolean addAll(Collection<? extends E> coll) {
    return addAll(size(), coll);
  }
  
  public boolean addAll(int index, Collection<? extends E> coll) {
    List<E> temp = new ArrayList<E>();
    for (E e : coll) {
      if (this.set.add(e))
        temp.add(e); 
    } 
    return super.addAll(index, temp);
  }
  
  public E set(int index, E object) {
    int pos = indexOf(object);
    E removed = super.set(index, object);
    if (pos != -1 && pos != index)
      super.remove(pos); 
    this.set.remove(removed);
    this.set.add(object);
    return removed;
  }
  
  public boolean remove(Object object) {
    boolean result = this.set.remove(object);
    if (result)
      super.remove(object); 
    return result;
  }
  
  public E remove(int index) {
    E result = super.remove(index);
    this.set.remove(result);
    return result;
  }
  
  public boolean removeAll(Collection<?> coll) {
    boolean result = false;
    for (Object name : coll)
      result |= remove(name); 
    return result;
  }
  
  public boolean retainAll(Collection<?> coll) {
    Set<Object> setRetainAll = new HashSet();
    for (Object next : coll) {
      if (this.set.contains(next))
        setRetainAll.add(next); 
    } 
    if (setRetainAll.size() == this.set.size())
      return false; 
    if (setRetainAll.size() == 0) {
      clear();
    } else {
      for (Iterator<E> it = iterator(); it.hasNext();) {
        if (!setRetainAll.contains(it.next()))
          it.remove(); 
      } 
    } 
    return true;
  }
  
  public void clear() {
    super.clear();
    this.set.clear();
  }
  
  public boolean contains(Object object) {
    return this.set.contains(object);
  }
  
  public boolean containsAll(Collection<?> coll) {
    return this.set.containsAll(coll);
  }
  
  public Iterator<E> iterator() {
    return (Iterator<E>)new SetListIterator<E>(super.iterator(), this.set);
  }
  
  public ListIterator<E> listIterator() {
    return (ListIterator<E>)new SetListListIterator<E>(super.listIterator(), this.set);
  }
  
  public ListIterator<E> listIterator(int index) {
    return (ListIterator<E>)new SetListListIterator<E>(super.listIterator(index), this.set);
  }
  
  public List<E> subList(int fromIndex, int toIndex) {
    List<E> superSubList = super.subList(fromIndex, toIndex);
    Set<E> subSet = createSetBasedOnList(this.set, superSubList);
    return ListUtils.unmodifiableList(new SetUniqueList(superSubList, subSet));
  }
  
  protected Set<E> createSetBasedOnList(Set<E> set, List<E> list) {
    Set<E> set1;
    if (set.getClass().equals(HashSet.class)) {
      set1 = new HashSet<E>(list.size());
    } else {
      try {
        set1 = (Set<E>)set.getClass().newInstance();
      } catch (InstantiationException ie) {
        set1 = new HashSet();
      } catch (IllegalAccessException iae) {
        set1 = new HashSet();
      } 
    } 
    set1.addAll(list);
    return set1;
  }
  
  static class SetListIterator<E> extends AbstractIteratorDecorator<E> {
    private final Set<E> set;
    
    private E last = null;
    
    protected SetListIterator(Iterator<E> it, Set<E> set) {
      super(it);
      this.set = set;
    }
    
    public E next() {
      this.last = (E)super.next();
      return this.last;
    }
    
    public void remove() {
      super.remove();
      this.set.remove(this.last);
      this.last = null;
    }
  }
  
  static class SetListListIterator<E> extends AbstractListIteratorDecorator<E> {
    private final Set<E> set;
    
    private E last = null;
    
    protected SetListListIterator(ListIterator<E> it, Set<E> set) {
      super(it);
      this.set = set;
    }
    
    public E next() {
      this.last = (E)super.next();
      return this.last;
    }
    
    public E previous() {
      this.last = (E)super.previous();
      return this.last;
    }
    
    public void remove() {
      super.remove();
      this.set.remove(this.last);
      this.last = null;
    }
    
    public void add(E object) {
      if (!this.set.contains(object)) {
        super.add(object);
        this.set.add(object);
      } 
    }
    
    public void set(E object) {
      throw new UnsupportedOperationException("ListIterator does not support set");
    }
  }
}

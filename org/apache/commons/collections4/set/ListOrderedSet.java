package org.apache.commons.collections4.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.apache.commons.collections4.OrderedIterator;
import org.apache.commons.collections4.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections4.list.UnmodifiableList;

public class ListOrderedSet<E> extends AbstractSerializableSetDecorator<E> {
  private static final long serialVersionUID = -228664372470420141L;
  
  private final List<E> setOrder;
  
  public static <E> ListOrderedSet<E> listOrderedSet(Set<E> set, List<E> list) {
    if (set == null)
      throw new IllegalArgumentException("Set must not be null"); 
    if (list == null)
      throw new IllegalArgumentException("List must not be null"); 
    if (set.size() > 0 || list.size() > 0)
      throw new IllegalArgumentException("Set and List must be empty"); 
    return new ListOrderedSet<E>(set, list);
  }
  
  public static <E> ListOrderedSet<E> listOrderedSet(Set<E> set) {
    return new ListOrderedSet<E>(set);
  }
  
  public static <E> ListOrderedSet<E> listOrderedSet(List<E> list) {
    if (list == null)
      throw new IllegalArgumentException("List must not be null"); 
    Set<E> set = new HashSet<E>(list);
    list.retainAll(set);
    return new ListOrderedSet<E>(set, list);
  }
  
  public ListOrderedSet() {
    super(new HashSet<E>());
    this.setOrder = new ArrayList<E>();
  }
  
  protected ListOrderedSet(Set<E> set) {
    super(set);
    this.setOrder = new ArrayList<E>(set);
  }
  
  protected ListOrderedSet(Set<E> set, List<E> list) {
    super(set);
    if (list == null)
      throw new IllegalArgumentException("List must not be null"); 
    this.setOrder = list;
  }
  
  public List<E> asList() {
    return UnmodifiableList.unmodifiableList(this.setOrder);
  }
  
  public void clear() {
    decorated().clear();
    this.setOrder.clear();
  }
  
  public OrderedIterator<E> iterator() {
    return new OrderedSetIterator<E>(this.setOrder.listIterator(), decorated());
  }
  
  public boolean add(E object) {
    if (decorated().add(object)) {
      this.setOrder.add(object);
      return true;
    } 
    return false;
  }
  
  public boolean addAll(Collection<? extends E> coll) {
    boolean result = false;
    for (E e : coll)
      result |= add(e); 
    return result;
  }
  
  public boolean remove(Object object) {
    boolean result = decorated().remove(object);
    if (result)
      this.setOrder.remove(object); 
    return result;
  }
  
  public boolean removeAll(Collection<?> coll) {
    boolean result = false;
    for (Object name : coll)
      result |= remove(name); 
    return result;
  }
  
  public boolean retainAll(Collection<?> coll) {
    Set<Object> collectionRetainAll = new HashSet();
    for (Object next : coll) {
      if (decorated().contains(next))
        collectionRetainAll.add(next); 
    } 
    if (collectionRetainAll.size() == decorated().size())
      return false; 
    if (collectionRetainAll.size() == 0) {
      clear();
    } else {
      for (OrderedIterator<E> orderedIterator = iterator(); orderedIterator.hasNext();) {
        if (!collectionRetainAll.contains(orderedIterator.next()))
          orderedIterator.remove(); 
      } 
    } 
    return true;
  }
  
  public Object[] toArray() {
    return this.setOrder.toArray();
  }
  
  public <T> T[] toArray(T[] a) {
    return this.setOrder.toArray(a);
  }
  
  public E get(int index) {
    return this.setOrder.get(index);
  }
  
  public int indexOf(Object object) {
    return this.setOrder.indexOf(object);
  }
  
  public void add(int index, E object) {
    if (!contains(object)) {
      decorated().add(object);
      this.setOrder.add(index, object);
    } 
  }
  
  public boolean addAll(int index, Collection<? extends E> coll) {
    boolean changed = false;
    List<E> toAdd = new ArrayList<E>();
    for (E e : coll) {
      if (contains(e))
        continue; 
      decorated().add(e);
      toAdd.add(e);
      changed = true;
    } 
    if (changed)
      this.setOrder.addAll(index, toAdd); 
    return changed;
  }
  
  public Object remove(int index) {
    Object obj = this.setOrder.remove(index);
    remove(obj);
    return obj;
  }
  
  public String toString() {
    return this.setOrder.toString();
  }
  
  static class OrderedSetIterator<E> extends AbstractIteratorDecorator<E> implements OrderedIterator<E> {
    private final Collection<E> set;
    
    private E last;
    
    private OrderedSetIterator(ListIterator<E> iterator, Collection<E> set) {
      super(iterator);
      this.set = set;
    }
    
    public E next() {
      this.last = getIterator().next();
      return this.last;
    }
    
    public void remove() {
      this.set.remove(this.last);
      getIterator().remove();
      this.last = null;
    }
    
    public boolean hasPrevious() {
      return ((ListIterator)getIterator()).hasPrevious();
    }
    
    public E previous() {
      this.last = ((ListIterator<E>)getIterator()).previous();
      return this.last;
    }
  }
}

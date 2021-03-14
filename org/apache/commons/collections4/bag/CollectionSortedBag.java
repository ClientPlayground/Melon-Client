package org.apache.commons.collections4.bag;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.collections4.SortedBag;

public final class CollectionSortedBag<E> extends AbstractSortedBagDecorator<E> {
  private static final long serialVersionUID = -2560033712679053143L;
  
  public static <E> SortedBag<E> collectionSortedBag(SortedBag<E> bag) {
    return new CollectionSortedBag<E>(bag);
  }
  
  public CollectionSortedBag(SortedBag<E> bag) {
    super(bag);
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(decorated());
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    setCollection((Collection)in.readObject());
  }
  
  public boolean containsAll(Collection<?> coll) {
    Iterator<?> e = coll.iterator();
    while (e.hasNext()) {
      if (!contains(e.next()))
        return false; 
    } 
    return true;
  }
  
  public boolean add(E object) {
    return add(object, 1);
  }
  
  public boolean addAll(Collection<? extends E> coll) {
    boolean changed = false;
    Iterator<? extends E> i = coll.iterator();
    while (i.hasNext()) {
      boolean added = add(i.next(), 1);
      changed = (changed || added);
    } 
    return changed;
  }
  
  public boolean remove(Object object) {
    return remove(object, 1);
  }
  
  public boolean removeAll(Collection<?> coll) {
    if (coll != null) {
      boolean result = false;
      Iterator<?> i = coll.iterator();
      while (i.hasNext()) {
        Object obj = i.next();
        boolean changed = remove(obj, getCount(obj));
        result = (result || changed);
      } 
      return result;
    } 
    return decorated().removeAll(null);
  }
  
  public boolean retainAll(Collection<?> coll) {
    if (coll != null) {
      boolean modified = false;
      Iterator<E> e = iterator();
      while (e.hasNext()) {
        if (!coll.contains(e.next())) {
          e.remove();
          modified = true;
        } 
      } 
      return modified;
    } 
    return decorated().retainAll(null);
  }
  
  public boolean add(E object, int count) {
    decorated().add(object, count);
    return true;
  }
}

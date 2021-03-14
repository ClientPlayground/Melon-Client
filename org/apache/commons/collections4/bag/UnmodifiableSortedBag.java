package org.apache.commons.collections4.bag;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.collections4.SortedBag;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableIterator;
import org.apache.commons.collections4.set.UnmodifiableSet;

public final class UnmodifiableSortedBag<E> extends AbstractSortedBagDecorator<E> implements Unmodifiable {
  private static final long serialVersionUID = -3190437252665717841L;
  
  public static <E> SortedBag<E> unmodifiableSortedBag(SortedBag<E> bag) {
    if (bag instanceof Unmodifiable)
      return bag; 
    return new UnmodifiableSortedBag<E>(bag);
  }
  
  private UnmodifiableSortedBag(SortedBag<E> bag) {
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
  
  public Iterator<E> iterator() {
    return UnmodifiableIterator.unmodifiableIterator(decorated().iterator());
  }
  
  public boolean add(E object) {
    throw new UnsupportedOperationException();
  }
  
  public boolean addAll(Collection<? extends E> coll) {
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
  
  public boolean add(E object, int count) {
    throw new UnsupportedOperationException();
  }
  
  public boolean remove(Object object, int count) {
    throw new UnsupportedOperationException();
  }
  
  public Set<E> uniqueSet() {
    Set<E> set = decorated().uniqueSet();
    return UnmodifiableSet.unmodifiableSet(set);
  }
}

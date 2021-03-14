package org.apache.commons.collections4.set;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableIterator;

public final class UnmodifiableSortedSet<E> extends AbstractSortedSetDecorator<E> implements Unmodifiable {
  private static final long serialVersionUID = -725356885467962424L;
  
  public static <E> SortedSet<E> unmodifiableSortedSet(SortedSet<E> set) {
    if (set instanceof Unmodifiable)
      return set; 
    return new UnmodifiableSortedSet<E>(set);
  }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeObject(decorated());
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    setCollection((Collection)in.readObject());
  }
  
  private UnmodifiableSortedSet(SortedSet<E> set) {
    super(set);
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
  
  public SortedSet<E> subSet(E fromElement, E toElement) {
    SortedSet<E> sub = decorated().subSet(fromElement, toElement);
    return new UnmodifiableSortedSet(sub);
  }
  
  public SortedSet<E> headSet(E toElement) {
    SortedSet<E> sub = decorated().headSet(toElement);
    return new UnmodifiableSortedSet(sub);
  }
  
  public SortedSet<E> tailSet(E fromElement) {
    SortedSet<E> sub = decorated().tailSet(fromElement);
    return new UnmodifiableSortedSet(sub);
  }
}

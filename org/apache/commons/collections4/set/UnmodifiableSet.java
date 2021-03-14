package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableIterator;

public final class UnmodifiableSet<E> extends AbstractSerializableSetDecorator<E> implements Unmodifiable {
  private static final long serialVersionUID = 6499119872185240161L;
  
  public static <E> Set<E> unmodifiableSet(Set<? extends E> set) {
    if (set instanceof Unmodifiable)
      return (Set)set; 
    return new UnmodifiableSet<E>(set);
  }
  
  private UnmodifiableSet(Set<? extends E> set) {
    super((Set)set);
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
}

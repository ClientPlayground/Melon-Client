package org.apache.commons.collections4.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.collections4.Unmodifiable;
import org.apache.commons.collections4.iterators.UnmodifiableIterator;
import org.apache.commons.collections4.iterators.UnmodifiableListIterator;

public final class UnmodifiableList<E> extends AbstractSerializableListDecorator<E> implements Unmodifiable {
  private static final long serialVersionUID = 6595182819922443652L;
  
  public static <E> List<E> unmodifiableList(List<? extends E> list) {
    if (list instanceof Unmodifiable)
      return (List)list; 
    return new UnmodifiableList<E>(list);
  }
  
  public UnmodifiableList(List<? extends E> list) {
    super((List)list);
  }
  
  public Iterator<E> iterator() {
    return UnmodifiableIterator.unmodifiableIterator(decorated().iterator());
  }
  
  public boolean add(Object object) {
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
  
  public ListIterator<E> listIterator() {
    return UnmodifiableListIterator.umodifiableListIterator(decorated().listIterator());
  }
  
  public ListIterator<E> listIterator(int index) {
    return UnmodifiableListIterator.umodifiableListIterator(decorated().listIterator(index));
  }
  
  public void add(int index, E object) {
    throw new UnsupportedOperationException();
  }
  
  public boolean addAll(int index, Collection<? extends E> coll) {
    throw new UnsupportedOperationException();
  }
  
  public E remove(int index) {
    throw new UnsupportedOperationException();
  }
  
  public E set(int index, E object) {
    throw new UnsupportedOperationException();
  }
  
  public List<E> subList(int fromIndex, int toIndex) {
    List<E> sub = decorated().subList(fromIndex, toIndex);
    return new UnmodifiableList(sub);
  }
}

package org.apache.commons.collections4.set;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

public abstract class AbstractSortedSetDecorator<E> extends AbstractSetDecorator<E> implements SortedSet<E> {
  private static final long serialVersionUID = -3462240946294214398L;
  
  protected AbstractSortedSetDecorator() {}
  
  protected AbstractSortedSetDecorator(Set<E> set) {
    super(set);
  }
  
  protected SortedSet<E> decorated() {
    return (SortedSet<E>)super.decorated();
  }
  
  public SortedSet<E> subSet(E fromElement, E toElement) {
    return decorated().subSet(fromElement, toElement);
  }
  
  public SortedSet<E> headSet(E toElement) {
    return decorated().headSet(toElement);
  }
  
  public SortedSet<E> tailSet(E fromElement) {
    return decorated().tailSet(fromElement);
  }
  
  public E first() {
    return decorated().first();
  }
  
  public E last() {
    return decorated().last();
  }
  
  public Comparator<? super E> comparator() {
    return decorated().comparator();
  }
}

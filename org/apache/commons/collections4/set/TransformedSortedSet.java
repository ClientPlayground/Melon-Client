package org.apache.commons.collections4.set;

import java.util.Comparator;
import java.util.SortedSet;
import org.apache.commons.collections4.Transformer;

public class TransformedSortedSet<E> extends TransformedSet<E> implements SortedSet<E> {
  private static final long serialVersionUID = -1675486811351124386L;
  
  public static <E> TransformedSortedSet<E> transformingSortedSet(SortedSet<E> set, Transformer<? super E, ? extends E> transformer) {
    return new TransformedSortedSet<E>(set, transformer);
  }
  
  public static <E> TransformedSortedSet<E> transformedSortedSet(SortedSet<E> set, Transformer<? super E, ? extends E> transformer) {
    TransformedSortedSet<E> decorated = new TransformedSortedSet<E>(set, transformer);
    if (transformer != null && set != null && set.size() > 0) {
      E[] values = (E[])set.toArray();
      set.clear();
      for (E value : values)
        decorated.decorated().add(transformer.transform(value)); 
    } 
    return decorated;
  }
  
  protected TransformedSortedSet(SortedSet<E> set, Transformer<? super E, ? extends E> transformer) {
    super(set, transformer);
  }
  
  protected SortedSet<E> getSortedSet() {
    return (SortedSet<E>)decorated();
  }
  
  public E first() {
    return getSortedSet().first();
  }
  
  public E last() {
    return getSortedSet().last();
  }
  
  public Comparator<? super E> comparator() {
    return getSortedSet().comparator();
  }
  
  public SortedSet<E> subSet(E fromElement, E toElement) {
    SortedSet<E> set = getSortedSet().subSet(fromElement, toElement);
    return new TransformedSortedSet(set, this.transformer);
  }
  
  public SortedSet<E> headSet(E toElement) {
    SortedSet<E> set = getSortedSet().headSet(toElement);
    return new TransformedSortedSet(set, this.transformer);
  }
  
  public SortedSet<E> tailSet(E fromElement) {
    SortedSet<E> set = getSortedSet().tailSet(fromElement);
    return new TransformedSortedSet(set, this.transformer);
  }
}

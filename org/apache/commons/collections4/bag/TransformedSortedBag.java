package org.apache.commons.collections4.bag;

import java.util.Comparator;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.SortedBag;
import org.apache.commons.collections4.Transformer;

public class TransformedSortedBag<E> extends TransformedBag<E> implements SortedBag<E> {
  private static final long serialVersionUID = -251737742649401930L;
  
  public static <E> TransformedSortedBag<E> transformingSortedBag(SortedBag<E> bag, Transformer<? super E, ? extends E> transformer) {
    return new TransformedSortedBag<E>(bag, transformer);
  }
  
  public static <E> TransformedSortedBag<E> transformedSortedBag(SortedBag<E> bag, Transformer<? super E, ? extends E> transformer) {
    TransformedSortedBag<E> decorated = new TransformedSortedBag<E>(bag, transformer);
    if (transformer != null && bag != null && bag.size() > 0) {
      E[] values = (E[])bag.toArray();
      bag.clear();
      for (E value : values)
        decorated.decorated().add(transformer.transform(value)); 
    } 
    return decorated;
  }
  
  protected TransformedSortedBag(SortedBag<E> bag, Transformer<? super E, ? extends E> transformer) {
    super((Bag<E>)bag, transformer);
  }
  
  protected SortedBag<E> getSortedBag() {
    return (SortedBag<E>)decorated();
  }
  
  public E first() {
    return (E)getSortedBag().first();
  }
  
  public E last() {
    return (E)getSortedBag().last();
  }
  
  public Comparator<? super E> comparator() {
    return getSortedBag().comparator();
  }
}

package org.apache.commons.collections4.iterators;

import org.apache.commons.collections4.OrderedIterator;
import org.apache.commons.collections4.ResettableIterator;

public class EmptyOrderedIterator<E> extends AbstractEmptyIterator<E> implements OrderedIterator<E>, ResettableIterator<E> {
  public static final OrderedIterator INSTANCE = new EmptyOrderedIterator();
  
  public static <E> OrderedIterator<E> emptyOrderedIterator() {
    return INSTANCE;
  }
}

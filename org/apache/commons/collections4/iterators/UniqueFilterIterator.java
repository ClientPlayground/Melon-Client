package org.apache.commons.collections4.iterators;

import java.util.Iterator;
import org.apache.commons.collections4.functors.UniquePredicate;

public class UniqueFilterIterator<E> extends FilterIterator<E> {
  public UniqueFilterIterator(Iterator<? extends E> iterator) {
    super(iterator, UniquePredicate.uniquePredicate());
  }
}

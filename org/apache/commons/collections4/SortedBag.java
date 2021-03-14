package org.apache.commons.collections4;

import java.util.Comparator;

public interface SortedBag<E> extends Bag<E> {
  Comparator<? super E> comparator();
  
  E first();
  
  E last();
}

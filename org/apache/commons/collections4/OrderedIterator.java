package org.apache.commons.collections4;

import java.util.Iterator;

public interface OrderedIterator<E> extends Iterator<E> {
  boolean hasPrevious();
  
  E previous();
}

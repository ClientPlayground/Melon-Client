package org.apache.commons.collections4;

import java.util.Iterator;

public interface ResettableIterator<E> extends Iterator<E> {
  void reset();
}

package org.apache.commons.collections4;

import java.util.Collection;

public interface BoundedCollection<E> extends Collection<E> {
  boolean isFull();
  
  int maxSize();
}

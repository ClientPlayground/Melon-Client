package org.apache.commons.collections4;

public interface Equator<T> {
  boolean equate(T paramT1, T paramT2);
  
  int hash(T paramT);
}

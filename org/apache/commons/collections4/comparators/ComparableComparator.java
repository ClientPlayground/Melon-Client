package org.apache.commons.collections4.comparators;

import java.io.Serializable;
import java.util.Comparator;

public class ComparableComparator<E extends Comparable<? super E>> implements Comparator<E>, Serializable {
  private static final long serialVersionUID = -291439688585137865L;
  
  public static final ComparableComparator INSTANCE = new ComparableComparator();
  
  public static <E extends Comparable<? super E>> ComparableComparator<E> comparableComparator() {
    return INSTANCE;
  }
  
  public int compare(E obj1, E obj2) {
    return obj1.compareTo(obj2);
  }
  
  public int hashCode() {
    return "ComparableComparator".hashCode();
  }
  
  public boolean equals(Object object) {
    return (this == object || (null != object && object.getClass().equals(getClass())));
  }
}

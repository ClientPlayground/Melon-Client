package org.apache.commons.collections4.functors;

import java.io.Serializable;
import java.util.Comparator;
import org.apache.commons.collections4.Predicate;

public class ComparatorPredicate<T> implements Predicate<T>, Serializable {
  private static final long serialVersionUID = -1863209236504077399L;
  
  private final T object;
  
  private final Comparator<T> comparator;
  
  private final Criterion criterion;
  
  public enum Criterion {
    EQUAL, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL;
  }
  
  public static <T> Predicate<T> comparatorPredicate(T object, Comparator<T> comparator) {
    return comparatorPredicate(object, comparator, Criterion.EQUAL);
  }
  
  public static <T> Predicate<T> comparatorPredicate(T object, Comparator<T> comparator, Criterion criterion) {
    if (comparator == null)
      throw new IllegalArgumentException("Comparator must not be null."); 
    if (criterion == null)
      throw new IllegalArgumentException("Criterion must not be null."); 
    return new ComparatorPredicate<T>(object, comparator, criterion);
  }
  
  public ComparatorPredicate(T object, Comparator<T> comparator, Criterion criterion) {
    this.object = object;
    this.comparator = comparator;
    this.criterion = criterion;
  }
  
  public boolean evaluate(T target) {
    boolean result = false;
    int comparison = this.comparator.compare(this.object, target);
    switch (this.criterion) {
      case EQUAL:
        result = (comparison == 0);
        return result;
      case GREATER:
        result = (comparison > 0);
        return result;
      case LESS:
        result = (comparison < 0);
        return result;
      case GREATER_OR_EQUAL:
        result = (comparison >= 0);
        return result;
      case LESS_OR_EQUAL:
        result = (comparison <= 0);
        return result;
    } 
    throw new IllegalStateException("The current criterion '" + this.criterion + "' is invalid.");
  }
}

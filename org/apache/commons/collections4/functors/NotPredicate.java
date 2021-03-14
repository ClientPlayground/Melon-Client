package org.apache.commons.collections4.functors;

import java.io.Serializable;
import org.apache.commons.collections4.Predicate;

public final class NotPredicate<T> implements PredicateDecorator<T>, Serializable {
  private static final long serialVersionUID = -2654603322338049674L;
  
  private final Predicate<? super T> iPredicate;
  
  public static <T> Predicate<T> notPredicate(Predicate<? super T> predicate) {
    if (predicate == null)
      throw new IllegalArgumentException("Predicate must not be null"); 
    return new NotPredicate<T>(predicate);
  }
  
  public NotPredicate(Predicate<? super T> predicate) {
    this.iPredicate = predicate;
  }
  
  public boolean evaluate(T object) {
    return !this.iPredicate.evaluate(object);
  }
  
  public Predicate<? super T>[] getPredicates() {
    return (Predicate<? super T>[])new Predicate[] { this.iPredicate };
  }
}

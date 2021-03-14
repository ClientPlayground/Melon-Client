package org.apache.commons.collections4.functors;

import java.io.Serializable;
import org.apache.commons.collections4.Predicate;

public final class NullIsFalsePredicate<T> implements PredicateDecorator<T>, Serializable {
  private static final long serialVersionUID = -2997501534564735525L;
  
  private final Predicate<? super T> iPredicate;
  
  public static <T> Predicate<T> nullIsFalsePredicate(Predicate<? super T> predicate) {
    if (predicate == null)
      throw new IllegalArgumentException("Predicate must not be null"); 
    return new NullIsFalsePredicate<T>(predicate);
  }
  
  public NullIsFalsePredicate(Predicate<? super T> predicate) {
    this.iPredicate = predicate;
  }
  
  public boolean evaluate(T object) {
    if (object == null)
      return false; 
    return this.iPredicate.evaluate(object);
  }
  
  public Predicate<? super T>[] getPredicates() {
    return (Predicate<? super T>[])new Predicate[] { this.iPredicate };
  }
}

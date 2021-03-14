package org.apache.commons.collections4.functors;

import java.util.Collection;
import org.apache.commons.collections4.Predicate;

public final class OnePredicate<T> extends AbstractQuantifierPredicate<T> {
  private static final long serialVersionUID = -8125389089924745785L;
  
  public static <T> Predicate<T> onePredicate(Predicate<? super T>... predicates) {
    FunctorUtils.validate((Predicate<?>[])predicates);
    if (predicates.length == 0)
      return FalsePredicate.falsePredicate(); 
    if (predicates.length == 1)
      return (Predicate)predicates[0]; 
    return new OnePredicate<T>((Predicate<? super T>[])FunctorUtils.copy(predicates));
  }
  
  public static <T> Predicate<T> onePredicate(Collection<? extends Predicate<T>> predicates) {
    Predicate[] arrayOfPredicate = (Predicate[])FunctorUtils.validate(predicates);
    return new OnePredicate<T>((Predicate<? super T>[])arrayOfPredicate);
  }
  
  public OnePredicate(Predicate<? super T>... predicates) {
    super(predicates);
  }
  
  public boolean evaluate(T object) {
    boolean match = false;
    for (Predicate<? super T> iPredicate : this.iPredicates) {
      if (iPredicate.evaluate(object)) {
        if (match)
          return false; 
        match = true;
      } 
    } 
    return match;
  }
}

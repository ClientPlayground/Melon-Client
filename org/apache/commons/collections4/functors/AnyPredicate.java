package org.apache.commons.collections4.functors;

import java.util.Collection;
import org.apache.commons.collections4.Predicate;

public final class AnyPredicate<T> extends AbstractQuantifierPredicate<T> {
  private static final long serialVersionUID = 7429999530934647542L;
  
  public static <T> Predicate<T> anyPredicate(Predicate<? super T>... predicates) {
    FunctorUtils.validate((Predicate<?>[])predicates);
    if (predicates.length == 0)
      return FalsePredicate.falsePredicate(); 
    if (predicates.length == 1)
      return (Predicate)predicates[0]; 
    return new AnyPredicate<T>((Predicate<? super T>[])FunctorUtils.copy(predicates));
  }
  
  public static <T> Predicate<T> anyPredicate(Collection<? extends Predicate<T>> predicates) {
    Predicate[] arrayOfPredicate = (Predicate[])FunctorUtils.validate(predicates);
    if (arrayOfPredicate.length == 0)
      return FalsePredicate.falsePredicate(); 
    if (arrayOfPredicate.length == 1)
      return arrayOfPredicate[0]; 
    return new AnyPredicate<T>((Predicate<? super T>[])arrayOfPredicate);
  }
  
  public AnyPredicate(Predicate<? super T>... predicates) {
    super(predicates);
  }
  
  public boolean evaluate(T object) {
    for (Predicate<? super T> iPredicate : this.iPredicates) {
      if (iPredicate.evaluate(object))
        return true; 
    } 
    return false;
  }
}

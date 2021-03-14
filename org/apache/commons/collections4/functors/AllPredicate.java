package org.apache.commons.collections4.functors;

import java.util.Collection;
import org.apache.commons.collections4.Predicate;

public final class AllPredicate<T> extends AbstractQuantifierPredicate<T> {
  private static final long serialVersionUID = -3094696765038308799L;
  
  public static <T> Predicate<T> allPredicate(Predicate<? super T>... predicates) {
    FunctorUtils.validate((Predicate<?>[])predicates);
    if (predicates.length == 0)
      return TruePredicate.truePredicate(); 
    if (predicates.length == 1)
      return FunctorUtils.coerce(predicates[0]); 
    return new AllPredicate<T>((Predicate<? super T>[])FunctorUtils.copy(predicates));
  }
  
  public static <T> Predicate<T> allPredicate(Collection<? extends Predicate<T>> predicates) {
    Predicate[] arrayOfPredicate = (Predicate[])FunctorUtils.validate(predicates);
    if (arrayOfPredicate.length == 0)
      return TruePredicate.truePredicate(); 
    if (arrayOfPredicate.length == 1)
      return arrayOfPredicate[0]; 
    return new AllPredicate<T>((Predicate<? super T>[])arrayOfPredicate);
  }
  
  public AllPredicate(Predicate<? super T>... predicates) {
    super(predicates);
  }
  
  public boolean evaluate(T object) {
    for (Predicate<? super T> iPredicate : this.iPredicates) {
      if (!iPredicate.evaluate(object))
        return false; 
    } 
    return true;
  }
}

package org.apache.commons.collections4.functors;

import java.util.Collection;
import org.apache.commons.collections4.Predicate;

public final class NonePredicate<T> extends AbstractQuantifierPredicate<T> {
  private static final long serialVersionUID = 2007613066565892961L;
  
  public static <T> Predicate<T> nonePredicate(Predicate<? super T>... predicates) {
    FunctorUtils.validate((Predicate<?>[])predicates);
    if (predicates.length == 0)
      return TruePredicate.truePredicate(); 
    return new NonePredicate<T>((Predicate<? super T>[])FunctorUtils.copy(predicates));
  }
  
  public static <T> Predicate<T> nonePredicate(Collection<? extends Predicate<T>> predicates) {
    Predicate[] arrayOfPredicate = (Predicate[])FunctorUtils.validate(predicates);
    if (arrayOfPredicate.length == 0)
      return TruePredicate.truePredicate(); 
    return new NonePredicate<T>((Predicate<? super T>[])arrayOfPredicate);
  }
  
  public NonePredicate(Predicate<? super T>... predicates) {
    super(predicates);
  }
  
  public boolean evaluate(T object) {
    for (Predicate<? super T> iPredicate : this.iPredicates) {
      if (iPredicate.evaluate(object))
        return false; 
    } 
    return true;
  }
}

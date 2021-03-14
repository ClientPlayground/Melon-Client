package org.apache.commons.collections4.functors;

import java.io.Serializable;
import java.util.Map;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.Predicate;

public class SwitchClosure<E> implements Closure<E>, Serializable {
  private static final long serialVersionUID = 3518477308466486130L;
  
  private final Predicate<? super E>[] iPredicates;
  
  private final Closure<? super E>[] iClosures;
  
  private final Closure<? super E> iDefault;
  
  public static <E> Closure<E> switchClosure(Predicate<? super E>[] predicates, Closure<? super E>[] closures, Closure<? super E> defaultClosure) {
    FunctorUtils.validate((Predicate<?>[])predicates);
    FunctorUtils.validate((Closure<?>[])closures);
    if (predicates.length != closures.length)
      throw new IllegalArgumentException("The predicate and closure arrays must be the same size"); 
    if (predicates.length == 0)
      return (defaultClosure == null) ? NOPClosure.<E>nopClosure() : (Closure)defaultClosure; 
    return new SwitchClosure<E>(predicates, closures, defaultClosure);
  }
  
  public static <E> Closure<E> switchClosure(Map<Predicate<E>, Closure<E>> predicatesAndClosures) {
    if (predicatesAndClosures == null)
      throw new IllegalArgumentException("The predicate and closure map must not be null"); 
    Closure<? super E> defaultClosure = predicatesAndClosures.remove(null);
    int size = predicatesAndClosures.size();
    if (size == 0)
      return (defaultClosure == null) ? NOPClosure.<E>nopClosure() : (Closure)defaultClosure; 
    Closure[] arrayOfClosure = new Closure[size];
    Predicate[] arrayOfPredicate = new Predicate[size];
    int i = 0;
    for (Map.Entry<Predicate<E>, Closure<E>> entry : predicatesAndClosures.entrySet()) {
      arrayOfPredicate[i] = entry.getKey();
      arrayOfClosure[i] = entry.getValue();
      i++;
    } 
    return new SwitchClosure<E>(false, (Predicate<? super E>[])arrayOfPredicate, (Closure<? super E>[])arrayOfClosure, defaultClosure);
  }
  
  private SwitchClosure(boolean clone, Predicate<? super E>[] predicates, Closure<? super E>[] closures, Closure<? super E> defaultClosure) {
    this.iPredicates = clone ? (Predicate<? super E>[])FunctorUtils.<E>copy(predicates) : predicates;
    this.iClosures = clone ? (Closure<? super E>[])FunctorUtils.<E>copy(closures) : closures;
    this.iDefault = (defaultClosure == null) ? NOPClosure.<E>nopClosure() : defaultClosure;
  }
  
  public SwitchClosure(Predicate<? super E>[] predicates, Closure<? super E>[] closures, Closure<? super E> defaultClosure) {
    this(true, predicates, closures, defaultClosure);
  }
  
  public void execute(E input) {
    for (int i = 0; i < this.iPredicates.length; i++) {
      if (this.iPredicates[i].evaluate(input) == true) {
        this.iClosures[i].execute(input);
        return;
      } 
    } 
    this.iDefault.execute(input);
  }
  
  public Predicate<? super E>[] getPredicates() {
    return (Predicate<? super E>[])FunctorUtils.copy(this.iPredicates);
  }
  
  public Closure<? super E>[] getClosures() {
    return (Closure<? super E>[])FunctorUtils.copy(this.iClosures);
  }
  
  public Closure<? super E> getDefaultClosure() {
    return this.iDefault;
  }
}

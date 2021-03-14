package org.apache.commons.collections4.functors;

import java.io.Serializable;
import java.util.Collection;
import org.apache.commons.collections4.Closure;

public class ChainedClosure<E> implements Closure<E>, Serializable {
  private static final long serialVersionUID = -3520677225766901240L;
  
  private final Closure<? super E>[] iClosures;
  
  public static <E> Closure<E> chainedClosure(Closure<? super E>... closures) {
    FunctorUtils.validate((Closure<?>[])closures);
    if (closures.length == 0)
      return NOPClosure.nopClosure(); 
    return new ChainedClosure<E>(closures);
  }
  
  public static <E> Closure<E> chainedClosure(Collection<Closure<E>> closures) {
    if (closures == null)
      throw new IllegalArgumentException("Closure collection must not be null"); 
    if (closures.size() == 0)
      return NOPClosure.nopClosure(); 
    Closure[] arrayOfClosure = new Closure[closures.size()];
    int i = 0;
    for (Closure<? super E> closure : closures)
      arrayOfClosure[i++] = closure; 
    FunctorUtils.validate((Closure<?>[])arrayOfClosure);
    return new ChainedClosure<E>(false, (Closure<? super E>[])arrayOfClosure);
  }
  
  private ChainedClosure(boolean clone, Closure<? super E>... closures) {
    this.iClosures = clone ? (Closure<? super E>[])FunctorUtils.<E>copy(closures) : closures;
  }
  
  public ChainedClosure(Closure<? super E>... closures) {
    this(true, closures);
  }
  
  public void execute(E input) {
    for (Closure<? super E> iClosure : this.iClosures)
      iClosure.execute(input); 
  }
  
  public Closure<? super E>[] getClosures() {
    return (Closure<? super E>[])FunctorUtils.copy(this.iClosures);
  }
}

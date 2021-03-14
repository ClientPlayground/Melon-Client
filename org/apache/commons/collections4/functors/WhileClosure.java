package org.apache.commons.collections4.functors;

import java.io.Serializable;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.Predicate;

public class WhileClosure<E> implements Closure<E>, Serializable {
  private static final long serialVersionUID = -3110538116913760108L;
  
  private final Predicate<? super E> iPredicate;
  
  private final Closure<? super E> iClosure;
  
  private final boolean iDoLoop;
  
  public static <E> Closure<E> whileClosure(Predicate<? super E> predicate, Closure<? super E> closure, boolean doLoop) {
    if (predicate == null)
      throw new IllegalArgumentException("Predicate must not be null"); 
    if (closure == null)
      throw new IllegalArgumentException("Closure must not be null"); 
    return new WhileClosure<E>(predicate, closure, doLoop);
  }
  
  public WhileClosure(Predicate<? super E> predicate, Closure<? super E> closure, boolean doLoop) {
    this.iPredicate = predicate;
    this.iClosure = closure;
    this.iDoLoop = doLoop;
  }
  
  public void execute(E input) {
    if (this.iDoLoop)
      this.iClosure.execute(input); 
    while (this.iPredicate.evaluate(input))
      this.iClosure.execute(input); 
  }
  
  public Predicate<? super E> getPredicate() {
    return this.iPredicate;
  }
  
  public Closure<? super E> getClosure() {
    return this.iClosure;
  }
  
  public boolean isDoLoop() {
    return this.iDoLoop;
  }
}

package org.apache.commons.collections4.functors;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.FunctorException;

public abstract class CatchAndRethrowClosure<E> implements Closure<E> {
  public void execute(E input) {
    try {
      executeAndThrow(input);
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Throwable t) {
      throw new FunctorException(t);
    } 
  }
  
  protected abstract void executeAndThrow(E paramE) throws Throwable;
}

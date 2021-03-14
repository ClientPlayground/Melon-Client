package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Iterator;

@GwtCompatible
public abstract class ForwardingIterator<T> extends ForwardingObject implements Iterator<T> {
  public boolean hasNext() {
    return delegate().hasNext();
  }
  
  public T next() {
    return delegate().next();
  }
  
  public void remove() {
    delegate().remove();
  }
  
  protected abstract Iterator<T> delegate();
}

package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.Iterator;

@GwtCompatible
public abstract class UnmodifiableIterator<E> implements Iterator<E> {
  @Deprecated
  public final void remove() {
    throw new UnsupportedOperationException();
  }
}

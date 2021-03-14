package com.google.common.util.concurrent;

import javax.annotation.Nullable;

public final class SettableFuture<V> extends AbstractFuture<V> {
  public static <V> SettableFuture<V> create() {
    return new SettableFuture<V>();
  }
  
  public boolean set(@Nullable V value) {
    return super.set(value);
  }
  
  public boolean setException(Throwable throwable) {
    return super.setException(throwable);
  }
}

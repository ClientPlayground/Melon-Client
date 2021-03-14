package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.PlatformDependent;

public final class FailedFuture<V> extends CompleteFuture<V> {
  private final Throwable cause;
  
  public FailedFuture(EventExecutor executor, Throwable cause) {
    super(executor);
    if (cause == null)
      throw new NullPointerException("cause"); 
    this.cause = cause;
  }
  
  public Throwable cause() {
    return this.cause;
  }
  
  public boolean isSuccess() {
    return false;
  }
  
  public Future<V> sync() {
    PlatformDependent.throwException(this.cause);
    return this;
  }
  
  public Future<V> syncUninterruptibly() {
    PlatformDependent.throwException(this.cause);
    return this;
  }
  
  public V getNow() {
    return null;
  }
}

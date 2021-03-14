package com.github.steveice10.netty.util.concurrent;

public final class SucceededFuture<V> extends CompleteFuture<V> {
  private final V result;
  
  public SucceededFuture(EventExecutor executor, V result) {
    super(executor);
    this.result = result;
  }
  
  public Throwable cause() {
    return null;
  }
  
  public boolean isSuccess() {
    return true;
  }
  
  public V getNow() {
    return this.result;
  }
}

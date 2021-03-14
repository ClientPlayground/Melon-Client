package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.ObjectUtil;

public final class PromiseCombiner {
  private int expectedCount;
  
  private int doneCount;
  
  private boolean doneAdding;
  
  private Promise<Void> aggregatePromise;
  
  private Throwable cause;
  
  private final GenericFutureListener<Future<?>> listener = new GenericFutureListener<Future<?>>() {
      public void operationComplete(Future<?> future) throws Exception {
        ++PromiseCombiner.this.doneCount;
        if (!future.isSuccess() && PromiseCombiner.this.cause == null)
          PromiseCombiner.this.cause = future.cause(); 
        if (PromiseCombiner.this.doneCount == PromiseCombiner.this.expectedCount && PromiseCombiner.this.doneAdding)
          PromiseCombiner.this.tryPromise(); 
      }
    };
  
  @Deprecated
  public void add(Promise promise) {
    add(promise);
  }
  
  public void add(Future<?> future) {
    checkAddAllowed();
    this.expectedCount++;
    future.addListener(this.listener);
  }
  
  @Deprecated
  public void addAll(Promise... promises) {
    addAll((Future[])promises);
  }
  
  public void addAll(Future... futures) {
    for (Future future : futures)
      add(future); 
  }
  
  public void finish(Promise<Void> aggregatePromise) {
    if (this.doneAdding)
      throw new IllegalStateException("Already finished"); 
    this.doneAdding = true;
    this.aggregatePromise = (Promise<Void>)ObjectUtil.checkNotNull(aggregatePromise, "aggregatePromise");
    if (this.doneCount == this.expectedCount)
      tryPromise(); 
  }
  
  private boolean tryPromise() {
    return (this.cause == null) ? this.aggregatePromise.trySuccess(null) : this.aggregatePromise.tryFailure(this.cause);
  }
  
  private void checkAddAllowed() {
    if (this.doneAdding)
      throw new IllegalStateException("Adding promises is not allowed after finished adding"); 
  }
}

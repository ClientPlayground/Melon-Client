package com.github.steveice10.netty.util.concurrent;

public interface ProgressivePromise<V> extends Promise<V>, ProgressiveFuture<V> {
  ProgressivePromise<V> setProgress(long paramLong1, long paramLong2);
  
  boolean tryProgress(long paramLong1, long paramLong2);
  
  ProgressivePromise<V> setSuccess(V paramV);
  
  ProgressivePromise<V> setFailure(Throwable paramThrowable);
  
  ProgressivePromise<V> addListener(GenericFutureListener<? extends Future<? super V>> paramGenericFutureListener);
  
  ProgressivePromise<V> addListeners(GenericFutureListener<? extends Future<? super V>>... paramVarArgs);
  
  ProgressivePromise<V> removeListener(GenericFutureListener<? extends Future<? super V>> paramGenericFutureListener);
  
  ProgressivePromise<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... paramVarArgs);
  
  ProgressivePromise<V> await() throws InterruptedException;
  
  ProgressivePromise<V> awaitUninterruptibly();
  
  ProgressivePromise<V> sync() throws InterruptedException;
  
  ProgressivePromise<V> syncUninterruptibly();
}

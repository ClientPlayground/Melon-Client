package com.github.steveice10.netty.util.concurrent;

public interface ProgressiveFuture<V> extends Future<V> {
  ProgressiveFuture<V> addListener(GenericFutureListener<? extends Future<? super V>> paramGenericFutureListener);
  
  ProgressiveFuture<V> addListeners(GenericFutureListener<? extends Future<? super V>>... paramVarArgs);
  
  ProgressiveFuture<V> removeListener(GenericFutureListener<? extends Future<? super V>> paramGenericFutureListener);
  
  ProgressiveFuture<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... paramVarArgs);
  
  ProgressiveFuture<V> sync() throws InterruptedException;
  
  ProgressiveFuture<V> syncUninterruptibly();
  
  ProgressiveFuture<V> await() throws InterruptedException;
  
  ProgressiveFuture<V> awaitUninterruptibly();
}

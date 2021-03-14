package com.github.steveice10.netty.util.concurrent;

public interface EventExecutor extends EventExecutorGroup {
  EventExecutor next();
  
  EventExecutorGroup parent();
  
  boolean inEventLoop();
  
  boolean inEventLoop(Thread paramThread);
  
  <V> Promise<V> newPromise();
  
  <V> ProgressivePromise<V> newProgressivePromise();
  
  <V> Future<V> newSucceededFuture(V paramV);
  
  <V> Future<V> newFailedFuture(Throwable paramThrowable);
}

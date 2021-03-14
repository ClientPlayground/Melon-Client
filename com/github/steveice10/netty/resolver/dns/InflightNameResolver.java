package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.resolver.NameResolver;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.FutureListener;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

final class InflightNameResolver<T> implements NameResolver<T> {
  private final EventExecutor executor;
  
  private final NameResolver<T> delegate;
  
  private final ConcurrentMap<String, Promise<T>> resolvesInProgress;
  
  private final ConcurrentMap<String, Promise<List<T>>> resolveAllsInProgress;
  
  InflightNameResolver(EventExecutor executor, NameResolver<T> delegate, ConcurrentMap<String, Promise<T>> resolvesInProgress, ConcurrentMap<String, Promise<List<T>>> resolveAllsInProgress) {
    this.executor = (EventExecutor)ObjectUtil.checkNotNull(executor, "executor");
    this.delegate = (NameResolver<T>)ObjectUtil.checkNotNull(delegate, "delegate");
    this.resolvesInProgress = (ConcurrentMap<String, Promise<T>>)ObjectUtil.checkNotNull(resolvesInProgress, "resolvesInProgress");
    this.resolveAllsInProgress = (ConcurrentMap<String, Promise<List<T>>>)ObjectUtil.checkNotNull(resolveAllsInProgress, "resolveAllsInProgress");
  }
  
  public Future<T> resolve(String inetHost) {
    return (Future<T>)resolve(inetHost, this.executor.newPromise());
  }
  
  public Future<List<T>> resolveAll(String inetHost) {
    return (Future<List<T>>)resolveAll(inetHost, this.executor.newPromise());
  }
  
  public void close() {
    this.delegate.close();
  }
  
  public Promise<T> resolve(String inetHost, Promise<T> promise) {
    return resolve(this.resolvesInProgress, inetHost, promise, false);
  }
  
  public Promise<List<T>> resolveAll(String inetHost, Promise<List<T>> promise) {
    return resolve(this.resolveAllsInProgress, inetHost, promise, true);
  }
  
  private <U> Promise<U> resolve(final ConcurrentMap<String, Promise<U>> resolveMap, final String inetHost, final Promise<U> promise, boolean resolveAll) {
    Promise<U> earlyPromise = resolveMap.putIfAbsent(inetHost, promise);
    if (earlyPromise != null) {
      if (earlyPromise.isDone()) {
        transferResult((Future<U>)earlyPromise, promise);
      } else {
        earlyPromise.addListener((GenericFutureListener)new FutureListener<U>() {
              public void operationComplete(Future<U> f) throws Exception {
                InflightNameResolver.transferResult(f, promise);
              }
            });
      } 
    } else {
      try {
        if (resolveAll) {
          Promise<U> promise1 = promise;
          this.delegate.resolveAll(inetHost, promise1);
        } else {
          Promise<T> castPromise = promise;
          this.delegate.resolve(inetHost, castPromise);
        } 
      } finally {
        if (promise.isDone()) {
          resolveMap.remove(inetHost);
        } else {
          promise.addListener((GenericFutureListener)new FutureListener<U>() {
                public void operationComplete(Future<U> f) throws Exception {
                  resolveMap.remove(inetHost);
                }
              });
        } 
      } 
    } 
    return promise;
  }
  
  private static <T> void transferResult(Future<T> src, Promise<T> dst) {
    if (src.isSuccess()) {
      dst.trySuccess(src.getNow());
    } else {
      dst.tryFailure(src.cause());
    } 
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '(' + this.delegate + ')';
  }
}

package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.List;

public abstract class SimpleNameResolver<T> implements NameResolver<T> {
  private final EventExecutor executor;
  
  protected SimpleNameResolver(EventExecutor executor) {
    this.executor = (EventExecutor)ObjectUtil.checkNotNull(executor, "executor");
  }
  
  protected EventExecutor executor() {
    return this.executor;
  }
  
  public final Future<T> resolve(String inetHost) {
    Promise<T> promise = executor().newPromise();
    return resolve(inetHost, promise);
  }
  
  public Future<T> resolve(String inetHost, Promise<T> promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    try {
      doResolve(inetHost, promise);
      return (Future<T>)promise;
    } catch (Exception e) {
      return (Future<T>)promise.setFailure(e);
    } 
  }
  
  public final Future<List<T>> resolveAll(String inetHost) {
    Promise<List<T>> promise = executor().newPromise();
    return resolveAll(inetHost, promise);
  }
  
  public Future<List<T>> resolveAll(String inetHost, Promise<List<T>> promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    try {
      doResolveAll(inetHost, promise);
      return (Future<List<T>>)promise;
    } catch (Exception e) {
      return (Future<List<T>>)promise.setFailure(e);
    } 
  }
  
  protected abstract void doResolve(String paramString, Promise<T> paramPromise) throws Exception;
  
  protected abstract void doResolveAll(String paramString, Promise<List<T>> paramPromise) throws Exception;
  
  public void close() {}
}

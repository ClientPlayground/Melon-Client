package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.FutureListener;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.Arrays;
import java.util.List;

public final class CompositeNameResolver<T> extends SimpleNameResolver<T> {
  private final NameResolver<T>[] resolvers;
  
  public CompositeNameResolver(EventExecutor executor, NameResolver<T>... resolvers) {
    super(executor);
    ObjectUtil.checkNotNull(resolvers, "resolvers");
    for (int i = 0; i < resolvers.length; i++) {
      if (resolvers[i] == null)
        throw new NullPointerException("resolvers[" + i + ']'); 
    } 
    if (resolvers.length < 2)
      throw new IllegalArgumentException("resolvers: " + Arrays.asList(resolvers) + " (expected: at least 2 resolvers)"); 
    this.resolvers = (NameResolver<T>[])resolvers.clone();
  }
  
  protected void doResolve(String inetHost, Promise<T> promise) throws Exception {
    doResolveRec(inetHost, promise, 0, null);
  }
  
  private void doResolveRec(final String inetHost, final Promise<T> promise, final int resolverIndex, Throwable lastFailure) throws Exception {
    if (resolverIndex >= this.resolvers.length) {
      promise.setFailure(lastFailure);
    } else {
      NameResolver<T> resolver = this.resolvers[resolverIndex];
      resolver.resolve(inetHost).addListener((GenericFutureListener)new FutureListener<T>() {
            public void operationComplete(Future<T> future) throws Exception {
              if (future.isSuccess()) {
                promise.setSuccess(future.getNow());
              } else {
                CompositeNameResolver.this.doResolveRec(inetHost, promise, resolverIndex + 1, future.cause());
              } 
            }
          });
    } 
  }
  
  protected void doResolveAll(String inetHost, Promise<List<T>> promise) throws Exception {
    doResolveAllRec(inetHost, promise, 0, null);
  }
  
  private void doResolveAllRec(final String inetHost, final Promise<List<T>> promise, final int resolverIndex, Throwable lastFailure) throws Exception {
    if (resolverIndex >= this.resolvers.length) {
      promise.setFailure(lastFailure);
    } else {
      NameResolver<T> resolver = this.resolvers[resolverIndex];
      resolver.resolveAll(inetHost).addListener((GenericFutureListener)new FutureListener<List<T>>() {
            public void operationComplete(Future<List<T>> future) throws Exception {
              if (future.isSuccess()) {
                promise.setSuccess(future.getNow());
              } else {
                CompositeNameResolver.this.doResolveAllRec(inetHost, promise, resolverIndex + 1, future.cause());
              } 
            }
          });
    } 
  }
}

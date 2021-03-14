package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.FutureListener;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.Closeable;
import java.net.SocketAddress;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class AddressResolverGroup<T extends SocketAddress> implements Closeable {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AddressResolverGroup.class);
  
  private final Map<EventExecutor, AddressResolver<T>> resolvers = new IdentityHashMap<EventExecutor, AddressResolver<T>>();
  
  public AddressResolver<T> getResolver(final EventExecutor executor) {
    AddressResolver<T> r;
    if (executor == null)
      throw new NullPointerException("executor"); 
    if (executor.isShuttingDown())
      throw new IllegalStateException("executor not accepting a task"); 
    synchronized (this.resolvers) {
      r = this.resolvers.get(executor);
      if (r == null) {
        final AddressResolver<T> newResolver;
        try {
          newResolver = newResolver(executor);
        } catch (Exception e) {
          throw new IllegalStateException("failed to create a new resolver", e);
        } 
        this.resolvers.put(executor, newResolver);
        executor.terminationFuture().addListener((GenericFutureListener)new FutureListener<Object>() {
              public void operationComplete(Future<Object> future) throws Exception {
                synchronized (AddressResolverGroup.this.resolvers) {
                  AddressResolverGroup.this.resolvers.remove(executor);
                } 
                newResolver.close();
              }
            });
        r = newResolver;
      } 
    } 
    return r;
  }
  
  public void close() {
    AddressResolver[] arrayOfAddressResolver;
    synchronized (this.resolvers) {
      arrayOfAddressResolver = (AddressResolver[])this.resolvers.values().toArray((Object[])new AddressResolver[this.resolvers.size()]);
      this.resolvers.clear();
    } 
    for (AddressResolver<T> r : arrayOfAddressResolver) {
      try {
        r.close();
      } catch (Throwable t) {
        logger.warn("Failed to close a resolver:", t);
      } 
    } 
  }
  
  protected abstract AddressResolver<T> newResolver(EventExecutor paramEventExecutor) throws Exception;
}

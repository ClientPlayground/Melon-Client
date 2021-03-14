package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.TypeParameterMatcher;
import java.net.SocketAddress;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Collections;
import java.util.List;

public abstract class AbstractAddressResolver<T extends SocketAddress> implements AddressResolver<T> {
  private final EventExecutor executor;
  
  private final TypeParameterMatcher matcher;
  
  protected AbstractAddressResolver(EventExecutor executor) {
    this.executor = (EventExecutor)ObjectUtil.checkNotNull(executor, "executor");
    this.matcher = TypeParameterMatcher.find(this, AbstractAddressResolver.class, "T");
  }
  
  protected AbstractAddressResolver(EventExecutor executor, Class<? extends T> addressType) {
    this.executor = (EventExecutor)ObjectUtil.checkNotNull(executor, "executor");
    this.matcher = TypeParameterMatcher.get(addressType);
  }
  
  protected EventExecutor executor() {
    return this.executor;
  }
  
  public boolean isSupported(SocketAddress address) {
    return this.matcher.match(address);
  }
  
  public final boolean isResolved(SocketAddress address) {
    if (!isSupported(address))
      throw new UnsupportedAddressTypeException(); 
    SocketAddress socketAddress = address;
    return doIsResolved((T)socketAddress);
  }
  
  protected abstract boolean doIsResolved(T paramT);
  
  public final Future<T> resolve(SocketAddress address) {
    if (!isSupported((SocketAddress)ObjectUtil.checkNotNull(address, "address")))
      return executor().newFailedFuture(new UnsupportedAddressTypeException()); 
    if (isResolved(address)) {
      SocketAddress socketAddress = address;
      return this.executor.newSucceededFuture(socketAddress);
    } 
    try {
      SocketAddress socketAddress = address;
      Promise<T> promise = executor().newPromise();
      doResolve((T)socketAddress, promise);
      return (Future<T>)promise;
    } catch (Exception e) {
      return executor().newFailedFuture(e);
    } 
  }
  
  public final Future<T> resolve(SocketAddress address, Promise<T> promise) {
    ObjectUtil.checkNotNull(address, "address");
    ObjectUtil.checkNotNull(promise, "promise");
    if (!isSupported(address))
      return (Future<T>)promise.setFailure(new UnsupportedAddressTypeException()); 
    if (isResolved(address)) {
      SocketAddress socketAddress = address;
      return (Future<T>)promise.setSuccess(socketAddress);
    } 
    try {
      SocketAddress socketAddress = address;
      doResolve((T)socketAddress, promise);
      return (Future<T>)promise;
    } catch (Exception e) {
      return (Future<T>)promise.setFailure(e);
    } 
  }
  
  public final Future<List<T>> resolveAll(SocketAddress address) {
    if (!isSupported((SocketAddress)ObjectUtil.checkNotNull(address, "address")))
      return executor().newFailedFuture(new UnsupportedAddressTypeException()); 
    if (isResolved(address)) {
      SocketAddress socketAddress = address;
      return this.executor.newSucceededFuture(Collections.singletonList(socketAddress));
    } 
    try {
      SocketAddress socketAddress = address;
      Promise<List<T>> promise = executor().newPromise();
      doResolveAll((T)socketAddress, promise);
      return (Future<List<T>>)promise;
    } catch (Exception e) {
      return executor().newFailedFuture(e);
    } 
  }
  
  public final Future<List<T>> resolveAll(SocketAddress address, Promise<List<T>> promise) {
    ObjectUtil.checkNotNull(address, "address");
    ObjectUtil.checkNotNull(promise, "promise");
    if (!isSupported(address))
      return (Future<List<T>>)promise.setFailure(new UnsupportedAddressTypeException()); 
    if (isResolved(address)) {
      SocketAddress socketAddress = address;
      return (Future<List<T>>)promise.setSuccess(Collections.singletonList(socketAddress));
    } 
    try {
      SocketAddress socketAddress = address;
      doResolveAll((T)socketAddress, promise);
      return (Future<List<T>>)promise;
    } catch (Exception e) {
      return (Future<List<T>>)promise.setFailure(e);
    } 
  }
  
  protected abstract void doResolve(T paramT, Promise<T> paramPromise) throws Exception;
  
  protected abstract void doResolveAll(T paramT, Promise<List<T>> paramPromise) throws Exception;
  
  public void close() {}
}

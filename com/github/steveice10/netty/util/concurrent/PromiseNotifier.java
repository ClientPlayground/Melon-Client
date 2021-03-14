package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PromiseNotificationUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;

public class PromiseNotifier<V, F extends Future<V>> implements GenericFutureListener<F> {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(PromiseNotifier.class);
  
  private final Promise<? super V>[] promises;
  
  private final boolean logNotifyFailure;
  
  @SafeVarargs
  public PromiseNotifier(Promise<? super V>... promises) {
    this(true, promises);
  }
  
  @SafeVarargs
  public PromiseNotifier(boolean logNotifyFailure, Promise<? super V>... promises) {
    ObjectUtil.checkNotNull(promises, "promises");
    for (Promise<? super V> promise : promises) {
      if (promise == null)
        throw new IllegalArgumentException("promises contains null Promise"); 
    } 
    this.promises = (Promise<? super V>[])promises.clone();
    this.logNotifyFailure = logNotifyFailure;
  }
  
  public void operationComplete(F future) throws Exception {
    InternalLogger internalLogger = this.logNotifyFailure ? logger : null;
    if (future.isSuccess()) {
      V result = future.get();
      for (Promise<? super V> p : this.promises)
        PromiseNotificationUtil.trySuccess(p, result, internalLogger); 
    } else if (future.isCancelled()) {
      for (Promise<? super V> p : this.promises)
        PromiseNotificationUtil.tryCancel(p, internalLogger); 
    } else {
      Throwable cause = future.cause();
      for (Promise<? super V> p : this.promises)
        PromiseNotificationUtil.tryFailure(p, cause, internalLogger); 
    } 
  }
}

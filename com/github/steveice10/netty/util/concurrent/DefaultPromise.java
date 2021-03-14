package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.InternalThreadLocalMap;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class DefaultPromise<V> extends AbstractFuture<V> implements Promise<V> {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultPromise.class);
  
  private static final InternalLogger rejectedExecutionLogger = InternalLoggerFactory.getInstance(DefaultPromise.class.getName() + ".rejectedExecution");
  
  private static final int MAX_LISTENER_STACK_DEPTH = Math.min(8, 
      SystemPropertyUtil.getInt("com.github.steveice10.netty.defaultPromise.maxListenerStackDepth", 8));
  
  private static final AtomicReferenceFieldUpdater<DefaultPromise, Object> RESULT_UPDATER = AtomicReferenceFieldUpdater.newUpdater(DefaultPromise.class, Object.class, "result");
  
  private static final Object SUCCESS = new Object();
  
  private static final Object UNCANCELLABLE = new Object();
  
  private static final CauseHolder CANCELLATION_CAUSE_HOLDER = new CauseHolder(ThrowableUtil.unknownStackTrace(new CancellationException(), DefaultPromise.class, "cancel(...)"));
  
  private volatile Object result;
  
  private final EventExecutor executor;
  
  private Object listeners;
  
  private short waiters;
  
  private boolean notifyingListeners;
  
  public DefaultPromise(EventExecutor executor) {
    this.executor = (EventExecutor)ObjectUtil.checkNotNull(executor, "executor");
  }
  
  protected DefaultPromise() {
    this.executor = null;
  }
  
  public Promise<V> setSuccess(V result) {
    if (setSuccess0(result)) {
      notifyListeners();
      return this;
    } 
    throw new IllegalStateException("complete already: " + this);
  }
  
  public boolean trySuccess(V result) {
    if (setSuccess0(result)) {
      notifyListeners();
      return true;
    } 
    return false;
  }
  
  public Promise<V> setFailure(Throwable cause) {
    if (setFailure0(cause)) {
      notifyListeners();
      return this;
    } 
    throw new IllegalStateException("complete already: " + this, cause);
  }
  
  public boolean tryFailure(Throwable cause) {
    if (setFailure0(cause)) {
      notifyListeners();
      return true;
    } 
    return false;
  }
  
  public boolean setUncancellable() {
    if (RESULT_UPDATER.compareAndSet(this, null, UNCANCELLABLE))
      return true; 
    Object result = this.result;
    return (!isDone0(result) || !isCancelled0(result));
  }
  
  public boolean isSuccess() {
    Object result = this.result;
    return (result != null && result != UNCANCELLABLE && !(result instanceof CauseHolder));
  }
  
  public boolean isCancellable() {
    return (this.result == null);
  }
  
  public Throwable cause() {
    Object result = this.result;
    return (result instanceof CauseHolder) ? ((CauseHolder)result).cause : null;
  }
  
  public Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener) {
    ObjectUtil.checkNotNull(listener, "listener");
    synchronized (this) {
      addListener0(listener);
    } 
    if (isDone())
      notifyListeners(); 
    return this;
  }
  
  public Promise<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners) {
    ObjectUtil.checkNotNull(listeners, "listeners");
    synchronized (this) {
      for (GenericFutureListener<? extends Future<? super V>> listener : listeners) {
        if (listener == null)
          break; 
        addListener0(listener);
      } 
    } 
    if (isDone())
      notifyListeners(); 
    return this;
  }
  
  public Promise<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener) {
    ObjectUtil.checkNotNull(listener, "listener");
    synchronized (this) {
      removeListener0(listener);
    } 
    return this;
  }
  
  public Promise<V> removeListeners(GenericFutureListener<? extends Future<? super V>>... listeners) {
    ObjectUtil.checkNotNull(listeners, "listeners");
    synchronized (this) {
      for (GenericFutureListener<? extends Future<? super V>> listener : listeners) {
        if (listener == null)
          break; 
        removeListener0(listener);
      } 
    } 
    return this;
  }
  
  public Promise<V> await() throws InterruptedException {
    if (isDone())
      return this; 
    if (Thread.interrupted())
      throw new InterruptedException(toString()); 
    checkDeadLock();
    synchronized (this) {
      while (!isDone()) {
        incWaiters();
        try {
          wait();
        } finally {
          decWaiters();
        } 
      } 
    } 
    return this;
  }
  
  public Promise<V> awaitUninterruptibly() {
    if (isDone())
      return this; 
    checkDeadLock();
    boolean interrupted = false;
    synchronized (this) {
      while (!isDone()) {
        incWaiters();
        try {
          wait();
        } catch (InterruptedException e) {
          interrupted = true;
        } finally {
          decWaiters();
        } 
      } 
    } 
    if (interrupted)
      Thread.currentThread().interrupt(); 
    return this;
  }
  
  public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
    return await0(unit.toNanos(timeout), true);
  }
  
  public boolean await(long timeoutMillis) throws InterruptedException {
    return await0(TimeUnit.MILLISECONDS.toNanos(timeoutMillis), true);
  }
  
  public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
    try {
      return await0(unit.toNanos(timeout), false);
    } catch (InterruptedException e) {
      throw new InternalError();
    } 
  }
  
  public boolean awaitUninterruptibly(long timeoutMillis) {
    try {
      return await0(TimeUnit.MILLISECONDS.toNanos(timeoutMillis), false);
    } catch (InterruptedException e) {
      throw new InternalError();
    } 
  }
  
  public V getNow() {
    Object result = this.result;
    if (result instanceof CauseHolder || result == SUCCESS)
      return null; 
    return (V)result;
  }
  
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (RESULT_UPDATER.compareAndSet(this, null, CANCELLATION_CAUSE_HOLDER)) {
      checkNotifyWaiters();
      notifyListeners();
      return true;
    } 
    return false;
  }
  
  public boolean isCancelled() {
    return isCancelled0(this.result);
  }
  
  public boolean isDone() {
    return isDone0(this.result);
  }
  
  public Promise<V> sync() throws InterruptedException {
    await();
    rethrowIfFailed();
    return this;
  }
  
  public Promise<V> syncUninterruptibly() {
    awaitUninterruptibly();
    rethrowIfFailed();
    return this;
  }
  
  public String toString() {
    return toStringBuilder().toString();
  }
  
  protected StringBuilder toStringBuilder() {
    StringBuilder buf = (new StringBuilder(64)).append(StringUtil.simpleClassName(this)).append('@').append(Integer.toHexString(hashCode()));
    Object result = this.result;
    if (result == SUCCESS) {
      buf.append("(success)");
    } else if (result == UNCANCELLABLE) {
      buf.append("(uncancellable)");
    } else if (result instanceof CauseHolder) {
      buf.append("(failure: ")
        .append(((CauseHolder)result).cause)
        .append(')');
    } else if (result != null) {
      buf.append("(success: ")
        .append(result)
        .append(')');
    } else {
      buf.append("(incomplete)");
    } 
    return buf;
  }
  
  protected EventExecutor executor() {
    return this.executor;
  }
  
  protected void checkDeadLock() {
    EventExecutor e = executor();
    if (e != null && e.inEventLoop())
      throw new BlockingOperationException(toString()); 
  }
  
  protected static void notifyListener(EventExecutor eventExecutor, Future<?> future, GenericFutureListener<?> listener) {
    ObjectUtil.checkNotNull(eventExecutor, "eventExecutor");
    ObjectUtil.checkNotNull(future, "future");
    ObjectUtil.checkNotNull(listener, "listener");
    notifyListenerWithStackOverFlowProtection(eventExecutor, future, listener);
  }
  
  private void notifyListeners() {
    EventExecutor executor = executor();
    if (executor.inEventLoop()) {
      InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
      int stackDepth = threadLocals.futureListenerStackDepth();
      if (stackDepth < MAX_LISTENER_STACK_DEPTH) {
        threadLocals.setFutureListenerStackDepth(stackDepth + 1);
        try {
          notifyListenersNow();
        } finally {
          threadLocals.setFutureListenerStackDepth(stackDepth);
        } 
        return;
      } 
    } 
    safeExecute(executor, new Runnable() {
          public void run() {
            DefaultPromise.this.notifyListenersNow();
          }
        });
  }
  
  private static void notifyListenerWithStackOverFlowProtection(EventExecutor executor, final Future<?> future, final GenericFutureListener<?> listener) {
    if (executor.inEventLoop()) {
      InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
      int stackDepth = threadLocals.futureListenerStackDepth();
      if (stackDepth < MAX_LISTENER_STACK_DEPTH) {
        threadLocals.setFutureListenerStackDepth(stackDepth + 1);
        try {
          notifyListener0(future, listener);
        } finally {
          threadLocals.setFutureListenerStackDepth(stackDepth);
        } 
        return;
      } 
    } 
    safeExecute(executor, new Runnable() {
          public void run() {
            DefaultPromise.notifyListener0(future, listener);
          }
        });
  }
  
  private void notifyListenersNow() {
    Object listeners;
    synchronized (this) {
      if (this.notifyingListeners || this.listeners == null)
        return; 
      this.notifyingListeners = true;
      listeners = this.listeners;
      this.listeners = null;
    } 
    while (true) {
      if (listeners instanceof DefaultFutureListeners) {
        notifyListeners0((DefaultFutureListeners)listeners);
      } else {
        notifyListener0(this, (GenericFutureListener)listeners);
      } 
      synchronized (this) {
        if (this.listeners == null) {
          this.notifyingListeners = false;
          return;
        } 
        listeners = this.listeners;
        this.listeners = null;
      } 
    } 
  }
  
  private void notifyListeners0(DefaultFutureListeners listeners) {
    GenericFutureListener[] arrayOfGenericFutureListener = (GenericFutureListener[])listeners.listeners();
    int size = listeners.size();
    for (int i = 0; i < size; i++)
      notifyListener0(this, arrayOfGenericFutureListener[i]); 
  }
  
  private static void notifyListener0(Future future, GenericFutureListener<Future> l) {
    try {
      l.operationComplete(future);
    } catch (Throwable t) {
      logger.warn("An exception was thrown by " + l.getClass().getName() + ".operationComplete()", t);
    } 
  }
  
  private void addListener0(GenericFutureListener<? extends Future<? super V>> listener) {
    if (this.listeners == null) {
      this.listeners = listener;
    } else if (this.listeners instanceof DefaultFutureListeners) {
      ((DefaultFutureListeners)this.listeners).add(listener);
    } else {
      this.listeners = new DefaultFutureListeners((GenericFutureListener<? extends Future<?>>)this.listeners, listener);
    } 
  }
  
  private void removeListener0(GenericFutureListener<? extends Future<? super V>> listener) {
    if (this.listeners instanceof DefaultFutureListeners) {
      ((DefaultFutureListeners)this.listeners).remove(listener);
    } else if (this.listeners == listener) {
      this.listeners = null;
    } 
  }
  
  private boolean setSuccess0(V result) {
    return setValue0((result == null) ? SUCCESS : result);
  }
  
  private boolean setFailure0(Throwable cause) {
    return setValue0(new CauseHolder((Throwable)ObjectUtil.checkNotNull(cause, "cause")));
  }
  
  private boolean setValue0(Object objResult) {
    if (RESULT_UPDATER.compareAndSet(this, null, objResult) || RESULT_UPDATER
      .compareAndSet(this, UNCANCELLABLE, objResult)) {
      checkNotifyWaiters();
      return true;
    } 
    return false;
  }
  
  private synchronized void checkNotifyWaiters() {
    if (this.waiters > 0)
      notifyAll(); 
  }
  
  private void incWaiters() {
    if (this.waiters == Short.MAX_VALUE)
      throw new IllegalStateException("too many waiters: " + this); 
    this.waiters = (short)(this.waiters + 1);
  }
  
  private void decWaiters() {
    this.waiters = (short)(this.waiters - 1);
  }
  
  private void rethrowIfFailed() {
    Throwable cause = cause();
    if (cause == null)
      return; 
    PlatformDependent.throwException(cause);
  }
  
  private boolean await0(long timeoutNanos, boolean interruptable) throws InterruptedException {
    if (isDone())
      return true; 
    if (timeoutNanos <= 0L)
      return isDone(); 
    if (interruptable && Thread.interrupted())
      throw new InterruptedException(toString()); 
    checkDeadLock();
    long startTime = System.nanoTime();
    long waitTime = timeoutNanos;
    boolean interrupted = false;
    try {
      while (true) {
        synchronized (this) {
          if (isDone())
            return true; 
          incWaiters();
          try {
            wait(waitTime / 1000000L, (int)(waitTime % 1000000L));
          } catch (InterruptedException e) {
            if (interruptable)
              throw e; 
            interrupted = true;
          } finally {
            decWaiters();
          } 
        } 
        if (isDone())
          return true; 
        waitTime = timeoutNanos - System.nanoTime() - startTime;
        if (waitTime <= 0L)
          return isDone(); 
      } 
    } finally {
      if (interrupted)
        Thread.currentThread().interrupt(); 
    } 
  }
  
  void notifyProgressiveListeners(final long progress, final long total) {
    Object listeners = progressiveListeners();
    if (listeners == null)
      return; 
    final ProgressiveFuture<V> self = (ProgressiveFuture<V>)this;
    EventExecutor executor = executor();
    if (executor.inEventLoop()) {
      if (listeners instanceof GenericProgressiveFutureListener[]) {
        notifyProgressiveListeners0(self, (GenericProgressiveFutureListener<?>[])listeners, progress, total);
      } else {
        notifyProgressiveListener0(self, (GenericProgressiveFutureListener)listeners, progress, total);
      } 
    } else if (listeners instanceof GenericProgressiveFutureListener[]) {
      final GenericProgressiveFutureListener[] array = (GenericProgressiveFutureListener[])listeners;
      safeExecute(executor, new Runnable() {
            public void run() {
              DefaultPromise.notifyProgressiveListeners0(self, (GenericProgressiveFutureListener<?>[])array, progress, total);
            }
          });
    } else {
      final GenericProgressiveFutureListener<ProgressiveFuture<V>> l = (GenericProgressiveFutureListener<ProgressiveFuture<V>>)listeners;
      safeExecute(executor, new Runnable() {
            public void run() {
              DefaultPromise.notifyProgressiveListener0(self, l, progress, total);
            }
          });
    } 
  }
  
  private synchronized Object progressiveListeners() {
    Object listeners = this.listeners;
    if (listeners == null)
      return null; 
    if (listeners instanceof DefaultFutureListeners) {
      DefaultFutureListeners dfl = (DefaultFutureListeners)listeners;
      int progressiveSize = dfl.progressiveSize();
      switch (progressiveSize) {
        case 0:
          return null;
        case 1:
          for (GenericFutureListener<?> l : dfl.listeners()) {
            if (l instanceof GenericProgressiveFutureListener)
              return l; 
          } 
          return null;
      } 
      GenericFutureListener[] arrayOfGenericFutureListener = (GenericFutureListener[])dfl.listeners();
      GenericProgressiveFutureListener[] arrayOfGenericProgressiveFutureListener = new GenericProgressiveFutureListener[progressiveSize];
      for (int i = 0, j = 0; j < progressiveSize; i++) {
        GenericFutureListener<?> l = arrayOfGenericFutureListener[i];
        if (l instanceof GenericProgressiveFutureListener)
          arrayOfGenericProgressiveFutureListener[j++] = (GenericProgressiveFutureListener)l; 
      } 
      return arrayOfGenericProgressiveFutureListener;
    } 
    if (listeners instanceof GenericProgressiveFutureListener)
      return listeners; 
    return null;
  }
  
  private static void notifyProgressiveListeners0(ProgressiveFuture<?> future, GenericProgressiveFutureListener<?>[] listeners, long progress, long total) {
    for (GenericProgressiveFutureListener<?> l : listeners) {
      if (l == null)
        break; 
      notifyProgressiveListener0(future, l, progress, total);
    } 
  }
  
  private static void notifyProgressiveListener0(ProgressiveFuture future, GenericProgressiveFutureListener<ProgressiveFuture> l, long progress, long total) {
    try {
      l.operationProgressed(future, progress, total);
    } catch (Throwable t) {
      logger.warn("An exception was thrown by " + l.getClass().getName() + ".operationProgressed()", t);
    } 
  }
  
  private static boolean isCancelled0(Object result) {
    return (result instanceof CauseHolder && ((CauseHolder)result).cause instanceof CancellationException);
  }
  
  private static boolean isDone0(Object result) {
    return (result != null && result != UNCANCELLABLE);
  }
  
  private static final class CauseHolder {
    final Throwable cause;
    
    CauseHolder(Throwable cause) {
      this.cause = cause;
    }
  }
  
  private static void safeExecute(EventExecutor executor, Runnable task) {
    try {
      executor.execute(task);
    } catch (Throwable t) {
      rejectedExecutionLogger.error("Failed to submit a listener notification task. Event loop shut down?", t);
    } 
  }
}

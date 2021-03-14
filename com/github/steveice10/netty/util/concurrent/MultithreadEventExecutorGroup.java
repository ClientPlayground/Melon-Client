package com.github.steveice10.netty.util.concurrent;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MultithreadEventExecutorGroup extends AbstractEventExecutorGroup {
  private final EventExecutor[] children;
  
  private final Set<EventExecutor> readonlyChildren;
  
  private final AtomicInteger terminatedChildren = new AtomicInteger();
  
  private final Promise<?> terminationFuture = new DefaultPromise(GlobalEventExecutor.INSTANCE);
  
  private final EventExecutorChooserFactory.EventExecutorChooser chooser;
  
  protected MultithreadEventExecutorGroup(int nThreads, ThreadFactory threadFactory, Object... args) {
    this(nThreads, (threadFactory == null) ? null : new ThreadPerTaskExecutor(threadFactory), args);
  }
  
  protected MultithreadEventExecutorGroup(int nThreads, Executor executor, Object... args) {
    this(nThreads, executor, DefaultEventExecutorChooserFactory.INSTANCE, args);
  }
  
  protected MultithreadEventExecutorGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, Object... args) {
    if (nThreads <= 0)
      throw new IllegalArgumentException(String.format("nThreads: %d (expected: > 0)", new Object[] { Integer.valueOf(nThreads) })); 
    if (executor == null)
      executor = new ThreadPerTaskExecutor(newDefaultThreadFactory()); 
    this.children = new EventExecutor[nThreads];
    for (int i = 0; i < nThreads; i++)
      boolean success = false; 
    this.chooser = chooserFactory.newChooser(this.children);
    FutureListener<Object> terminationListener = new FutureListener() {
        public void operationComplete(Future<Object> future) throws Exception {
          if (MultithreadEventExecutorGroup.this.terminatedChildren.incrementAndGet() == MultithreadEventExecutorGroup.this.children.length)
            MultithreadEventExecutorGroup.this.terminationFuture.setSuccess(null); 
        }
      };
    for (EventExecutor e : this.children)
      e.terminationFuture().addListener(terminationListener); 
    Set<EventExecutor> childrenSet = new LinkedHashSet<EventExecutor>(this.children.length);
    Collections.addAll(childrenSet, this.children);
    this.readonlyChildren = Collections.unmodifiableSet(childrenSet);
  }
  
  protected ThreadFactory newDefaultThreadFactory() {
    return new DefaultThreadFactory(getClass());
  }
  
  public EventExecutor next() {
    return this.chooser.next();
  }
  
  public Iterator<EventExecutor> iterator() {
    return this.readonlyChildren.iterator();
  }
  
  public final int executorCount() {
    return this.children.length;
  }
  
  public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
    for (EventExecutor l : this.children)
      l.shutdownGracefully(quietPeriod, timeout, unit); 
    return terminationFuture();
  }
  
  public Future<?> terminationFuture() {
    return this.terminationFuture;
  }
  
  @Deprecated
  public void shutdown() {
    for (EventExecutor l : this.children)
      l.shutdown(); 
  }
  
  public boolean isShuttingDown() {
    for (EventExecutor l : this.children) {
      if (!l.isShuttingDown())
        return false; 
    } 
    return true;
  }
  
  public boolean isShutdown() {
    for (EventExecutor l : this.children) {
      if (!l.isShutdown())
        return false; 
    } 
    return true;
  }
  
  public boolean isTerminated() {
    for (EventExecutor l : this.children) {
      if (!l.isTerminated())
        return false; 
    } 
    return true;
  }
  
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    long deadline = System.nanoTime() + unit.toNanos(timeout);
    EventExecutor[] arrayOfEventExecutor;
    int i;
    byte b;
    for (arrayOfEventExecutor = this.children, i = arrayOfEventExecutor.length, b = 0; b < i; ) {
      EventExecutor l = arrayOfEventExecutor[b];
      while (true) {
        long timeLeft = deadline - System.nanoTime();
        if (timeLeft <= 0L)
          break; 
        if (l.awaitTermination(timeLeft, TimeUnit.NANOSECONDS))
          b++; 
      } 
    } 
    return isTerminated();
  }
  
  protected abstract EventExecutor newChild(Executor paramExecutor, Object... paramVarArgs) throws Exception;
}

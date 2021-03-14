package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class UnorderedThreadPoolEventExecutor extends ScheduledThreadPoolExecutor implements EventExecutor {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(UnorderedThreadPoolEventExecutor.class);
  
  private final Promise<?> terminationFuture = GlobalEventExecutor.INSTANCE.newPromise();
  
  private final Set<EventExecutor> executorSet = Collections.singleton(this);
  
  public UnorderedThreadPoolEventExecutor(int corePoolSize) {
    this(corePoolSize, new DefaultThreadFactory(UnorderedThreadPoolEventExecutor.class));
  }
  
  public UnorderedThreadPoolEventExecutor(int corePoolSize, ThreadFactory threadFactory) {
    super(corePoolSize, threadFactory);
  }
  
  public UnorderedThreadPoolEventExecutor(int corePoolSize, RejectedExecutionHandler handler) {
    this(corePoolSize, new DefaultThreadFactory(UnorderedThreadPoolEventExecutor.class), handler);
  }
  
  public UnorderedThreadPoolEventExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    super(corePoolSize, threadFactory, handler);
  }
  
  public EventExecutor next() {
    return this;
  }
  
  public EventExecutorGroup parent() {
    return this;
  }
  
  public boolean inEventLoop() {
    return false;
  }
  
  public boolean inEventLoop(Thread thread) {
    return false;
  }
  
  public <V> Promise<V> newPromise() {
    return new DefaultPromise<V>(this);
  }
  
  public <V> ProgressivePromise<V> newProgressivePromise() {
    return new DefaultProgressivePromise<V>(this);
  }
  
  public <V> Future<V> newSucceededFuture(V result) {
    return new SucceededFuture<V>(this, result);
  }
  
  public <V> Future<V> newFailedFuture(Throwable cause) {
    return new FailedFuture<V>(this, cause);
  }
  
  public boolean isShuttingDown() {
    return isShutdown();
  }
  
  public List<Runnable> shutdownNow() {
    List<Runnable> tasks = super.shutdownNow();
    this.terminationFuture.trySuccess(null);
    return tasks;
  }
  
  public void shutdown() {
    super.shutdown();
    this.terminationFuture.trySuccess(null);
  }
  
  public Future<?> shutdownGracefully() {
    return shutdownGracefully(2L, 15L, TimeUnit.SECONDS);
  }
  
  public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
    shutdown();
    return terminationFuture();
  }
  
  public Future<?> terminationFuture() {
    return this.terminationFuture;
  }
  
  public Iterator<EventExecutor> iterator() {
    return this.executorSet.iterator();
  }
  
  protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
    return (runnable instanceof NonNotifyRunnable) ? task : new RunnableScheduledFutureTask<V>(this, runnable, task);
  }
  
  protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
    return new RunnableScheduledFutureTask<V>(this, callable, task);
  }
  
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    return (ScheduledFuture)super.schedule(command, delay, unit);
  }
  
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    return (ScheduledFuture<V>)super.<V>schedule(callable, delay, unit);
  }
  
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    return (ScheduledFuture)super.scheduleAtFixedRate(command, initialDelay, period, unit);
  }
  
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
    return (ScheduledFuture)super.scheduleWithFixedDelay(command, initialDelay, delay, unit);
  }
  
  public Future<?> submit(Runnable task) {
    return (Future)super.submit(task);
  }
  
  public <T> Future<T> submit(Runnable task, T result) {
    return (Future<T>)super.<T>submit(task, result);
  }
  
  public <T> Future<T> submit(Callable<T> task) {
    return (Future<T>)super.<T>submit(task);
  }
  
  public void execute(Runnable command) {
    super.schedule(new NonNotifyRunnable(command), 0L, TimeUnit.NANOSECONDS);
  }
  
  private static final class RunnableScheduledFutureTask<V> extends PromiseTask<V> implements RunnableScheduledFuture<V>, ScheduledFuture<V> {
    private final RunnableScheduledFuture<V> future;
    
    RunnableScheduledFutureTask(EventExecutor executor, Runnable runnable, RunnableScheduledFuture<V> future) {
      super(executor, runnable, (V)null);
      this.future = future;
    }
    
    RunnableScheduledFutureTask(EventExecutor executor, Callable<V> callable, RunnableScheduledFuture<V> future) {
      super(executor, callable);
      this.future = future;
    }
    
    public void run() {
      if (!isPeriodic()) {
        super.run();
      } else if (!isDone()) {
        try {
          this.task.call();
        } catch (Throwable cause) {
          if (!tryFailureInternal(cause))
            UnorderedThreadPoolEventExecutor.logger.warn("Failure during execution of task", cause); 
        } 
      } 
    }
    
    public boolean isPeriodic() {
      return this.future.isPeriodic();
    }
    
    public long getDelay(TimeUnit unit) {
      return this.future.getDelay(unit);
    }
    
    public int compareTo(Delayed o) {
      return this.future.compareTo(o);
    }
  }
  
  private static final class NonNotifyRunnable implements Runnable {
    private final Runnable task;
    
    NonNotifyRunnable(Runnable task) {
      this.task = task;
    }
    
    public void run() {
      this.task.run();
    }
  }
}

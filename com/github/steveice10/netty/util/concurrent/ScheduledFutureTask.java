package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.DefaultPriorityQueue;
import com.github.steveice10.netty.util.internal.PriorityQueue;
import com.github.steveice10.netty.util.internal.PriorityQueueNode;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

final class ScheduledFutureTask<V> extends PromiseTask<V> implements ScheduledFuture<V>, PriorityQueueNode {
  private static final AtomicLong nextTaskId = new AtomicLong();
  
  private static final long START_TIME = System.nanoTime();
  
  static long nanoTime() {
    return System.nanoTime() - START_TIME;
  }
  
  static long deadlineNanos(long delay) {
    return nanoTime() + delay;
  }
  
  private final long id = nextTaskId.getAndIncrement();
  
  private long deadlineNanos;
  
  private final long periodNanos;
  
  private int queueIndex = -1;
  
  ScheduledFutureTask(AbstractScheduledEventExecutor executor, Runnable runnable, V result, long nanoTime) {
    this(executor, toCallable(runnable, result), nanoTime);
  }
  
  ScheduledFutureTask(AbstractScheduledEventExecutor executor, Callable<V> callable, long nanoTime, long period) {
    super(executor, callable);
    if (period == 0L)
      throw new IllegalArgumentException("period: 0 (expected: != 0)"); 
    this.deadlineNanos = nanoTime;
    this.periodNanos = period;
  }
  
  ScheduledFutureTask(AbstractScheduledEventExecutor executor, Callable<V> callable, long nanoTime) {
    super(executor, callable);
    this.deadlineNanos = nanoTime;
    this.periodNanos = 0L;
  }
  
  protected EventExecutor executor() {
    return super.executor();
  }
  
  public long deadlineNanos() {
    return this.deadlineNanos;
  }
  
  public long delayNanos() {
    return Math.max(0L, deadlineNanos() - nanoTime());
  }
  
  public long delayNanos(long currentTimeNanos) {
    return Math.max(0L, deadlineNanos() - currentTimeNanos - START_TIME);
  }
  
  public long getDelay(TimeUnit unit) {
    return unit.convert(delayNanos(), TimeUnit.NANOSECONDS);
  }
  
  public int compareTo(Delayed o) {
    if (this == o)
      return 0; 
    ScheduledFutureTask<?> that = (ScheduledFutureTask)o;
    long d = deadlineNanos() - that.deadlineNanos();
    if (d < 0L)
      return -1; 
    if (d > 0L)
      return 1; 
    if (this.id < that.id)
      return -1; 
    if (this.id == that.id)
      throw new Error(); 
    return 1;
  }
  
  public void run() {
    assert executor().inEventLoop();
    try {
      if (this.periodNanos == 0L) {
        if (setUncancellableInternal()) {
          V result = this.task.call();
          setSuccessInternal(result);
        } 
      } else if (!isCancelled()) {
        this.task.call();
        if (!executor().isShutdown()) {
          long p = this.periodNanos;
          if (p > 0L) {
            this.deadlineNanos += p;
          } else {
            this.deadlineNanos = nanoTime() - p;
          } 
          if (!isCancelled()) {
            PriorityQueue<ScheduledFutureTask<?>> priorityQueue = ((AbstractScheduledEventExecutor)executor()).scheduledTaskQueue;
            assert priorityQueue != null;
            priorityQueue.add(this);
          } 
        } 
      } 
    } catch (Throwable cause) {
      setFailureInternal(cause);
    } 
  }
  
  public boolean cancel(boolean mayInterruptIfRunning) {
    boolean canceled = super.cancel(mayInterruptIfRunning);
    if (canceled)
      ((AbstractScheduledEventExecutor)executor()).removeScheduled(this); 
    return canceled;
  }
  
  boolean cancelWithoutRemove(boolean mayInterruptIfRunning) {
    return super.cancel(mayInterruptIfRunning);
  }
  
  protected StringBuilder toStringBuilder() {
    StringBuilder buf = super.toStringBuilder();
    buf.setCharAt(buf.length() - 1, ',');
    return buf.append(" id: ")
      .append(this.id)
      .append(", deadline: ")
      .append(this.deadlineNanos)
      .append(", period: ")
      .append(this.periodNanos)
      .append(')');
  }
  
  public int priorityQueueIndex(DefaultPriorityQueue<?> queue) {
    return this.queueIndex;
  }
  
  public void priorityQueueIndex(DefaultPriorityQueue<?> queue, int i) {
    this.queueIndex = i;
  }
}

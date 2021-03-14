package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.DefaultPriorityQueue;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PriorityQueue;
import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractScheduledEventExecutor extends AbstractEventExecutor {
  private static final Comparator<ScheduledFutureTask<?>> SCHEDULED_FUTURE_TASK_COMPARATOR = new Comparator<ScheduledFutureTask<?>>() {
      public int compare(ScheduledFutureTask<?> o1, ScheduledFutureTask<?> o2) {
        return o1.compareTo(o2);
      }
    };
  
  PriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue;
  
  protected AbstractScheduledEventExecutor() {}
  
  protected AbstractScheduledEventExecutor(EventExecutorGroup parent) {
    super(parent);
  }
  
  protected static long nanoTime() {
    return ScheduledFutureTask.nanoTime();
  }
  
  PriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue() {
    if (this.scheduledTaskQueue == null)
      this.scheduledTaskQueue = (PriorityQueue<ScheduledFutureTask<?>>)new DefaultPriorityQueue(SCHEDULED_FUTURE_TASK_COMPARATOR, 11); 
    return this.scheduledTaskQueue;
  }
  
  private static boolean isNullOrEmpty(Queue<ScheduledFutureTask<?>> queue) {
    return (queue == null || queue.isEmpty());
  }
  
  protected void cancelScheduledTasks() {
    assert inEventLoop();
    PriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
    if (isNullOrEmpty((Queue<ScheduledFutureTask<?>>)scheduledTaskQueue))
      return; 
    ScheduledFutureTask[] arrayOfScheduledFutureTask = (ScheduledFutureTask[])scheduledTaskQueue.toArray((Object[])new ScheduledFutureTask[scheduledTaskQueue.size()]);
    for (ScheduledFutureTask<?> task : arrayOfScheduledFutureTask)
      task.cancelWithoutRemove(false); 
    scheduledTaskQueue.clearIgnoringIndexes();
  }
  
  protected final Runnable pollScheduledTask() {
    return pollScheduledTask(nanoTime());
  }
  
  protected final Runnable pollScheduledTask(long nanoTime) {
    assert inEventLoop();
    PriorityQueue<ScheduledFutureTask<?>> priorityQueue = this.scheduledTaskQueue;
    ScheduledFutureTask<?> scheduledTask = (priorityQueue == null) ? null : priorityQueue.peek();
    if (scheduledTask == null)
      return null; 
    if (scheduledTask.deadlineNanos() <= nanoTime) {
      priorityQueue.remove();
      return scheduledTask;
    } 
    return null;
  }
  
  protected final long nextScheduledTaskNano() {
    PriorityQueue<ScheduledFutureTask<?>> priorityQueue = this.scheduledTaskQueue;
    ScheduledFutureTask<?> scheduledTask = (priorityQueue == null) ? null : priorityQueue.peek();
    if (scheduledTask == null)
      return -1L; 
    return Math.max(0L, scheduledTask.deadlineNanos() - nanoTime());
  }
  
  final ScheduledFutureTask<?> peekScheduledTask() {
    PriorityQueue<ScheduledFutureTask<?>> priorityQueue = this.scheduledTaskQueue;
    if (priorityQueue == null)
      return null; 
    return priorityQueue.peek();
  }
  
  protected final boolean hasScheduledTasks() {
    PriorityQueue<ScheduledFutureTask<?>> priorityQueue = this.scheduledTaskQueue;
    ScheduledFutureTask<?> scheduledTask = (priorityQueue == null) ? null : priorityQueue.peek();
    return (scheduledTask != null && scheduledTask.deadlineNanos() <= nanoTime());
  }
  
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    ObjectUtil.checkNotNull(command, "command");
    ObjectUtil.checkNotNull(unit, "unit");
    if (delay < 0L)
      delay = 0L; 
    validateScheduled(delay, unit);
    return schedule(new ScheduledFutureTask(this, command, null, 
          ScheduledFutureTask.deadlineNanos(unit.toNanos(delay))));
  }
  
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    ObjectUtil.checkNotNull(callable, "callable");
    ObjectUtil.checkNotNull(unit, "unit");
    if (delay < 0L)
      delay = 0L; 
    validateScheduled(delay, unit);
    return schedule(new ScheduledFutureTask<V>(this, callable, 
          ScheduledFutureTask.deadlineNanos(unit.toNanos(delay))));
  }
  
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    ObjectUtil.checkNotNull(command, "command");
    ObjectUtil.checkNotNull(unit, "unit");
    if (initialDelay < 0L)
      throw new IllegalArgumentException(
          String.format("initialDelay: %d (expected: >= 0)", new Object[] { Long.valueOf(initialDelay) })); 
    if (period <= 0L)
      throw new IllegalArgumentException(
          String.format("period: %d (expected: > 0)", new Object[] { Long.valueOf(period) })); 
    validateScheduled(initialDelay, unit);
    validateScheduled(period, unit);
    return schedule(new ScheduledFutureTask(this, 
          Executors.callable(command, null), 
          ScheduledFutureTask.deadlineNanos(unit.toNanos(initialDelay)), unit.toNanos(period)));
  }
  
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
    ObjectUtil.checkNotNull(command, "command");
    ObjectUtil.checkNotNull(unit, "unit");
    if (initialDelay < 0L)
      throw new IllegalArgumentException(
          String.format("initialDelay: %d (expected: >= 0)", new Object[] { Long.valueOf(initialDelay) })); 
    if (delay <= 0L)
      throw new IllegalArgumentException(
          String.format("delay: %d (expected: > 0)", new Object[] { Long.valueOf(delay) })); 
    validateScheduled(initialDelay, unit);
    validateScheduled(delay, unit);
    return schedule(new ScheduledFutureTask(this, 
          Executors.callable(command, null), 
          ScheduledFutureTask.deadlineNanos(unit.toNanos(initialDelay)), -unit.toNanos(delay)));
  }
  
  protected void validateScheduled(long amount, TimeUnit unit) {}
  
  <V> ScheduledFuture<V> schedule(final ScheduledFutureTask<V> task) {
    if (inEventLoop()) {
      scheduledTaskQueue().add(task);
    } else {
      execute(new Runnable() {
            public void run() {
              AbstractScheduledEventExecutor.this.scheduledTaskQueue().add(task);
            }
          });
    } 
    return task;
  }
  
  final void removeScheduled(final ScheduledFutureTask<?> task) {
    if (inEventLoop()) {
      scheduledTaskQueue().removeTyped(task);
    } else {
      execute(new Runnable() {
            public void run() {
              AbstractScheduledEventExecutor.this.removeScheduled(task);
            }
          });
    } 
  }
}

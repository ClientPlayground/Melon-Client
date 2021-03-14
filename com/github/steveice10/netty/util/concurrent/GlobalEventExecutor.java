package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.PriorityQueue;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GlobalEventExecutor extends AbstractScheduledEventExecutor {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(GlobalEventExecutor.class);
  
  private static final long SCHEDULE_QUIET_PERIOD_INTERVAL = TimeUnit.SECONDS.toNanos(1L);
  
  public static final GlobalEventExecutor INSTANCE = new GlobalEventExecutor();
  
  final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
  
  final ScheduledFutureTask<Void> quietPeriodTask = new ScheduledFutureTask<Void>(this, 
      Executors.callable(new Runnable() {
          public void run() {}
        },  null), ScheduledFutureTask.deadlineNanos(SCHEDULE_QUIET_PERIOD_INTERVAL), -SCHEDULE_QUIET_PERIOD_INTERVAL);
  
  final ThreadFactory threadFactory = new DefaultThreadFactory(
      DefaultThreadFactory.toPoolName(getClass()), false, 5, null);
  
  private final TaskRunner taskRunner = new TaskRunner();
  
  private final AtomicBoolean started = new AtomicBoolean();
  
  volatile Thread thread;
  
  private final Future<?> terminationFuture = new FailedFuture(this, new UnsupportedOperationException());
  
  private GlobalEventExecutor() {
    scheduledTaskQueue().add(this.quietPeriodTask);
  }
  
  Runnable takeTask() {
    BlockingQueue<Runnable> taskQueue = this.taskQueue;
    while (true) {
      Runnable task;
      ScheduledFutureTask<?> scheduledTask = peekScheduledTask();
      if (scheduledTask == null) {
        Runnable runnable = null;
        try {
          runnable = taskQueue.take();
        } catch (InterruptedException interruptedException) {}
        return runnable;
      } 
      long delayNanos = scheduledTask.delayNanos();
      if (delayNanos > 0L) {
        try {
          task = taskQueue.poll(delayNanos, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
          return null;
        } 
      } else {
        task = taskQueue.poll();
      } 
      if (task == null) {
        fetchFromScheduledTaskQueue();
        task = taskQueue.poll();
      } 
      if (task != null)
        return task; 
    } 
  }
  
  private void fetchFromScheduledTaskQueue() {
    long nanoTime = AbstractScheduledEventExecutor.nanoTime();
    Runnable scheduledTask = pollScheduledTask(nanoTime);
    while (scheduledTask != null) {
      this.taskQueue.add(scheduledTask);
      scheduledTask = pollScheduledTask(nanoTime);
    } 
  }
  
  public int pendingTasks() {
    return this.taskQueue.size();
  }
  
  private void addTask(Runnable task) {
    if (task == null)
      throw new NullPointerException("task"); 
    this.taskQueue.add(task);
  }
  
  public boolean inEventLoop(Thread thread) {
    return (thread == this.thread);
  }
  
  public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
    return terminationFuture();
  }
  
  public Future<?> terminationFuture() {
    return this.terminationFuture;
  }
  
  @Deprecated
  public void shutdown() {
    throw new UnsupportedOperationException();
  }
  
  public boolean isShuttingDown() {
    return false;
  }
  
  public boolean isShutdown() {
    return false;
  }
  
  public boolean isTerminated() {
    return false;
  }
  
  public boolean awaitTermination(long timeout, TimeUnit unit) {
    return false;
  }
  
  public boolean awaitInactivity(long timeout, TimeUnit unit) throws InterruptedException {
    if (unit == null)
      throw new NullPointerException("unit"); 
    Thread thread = this.thread;
    if (thread == null)
      throw new IllegalStateException("thread was not started"); 
    thread.join(unit.toMillis(timeout));
    return !thread.isAlive();
  }
  
  public void execute(Runnable task) {
    if (task == null)
      throw new NullPointerException("task"); 
    addTask(task);
    if (!inEventLoop())
      startThread(); 
  }
  
  private void startThread() {
    if (this.started.compareAndSet(false, true)) {
      final Thread t = this.threadFactory.newThread(this.taskRunner);
      AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
              t.setContextClassLoader(null);
              return null;
            }
          });
      this.thread = t;
      t.start();
    } 
  }
  
  final class TaskRunner implements Runnable {
    public void run() {
      while (true) {
        Runnable task = GlobalEventExecutor.this.takeTask();
        if (task != null) {
          try {
            task.run();
          } catch (Throwable t) {
            GlobalEventExecutor.logger.warn("Unexpected exception from the global event executor: ", t);
          } 
          if (task != GlobalEventExecutor.this.quietPeriodTask)
            continue; 
        } 
        PriorityQueue<ScheduledFutureTask<?>> priorityQueue = GlobalEventExecutor.this.scheduledTaskQueue;
        if (GlobalEventExecutor.this.taskQueue.isEmpty() && (priorityQueue == null || priorityQueue.size() == 1)) {
          boolean stopped = GlobalEventExecutor.this.started.compareAndSet(true, false);
          assert stopped;
          if (GlobalEventExecutor.this.taskQueue.isEmpty() && (priorityQueue == null || priorityQueue.size() == 1))
            break; 
          if (!GlobalEventExecutor.this.started.compareAndSet(false, true))
            break; 
        } 
      } 
    }
  }
}

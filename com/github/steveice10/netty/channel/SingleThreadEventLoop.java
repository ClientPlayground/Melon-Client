package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.EventExecutorGroup;
import com.github.steveice10.netty.util.concurrent.RejectedExecutionHandler;
import com.github.steveice10.netty.util.concurrent.RejectedExecutionHandlers;
import com.github.steveice10.netty.util.concurrent.SingleThreadEventExecutor;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public abstract class SingleThreadEventLoop extends SingleThreadEventExecutor implements EventLoop {
  protected static final int DEFAULT_MAX_PENDING_TASKS = Math.max(16, 
      SystemPropertyUtil.getInt("com.github.steveice10.netty.eventLoop.maxPendingTasks", 2147483647));
  
  private final Queue<Runnable> tailTasks;
  
  protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp) {
    this(parent, threadFactory, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS, RejectedExecutionHandlers.reject());
  }
  
  protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor, boolean addTaskWakesUp) {
    this(parent, executor, addTaskWakesUp, DEFAULT_MAX_PENDING_TASKS, RejectedExecutionHandlers.reject());
  }
  
  protected SingleThreadEventLoop(EventLoopGroup parent, ThreadFactory threadFactory, boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler) {
    super(parent, threadFactory, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
    this.tailTasks = newTaskQueue(maxPendingTasks);
  }
  
  protected SingleThreadEventLoop(EventLoopGroup parent, Executor executor, boolean addTaskWakesUp, int maxPendingTasks, RejectedExecutionHandler rejectedExecutionHandler) {
    super(parent, executor, addTaskWakesUp, maxPendingTasks, rejectedExecutionHandler);
    this.tailTasks = newTaskQueue(maxPendingTasks);
  }
  
  public EventLoopGroup parent() {
    return (EventLoopGroup)super.parent();
  }
  
  public EventLoop next() {
    return (EventLoop)super.next();
  }
  
  public ChannelFuture register(Channel channel) {
    return register(new DefaultChannelPromise(channel, (EventExecutor)this));
  }
  
  public ChannelFuture register(ChannelPromise promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    promise.channel().unsafe().register(this, promise);
    return promise;
  }
  
  @Deprecated
  public ChannelFuture register(Channel channel, ChannelPromise promise) {
    if (channel == null)
      throw new NullPointerException("channel"); 
    if (promise == null)
      throw new NullPointerException("promise"); 
    channel.unsafe().register(this, promise);
    return promise;
  }
  
  public final void executeAfterEventLoopIteration(Runnable task) {
    ObjectUtil.checkNotNull(task, "task");
    if (isShutdown())
      reject(); 
    if (!this.tailTasks.offer(task))
      reject(task); 
    if (wakesUpForTask(task))
      wakeup(inEventLoop()); 
  }
  
  final boolean removeAfterEventLoopIterationTask(Runnable task) {
    return this.tailTasks.remove(ObjectUtil.checkNotNull(task, "task"));
  }
  
  protected boolean wakesUpForTask(Runnable task) {
    return !(task instanceof NonWakeupRunnable);
  }
  
  protected void afterRunningAllTasks() {
    runAllTasksFrom(this.tailTasks);
  }
  
  protected boolean hasTasks() {
    return (super.hasTasks() || !this.tailTasks.isEmpty());
  }
  
  public int pendingTasks() {
    return super.pendingTasks() + this.tailTasks.size();
  }
  
  static interface NonWakeupRunnable extends Runnable {}
}

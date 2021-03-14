package com.github.steveice10.netty.util.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public class DefaultEventExecutorGroup extends MultithreadEventExecutorGroup {
  public DefaultEventExecutorGroup(int nThreads) {
    this(nThreads, null);
  }
  
  public DefaultEventExecutorGroup(int nThreads, ThreadFactory threadFactory) {
    this(nThreads, threadFactory, SingleThreadEventExecutor.DEFAULT_MAX_PENDING_EXECUTOR_TASKS, 
        RejectedExecutionHandlers.reject());
  }
  
  public DefaultEventExecutorGroup(int nThreads, ThreadFactory threadFactory, int maxPendingTasks, RejectedExecutionHandler rejectedHandler) {
    super(nThreads, threadFactory, new Object[] { Integer.valueOf(maxPendingTasks), rejectedHandler });
  }
  
  protected EventExecutor newChild(Executor executor, Object... args) throws Exception {
    return new DefaultEventExecutor(this, executor, ((Integer)args[0]).intValue(), (RejectedExecutionHandler)args[1]);
  }
}

package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public final class RejectedExecutionHandlers {
  private static final RejectedExecutionHandler REJECT = new RejectedExecutionHandler() {
      public void rejected(Runnable task, SingleThreadEventExecutor executor) {
        throw new RejectedExecutionException();
      }
    };
  
  public static RejectedExecutionHandler reject() {
    return REJECT;
  }
  
  public static RejectedExecutionHandler backoff(final int retries, long backoffAmount, TimeUnit unit) {
    ObjectUtil.checkPositive(retries, "retries");
    final long backOffNanos = unit.toNanos(backoffAmount);
    return new RejectedExecutionHandler() {
        public void rejected(Runnable task, SingleThreadEventExecutor executor) {
          if (!executor.inEventLoop())
            for (int i = 0; i < retries; i++) {
              executor.wakeup(false);
              LockSupport.parkNanos(backOffNanos);
              if (executor.offerTask(task))
                return; 
            }  
          throw new RejectedExecutionException();
        }
      };
  }
}

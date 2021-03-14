package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.ObjectUtil;

final class FastThreadLocalRunnable implements Runnable {
  private final Runnable runnable;
  
  private FastThreadLocalRunnable(Runnable runnable) {
    this.runnable = (Runnable)ObjectUtil.checkNotNull(runnable, "runnable");
  }
  
  public void run() {
    try {
      this.runnable.run();
    } finally {
      FastThreadLocal.removeAll();
    } 
  }
  
  static Runnable wrap(Runnable runnable) {
    return (runnable instanceof FastThreadLocalRunnable) ? runnable : new FastThreadLocalRunnable(runnable);
  }
}

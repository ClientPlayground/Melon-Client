package com.github.steveice10.netty.util.concurrent;

public interface RejectedExecutionHandler {
  void rejected(Runnable paramRunnable, SingleThreadEventExecutor paramSingleThreadEventExecutor);
}

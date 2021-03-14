package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public final class ImmediateEventExecutor extends AbstractEventExecutor {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ImmediateEventExecutor.class);
  
  public static final ImmediateEventExecutor INSTANCE = new ImmediateEventExecutor();
  
  private static final FastThreadLocal<Queue<Runnable>> DELAYED_RUNNABLES = new FastThreadLocal<Queue<Runnable>>() {
      protected Queue<Runnable> initialValue() throws Exception {
        return new ArrayDeque<Runnable>();
      }
    };
  
  private static final FastThreadLocal<Boolean> RUNNING = new FastThreadLocal<Boolean>() {
      protected Boolean initialValue() throws Exception {
        return Boolean.valueOf(false);
      }
    };
  
  private final Future<?> terminationFuture = new FailedFuture(GlobalEventExecutor.INSTANCE, new UnsupportedOperationException());
  
  public boolean inEventLoop() {
    return true;
  }
  
  public boolean inEventLoop(Thread thread) {
    return true;
  }
  
  public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
    return terminationFuture();
  }
  
  public Future<?> terminationFuture() {
    return this.terminationFuture;
  }
  
  @Deprecated
  public void shutdown() {}
  
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
  
  public void execute(Runnable command) {
    if (command == null)
      throw new NullPointerException("command"); 
    if (!((Boolean)RUNNING.get()).booleanValue()) {
      RUNNING.set(Boolean.valueOf(true));
      try {
        command.run();
      } catch (Throwable cause) {
        logger.info("Throwable caught while executing Runnable {}", command, cause);
      } finally {
        Queue<Runnable> delayedRunnables = DELAYED_RUNNABLES.get();
        Runnable runnable;
        while ((runnable = delayedRunnables.poll()) != null) {
          try {
            runnable.run();
          } catch (Throwable cause) {
            logger.info("Throwable caught while executing Runnable {}", runnable, cause);
          } 
        } 
        RUNNING.set(Boolean.valueOf(false));
      } 
    } else {
      ((Queue<Runnable>)DELAYED_RUNNABLES.get()).add(command);
    } 
  }
  
  public <V> Promise<V> newPromise() {
    return new ImmediatePromise<V>(this);
  }
  
  public <V> ProgressivePromise<V> newProgressivePromise() {
    return new ImmediateProgressivePromise<V>(this);
  }
  
  static class ImmediatePromise<V> extends DefaultPromise<V> {
    ImmediatePromise(EventExecutor executor) {
      super(executor);
    }
    
    protected void checkDeadLock() {}
  }
  
  static class ImmediateProgressivePromise<V> extends DefaultProgressivePromise<V> {
    ImmediateProgressivePromise(EventExecutor executor) {
      super(executor);
    }
    
    protected void checkDeadLock() {}
  }
}

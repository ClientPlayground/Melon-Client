package com.github.steveice10.netty.util.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultEventExecutorChooserFactory implements EventExecutorChooserFactory {
  public static final DefaultEventExecutorChooserFactory INSTANCE = new DefaultEventExecutorChooserFactory();
  
  public EventExecutorChooserFactory.EventExecutorChooser newChooser(EventExecutor[] executors) {
    if (isPowerOfTwo(executors.length))
      return new PowerOfTwoEventExecutorChooser(executors); 
    return new GenericEventExecutorChooser(executors);
  }
  
  private static boolean isPowerOfTwo(int val) {
    return ((val & -val) == val);
  }
  
  private static final class PowerOfTwoEventExecutorChooser implements EventExecutorChooserFactory.EventExecutorChooser {
    private final AtomicInteger idx = new AtomicInteger();
    
    private final EventExecutor[] executors;
    
    PowerOfTwoEventExecutorChooser(EventExecutor[] executors) {
      this.executors = executors;
    }
    
    public EventExecutor next() {
      return this.executors[this.idx.getAndIncrement() & this.executors.length - 1];
    }
  }
  
  private static final class GenericEventExecutorChooser implements EventExecutorChooserFactory.EventExecutorChooser {
    private final AtomicInteger idx = new AtomicInteger();
    
    private final EventExecutor[] executors;
    
    GenericEventExecutorChooser(EventExecutor[] executors) {
      this.executors = executors;
    }
    
    public EventExecutor next() {
      return this.executors[Math.abs(this.idx.getAndIncrement() % this.executors.length)];
    }
  }
}

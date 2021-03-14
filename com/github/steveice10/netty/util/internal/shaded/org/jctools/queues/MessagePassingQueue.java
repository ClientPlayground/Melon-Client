package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

public interface MessagePassingQueue<T> {
  public static final int UNBOUNDED_CAPACITY = -1;
  
  boolean offer(T paramT);
  
  T poll();
  
  T peek();
  
  int size();
  
  void clear();
  
  boolean isEmpty();
  
  int capacity();
  
  boolean relaxedOffer(T paramT);
  
  T relaxedPoll();
  
  T relaxedPeek();
  
  int drain(Consumer<T> paramConsumer);
  
  int fill(Supplier<T> paramSupplier);
  
  int drain(Consumer<T> paramConsumer, int paramInt);
  
  int fill(Supplier<T> paramSupplier, int paramInt);
  
  void drain(Consumer<T> paramConsumer, WaitStrategy paramWaitStrategy, ExitCondition paramExitCondition);
  
  void fill(Supplier<T> paramSupplier, WaitStrategy paramWaitStrategy, ExitCondition paramExitCondition);
  
  public static interface ExitCondition {
    boolean keepRunning();
  }
  
  public static interface WaitStrategy {
    int idle(int param1Int);
  }
  
  public static interface Consumer<T> {
    void accept(T param1T);
  }
  
  public static interface Supplier<T> {
    T get();
  }
}

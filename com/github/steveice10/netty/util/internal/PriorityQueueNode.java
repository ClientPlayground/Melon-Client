package com.github.steveice10.netty.util.internal;

public interface PriorityQueueNode {
  public static final int INDEX_NOT_IN_QUEUE = -1;
  
  int priorityQueueIndex(DefaultPriorityQueue<?> paramDefaultPriorityQueue);
  
  void priorityQueueIndex(DefaultPriorityQueue<?> paramDefaultPriorityQueue, int paramInt);
}

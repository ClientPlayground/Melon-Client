package com.github.steveice10.netty.util.internal;

import java.util.Queue;

public interface PriorityQueue<T> extends Queue<T> {
  boolean removeTyped(T paramT);
  
  boolean containsTyped(T paramT);
  
  void priorityChanged(T paramT);
  
  void clearIgnoringIndexes();
}

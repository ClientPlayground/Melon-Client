package com.github.steveice10.netty.util;

public interface ResourceLeakTracker<T> {
  void record();
  
  void record(Object paramObject);
  
  boolean close(T paramT);
}

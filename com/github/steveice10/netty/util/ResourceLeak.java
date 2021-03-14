package com.github.steveice10.netty.util;

@Deprecated
public interface ResourceLeak {
  void record();
  
  void record(Object paramObject);
  
  boolean close();
}

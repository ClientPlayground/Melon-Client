package com.github.steveice10.netty.util.internal;

public interface LongCounter {
  void add(long paramLong);
  
  void increment();
  
  void decrement();
  
  long value();
}

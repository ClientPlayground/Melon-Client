package com.github.steveice10.netty.util;

public interface Timeout {
  Timer timer();
  
  TimerTask task();
  
  boolean isExpired();
  
  boolean isCancelled();
  
  boolean cancel();
}

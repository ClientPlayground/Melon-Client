package com.github.steveice10.netty.util;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface Timer {
  Timeout newTimeout(TimerTask paramTimerTask, long paramLong, TimeUnit paramTimeUnit);
  
  Set<Timeout> stop();
}

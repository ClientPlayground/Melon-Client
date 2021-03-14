package com.github.steveice10.netty.buffer;

public interface ByteBufAllocatorMetric {
  long usedHeapMemory();
  
  long usedDirectMemory();
}

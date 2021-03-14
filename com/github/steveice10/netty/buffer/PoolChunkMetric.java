package com.github.steveice10.netty.buffer;

public interface PoolChunkMetric {
  int usage();
  
  int chunkSize();
  
  int freeBytes();
}

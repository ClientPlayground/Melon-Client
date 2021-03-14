package com.github.steveice10.netty.buffer;

public interface PoolChunkListMetric extends Iterable<PoolChunkMetric> {
  int minUsage();
  
  int maxUsage();
}

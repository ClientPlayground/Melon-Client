package com.github.steveice10.netty.channel.pool;

public interface ChannelPoolMap<K, P extends ChannelPool> {
  P get(K paramK);
  
  boolean contains(K paramK);
}

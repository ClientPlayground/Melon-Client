package com.github.steveice10.netty.channel.pool;

import com.github.steveice10.netty.channel.Channel;

public interface ChannelPoolHandler {
  void channelReleased(Channel paramChannel) throws Exception;
  
  void channelAcquired(Channel paramChannel) throws Exception;
  
  void channelCreated(Channel paramChannel) throws Exception;
}

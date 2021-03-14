package com.github.steveice10.netty.channel.pool;

import com.github.steveice10.netty.channel.Channel;

public abstract class AbstractChannelPoolHandler implements ChannelPoolHandler {
  public void channelAcquired(Channel ch) throws Exception {}
  
  public void channelReleased(Channel ch) throws Exception {}
}

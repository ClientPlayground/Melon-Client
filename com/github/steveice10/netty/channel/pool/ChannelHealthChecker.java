package com.github.steveice10.netty.channel.pool;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.util.concurrent.Future;

public interface ChannelHealthChecker {
  public static final ChannelHealthChecker ACTIVE = new ChannelHealthChecker() {
      public Future<Boolean> isHealthy(Channel channel) {
        EventLoop loop = channel.eventLoop();
        return channel.isActive() ? loop.newSucceededFuture(Boolean.TRUE) : loop.newSucceededFuture(Boolean.FALSE);
      }
    };
  
  Future<Boolean> isHealthy(Channel paramChannel);
}

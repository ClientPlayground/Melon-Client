package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.OrderedEventExecutor;

public interface EventLoop extends OrderedEventExecutor, EventLoopGroup {
  EventLoopGroup parent();
}

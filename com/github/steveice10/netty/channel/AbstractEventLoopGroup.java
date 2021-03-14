package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.AbstractEventExecutorGroup;
import com.github.steveice10.netty.util.concurrent.EventExecutor;

public abstract class AbstractEventLoopGroup extends AbstractEventExecutorGroup implements EventLoopGroup {
  public abstract EventLoop next();
}

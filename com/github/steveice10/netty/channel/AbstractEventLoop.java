package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.AbstractEventExecutor;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.EventExecutorGroup;

public abstract class AbstractEventLoop extends AbstractEventExecutor implements EventLoop {
  protected AbstractEventLoop() {}
  
  protected AbstractEventLoop(EventLoopGroup parent) {
    super(parent);
  }
  
  public EventLoopGroup parent() {
    return (EventLoopGroup)super.parent();
  }
  
  public EventLoop next() {
    return (EventLoop)super.next();
  }
}

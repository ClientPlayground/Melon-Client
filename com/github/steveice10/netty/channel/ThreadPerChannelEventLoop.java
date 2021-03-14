package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.Future;

public class ThreadPerChannelEventLoop extends SingleThreadEventLoop {
  private final ThreadPerChannelEventLoopGroup parent;
  
  private Channel ch;
  
  public ThreadPerChannelEventLoop(ThreadPerChannelEventLoopGroup parent) {
    super(parent, parent.executor, true);
    this.parent = parent;
  }
  
  public ChannelFuture register(ChannelPromise promise) {
    return super.register(promise).addListener(new ChannelFutureListener() {
          public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
              ThreadPerChannelEventLoop.this.ch = future.channel();
            } else {
              ThreadPerChannelEventLoop.this.deregister();
            } 
          }
        });
  }
  
  @Deprecated
  public ChannelFuture register(Channel channel, ChannelPromise promise) {
    return super.register(channel, promise).addListener(new ChannelFutureListener() {
          public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
              ThreadPerChannelEventLoop.this.ch = future.channel();
            } else {
              ThreadPerChannelEventLoop.this.deregister();
            } 
          }
        });
  }
  
  protected void run() {
    while (true) {
      Runnable task = takeTask();
      if (task != null) {
        task.run();
        updateLastExecutionTime();
      } 
      Channel ch = this.ch;
      if (isShuttingDown()) {
        if (ch != null)
          ch.unsafe().close(ch.unsafe().voidPromise()); 
        if (confirmShutdown())
          break; 
        continue;
      } 
      if (ch != null)
        if (!ch.isRegistered()) {
          runAllTasks();
          deregister();
        }  
    } 
  }
  
  protected void deregister() {
    this.ch = null;
    this.parent.activeChildren.remove(this);
    this.parent.idleChildren.add(this);
  }
}

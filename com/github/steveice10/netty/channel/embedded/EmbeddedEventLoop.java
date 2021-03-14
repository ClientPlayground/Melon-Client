package com.github.steveice10.netty.channel.embedded;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.DefaultChannelPromise;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.EventLoopGroup;
import com.github.steveice10.netty.util.concurrent.AbstractScheduledEventExecutor;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.EventExecutorGroup;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

final class EmbeddedEventLoop extends AbstractScheduledEventExecutor implements EventLoop {
  private final Queue<Runnable> tasks = new ArrayDeque<Runnable>(2);
  
  public EventLoopGroup parent() {
    return (EventLoopGroup)super.parent();
  }
  
  public EventLoop next() {
    return (EventLoop)super.next();
  }
  
  public void execute(Runnable command) {
    if (command == null)
      throw new NullPointerException("command"); 
    this.tasks.add(command);
  }
  
  void runTasks() {
    while (true) {
      Runnable task = this.tasks.poll();
      if (task == null)
        break; 
      task.run();
    } 
  }
  
  long runScheduledTasks() {
    long time = AbstractScheduledEventExecutor.nanoTime();
    while (true) {
      Runnable task = pollScheduledTask(time);
      if (task == null)
        return nextScheduledTaskNano(); 
      task.run();
    } 
  }
  
  long nextScheduledTask() {
    return nextScheduledTaskNano();
  }
  
  protected void cancelScheduledTasks() {
    super.cancelScheduledTasks();
  }
  
  public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
    throw new UnsupportedOperationException();
  }
  
  public Future<?> terminationFuture() {
    throw new UnsupportedOperationException();
  }
  
  @Deprecated
  public void shutdown() {
    throw new UnsupportedOperationException();
  }
  
  public boolean isShuttingDown() {
    return false;
  }
  
  public boolean isShutdown() {
    return false;
  }
  
  public boolean isTerminated() {
    return false;
  }
  
  public boolean awaitTermination(long timeout, TimeUnit unit) {
    return false;
  }
  
  public ChannelFuture register(Channel channel) {
    return register((ChannelPromise)new DefaultChannelPromise(channel, (EventExecutor)this));
  }
  
  public ChannelFuture register(ChannelPromise promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    promise.channel().unsafe().register(this, promise);
    return (ChannelFuture)promise;
  }
  
  @Deprecated
  public ChannelFuture register(Channel channel, ChannelPromise promise) {
    channel.unsafe().register(this, promise);
    return (ChannelFuture)promise;
  }
  
  public boolean inEventLoop() {
    return true;
  }
  
  public boolean inEventLoop(Thread thread) {
    return true;
  }
}

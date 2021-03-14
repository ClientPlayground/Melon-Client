package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.channel.DefaultSelectStrategyFactory;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.EventLoopGroup;
import com.github.steveice10.netty.channel.MultithreadEventLoopGroup;
import com.github.steveice10.netty.channel.SelectStrategyFactory;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.EventExecutorChooserFactory;
import com.github.steveice10.netty.util.concurrent.RejectedExecutionHandler;
import com.github.steveice10.netty.util.concurrent.RejectedExecutionHandlers;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

public final class EpollEventLoopGroup extends MultithreadEventLoopGroup {
  public EpollEventLoopGroup() {
    this(0);
  }
  
  public EpollEventLoopGroup(int nThreads) {
    this(nThreads, (ThreadFactory)null);
  }
  
  public EpollEventLoopGroup(int nThreads, SelectStrategyFactory selectStrategyFactory) {
    this(nThreads, (ThreadFactory)null, selectStrategyFactory);
  }
  
  public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
    this(nThreads, threadFactory, 0);
  }
  
  public EpollEventLoopGroup(int nThreads, Executor executor) {
    this(nThreads, executor, DefaultSelectStrategyFactory.INSTANCE);
  }
  
  public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory, SelectStrategyFactory selectStrategyFactory) {
    this(nThreads, threadFactory, 0, selectStrategyFactory);
  }
  
  @Deprecated
  public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory, int maxEventsAtOnce) {
    this(nThreads, threadFactory, maxEventsAtOnce, DefaultSelectStrategyFactory.INSTANCE);
  }
  
  @Deprecated
  public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory, int maxEventsAtOnce, SelectStrategyFactory selectStrategyFactory) {
    super(nThreads, threadFactory, new Object[] { Integer.valueOf(maxEventsAtOnce), selectStrategyFactory, RejectedExecutionHandlers.reject() });
    Epoll.ensureAvailability();
  }
  
  public EpollEventLoopGroup(int nThreads, Executor executor, SelectStrategyFactory selectStrategyFactory) {
    super(nThreads, executor, new Object[] { Integer.valueOf(0), selectStrategyFactory, RejectedExecutionHandlers.reject() });
    Epoll.ensureAvailability();
  }
  
  public EpollEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectStrategyFactory selectStrategyFactory) {
    super(nThreads, executor, chooserFactory, new Object[] { Integer.valueOf(0), selectStrategyFactory, RejectedExecutionHandlers.reject() });
    Epoll.ensureAvailability();
  }
  
  public EpollEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory, SelectStrategyFactory selectStrategyFactory, RejectedExecutionHandler rejectedExecutionHandler) {
    super(nThreads, executor, chooserFactory, new Object[] { Integer.valueOf(0), selectStrategyFactory, rejectedExecutionHandler });
    Epoll.ensureAvailability();
  }
  
  public void setIoRatio(int ioRatio) {
    for (EventExecutor e : this)
      ((EpollEventLoop)e).setIoRatio(ioRatio); 
  }
  
  protected EventLoop newChild(Executor executor, Object... args) throws Exception {
    return (EventLoop)new EpollEventLoop((EventLoopGroup)this, executor, ((Integer)args[0]).intValue(), ((SelectStrategyFactory)args[1])
        .newSelectStrategy(), (RejectedExecutionHandler)args[2]);
  }
}
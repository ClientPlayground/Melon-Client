package com.github.steveice10.netty.channel.local;

import com.github.steveice10.netty.channel.DefaultEventLoopGroup;
import java.util.concurrent.ThreadFactory;

@Deprecated
public class LocalEventLoopGroup extends DefaultEventLoopGroup {
  public LocalEventLoopGroup() {}
  
  public LocalEventLoopGroup(int nThreads) {
    super(nThreads);
  }
  
  public LocalEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
    super(nThreads, threadFactory);
  }
}

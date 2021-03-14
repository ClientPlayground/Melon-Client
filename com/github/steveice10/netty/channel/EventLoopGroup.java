package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.EventExecutorGroup;

public interface EventLoopGroup extends EventExecutorGroup {
  EventLoop next();
  
  ChannelFuture register(Channel paramChannel);
  
  ChannelFuture register(ChannelPromise paramChannelPromise);
  
  @Deprecated
  ChannelFuture register(Channel paramChannel, ChannelPromise paramChannelPromise);
}

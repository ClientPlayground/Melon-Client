package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.internal.PlatformDependent;

final class FailedChannelFuture extends CompleteChannelFuture {
  private final Throwable cause;
  
  FailedChannelFuture(Channel channel, EventExecutor executor, Throwable cause) {
    super(channel, executor);
    if (cause == null)
      throw new NullPointerException("cause"); 
    this.cause = cause;
  }
  
  public Throwable cause() {
    return this.cause;
  }
  
  public boolean isSuccess() {
    return false;
  }
  
  public ChannelFuture sync() {
    PlatformDependent.throwException(this.cause);
    return this;
  }
  
  public ChannelFuture syncUninterruptibly() {
    PlatformDependent.throwException(this.cause);
    return this;
  }
}

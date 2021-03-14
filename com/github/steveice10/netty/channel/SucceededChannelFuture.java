package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.EventExecutor;

final class SucceededChannelFuture extends CompleteChannelFuture {
  SucceededChannelFuture(Channel channel, EventExecutor executor) {
    super(channel, executor);
  }
  
  public Throwable cause() {
    return null;
  }
  
  public boolean isSuccess() {
    return true;
  }
}

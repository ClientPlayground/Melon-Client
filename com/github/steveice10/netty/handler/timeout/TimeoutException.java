package com.github.steveice10.netty.handler.timeout;

import com.github.steveice10.netty.channel.ChannelException;

public class TimeoutException extends ChannelException {
  private static final long serialVersionUID = 4673641882869672533L;
  
  public Throwable fillInStackTrace() {
    return (Throwable)this;
  }
}

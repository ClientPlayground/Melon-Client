package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.util.internal.ObjectUtil;

public abstract class SslCompletionEvent {
  private final Throwable cause;
  
  SslCompletionEvent() {
    this.cause = null;
  }
  
  SslCompletionEvent(Throwable cause) {
    this.cause = (Throwable)ObjectUtil.checkNotNull(cause, "cause");
  }
  
  public final boolean isSuccess() {
    return (this.cause == null);
  }
  
  public final Throwable cause() {
    return this.cause;
  }
  
  public String toString() {
    Throwable cause = cause();
    return (cause == null) ? (getClass().getSimpleName() + "(SUCCESS)") : (
      getClass().getSimpleName() + '(' + cause + ')');
  }
}

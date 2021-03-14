package com.github.steveice10.netty.handler.ssl;

public final class SniCompletionEvent extends SslCompletionEvent {
  private final String hostname;
  
  SniCompletionEvent(String hostname) {
    this.hostname = hostname;
  }
  
  SniCompletionEvent(String hostname, Throwable cause) {
    super(cause);
    this.hostname = hostname;
  }
  
  SniCompletionEvent(Throwable cause) {
    this(null, cause);
  }
  
  public String hostname() {
    return this.hostname;
  }
  
  public String toString() {
    Throwable cause = cause();
    return (cause == null) ? (getClass().getSimpleName() + "(SUCCESS='" + this.hostname + "'\")") : (
      getClass().getSimpleName() + '(' + cause + ')');
  }
}

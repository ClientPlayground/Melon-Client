package com.github.steveice10.netty.handler.ssl;

public final class SslHandshakeCompletionEvent extends SslCompletionEvent {
  public static final SslHandshakeCompletionEvent SUCCESS = new SslHandshakeCompletionEvent();
  
  private SslHandshakeCompletionEvent() {}
  
  public SslHandshakeCompletionEvent(Throwable cause) {
    super(cause);
  }
}

package com.github.steveice10.netty.handler.codec.http.websocketx;

public class WebSocketHandshakeException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  public WebSocketHandshakeException(String s) {
    super(s);
  }
  
  public WebSocketHandshakeException(String s, Throwable throwable) {
    super(s, throwable);
  }
}

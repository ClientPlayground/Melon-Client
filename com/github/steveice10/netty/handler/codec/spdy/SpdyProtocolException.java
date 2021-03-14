package com.github.steveice10.netty.handler.codec.spdy;

public class SpdyProtocolException extends Exception {
  private static final long serialVersionUID = 7870000537743847264L;
  
  public SpdyProtocolException() {}
  
  public SpdyProtocolException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public SpdyProtocolException(String message) {
    super(message);
  }
  
  public SpdyProtocolException(Throwable cause) {
    super(cause);
  }
}

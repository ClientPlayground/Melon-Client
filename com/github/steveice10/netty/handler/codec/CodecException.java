package com.github.steveice10.netty.handler.codec;

public class CodecException extends RuntimeException {
  private static final long serialVersionUID = -1464830400709348473L;
  
  public CodecException() {}
  
  public CodecException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public CodecException(String message) {
    super(message);
  }
  
  public CodecException(Throwable cause) {
    super(cause);
  }
}

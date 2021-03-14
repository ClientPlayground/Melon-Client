package com.github.steveice10.netty.handler.codec;

public class EncoderException extends CodecException {
  private static final long serialVersionUID = -5086121160476476774L;
  
  public EncoderException() {}
  
  public EncoderException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public EncoderException(String message) {
    super(message);
  }
  
  public EncoderException(Throwable cause) {
    super(cause);
  }
}

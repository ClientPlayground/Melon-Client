package com.github.steveice10.netty.handler.codec;

public class CorruptedFrameException extends DecoderException {
  private static final long serialVersionUID = 3918052232492988408L;
  
  public CorruptedFrameException() {}
  
  public CorruptedFrameException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public CorruptedFrameException(String message) {
    super(message);
  }
  
  public CorruptedFrameException(Throwable cause) {
    super(cause);
  }
}

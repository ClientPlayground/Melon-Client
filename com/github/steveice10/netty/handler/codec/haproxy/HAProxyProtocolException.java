package com.github.steveice10.netty.handler.codec.haproxy;

import com.github.steveice10.netty.handler.codec.DecoderException;

public class HAProxyProtocolException extends DecoderException {
  private static final long serialVersionUID = 713710864325167351L;
  
  public HAProxyProtocolException() {}
  
  public HAProxyProtocolException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public HAProxyProtocolException(String message) {
    super(message);
  }
  
  public HAProxyProtocolException(Throwable cause) {
    super(cause);
  }
}

package com.github.steveice10.netty.handler.timeout;

public final class ReadTimeoutException extends TimeoutException {
  private static final long serialVersionUID = 169287984113283421L;
  
  public static final ReadTimeoutException INSTANCE = new ReadTimeoutException();
}

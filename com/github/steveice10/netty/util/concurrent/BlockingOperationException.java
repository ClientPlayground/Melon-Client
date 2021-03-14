package com.github.steveice10.netty.util.concurrent;

public class BlockingOperationException extends IllegalStateException {
  private static final long serialVersionUID = 2462223247762460301L;
  
  public BlockingOperationException() {}
  
  public BlockingOperationException(String s) {
    super(s);
  }
  
  public BlockingOperationException(Throwable cause) {
    super(cause);
  }
  
  public BlockingOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}

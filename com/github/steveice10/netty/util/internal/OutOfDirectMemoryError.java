package com.github.steveice10.netty.util.internal;

public final class OutOfDirectMemoryError extends OutOfMemoryError {
  private static final long serialVersionUID = 4228264016184011555L;
  
  OutOfDirectMemoryError(String s) {
    super(s);
  }
}

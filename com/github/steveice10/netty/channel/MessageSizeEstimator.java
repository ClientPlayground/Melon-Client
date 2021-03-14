package com.github.steveice10.netty.channel;

public interface MessageSizeEstimator {
  Handle newHandle();
  
  public static interface Handle {
    int size(Object param1Object);
  }
}

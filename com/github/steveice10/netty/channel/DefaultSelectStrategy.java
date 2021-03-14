package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.IntSupplier;

final class DefaultSelectStrategy implements SelectStrategy {
  static final SelectStrategy INSTANCE = new DefaultSelectStrategy();
  
  public int calculateStrategy(IntSupplier selectSupplier, boolean hasTasks) throws Exception {
    return hasTasks ? selectSupplier.get() : -1;
  }
}

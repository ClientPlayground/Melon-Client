package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.IntSupplier;

public interface SelectStrategy {
  public static final int SELECT = -1;
  
  public static final int CONTINUE = -2;
  
  int calculateStrategy(IntSupplier paramIntSupplier, boolean paramBoolean) throws Exception;
}

package com.github.steveice10.netty.util.concurrent;

import java.util.concurrent.Executor;

public final class ImmediateExecutor implements Executor {
  public static final ImmediateExecutor INSTANCE = new ImmediateExecutor();
  
  public void execute(Runnable command) {
    if (command == null)
      throw new NullPointerException("command"); 
    command.run();
  }
}

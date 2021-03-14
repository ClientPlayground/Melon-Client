package com.google.common.util.concurrent;

import com.google.common.annotations.VisibleForTesting;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UncaughtExceptionHandlers {
  public static Thread.UncaughtExceptionHandler systemExit() {
    return new Exiter(Runtime.getRuntime());
  }
  
  @VisibleForTesting
  static final class Exiter implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = Logger.getLogger(Exiter.class.getName());
    
    private final Runtime runtime;
    
    Exiter(Runtime runtime) {
      this.runtime = runtime;
    }
    
    public void uncaughtException(Thread t, Throwable e) {
      try {
        logger.log(Level.SEVERE, String.format("Caught an exception in %s.  Shutting down.", new Object[] { t }), e);
      } catch (Throwable errorInLogging) {
        System.err.println(e.getMessage());
        System.err.println(errorInLogging.getMessage());
      } finally {
        this.runtime.exit(1);
      } 
    }
  }
}

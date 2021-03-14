package com.sun.jna;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface Callback {
  public static final String METHOD_NAME = "callback";
  
  public static final List<String> FORBIDDEN_NAMES = Collections.unmodifiableList(
      Arrays.asList(new String[] { "hashCode", "equals", "toString" }));
  
  public static interface UncaughtExceptionHandler {
    void uncaughtException(Callback param1Callback, Throwable param1Throwable);
  }
}

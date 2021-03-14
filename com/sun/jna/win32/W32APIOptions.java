package com.sun.jna.win32;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface W32APIOptions extends StdCallLibrary {
  public static final Map<String, Object> UNICODE_OPTIONS = Collections.unmodifiableMap(new HashMap<String, Object>() {
        private static final long serialVersionUID = 1L;
      });
  
  public static final Map<String, Object> ASCII_OPTIONS = Collections.unmodifiableMap(new HashMap<String, Object>() {
        private static final long serialVersionUID = 1L;
      });
  
  public static final Map<String, Object> DEFAULT_OPTIONS = Boolean.getBoolean("w32.ascii") ? ASCII_OPTIONS : UNICODE_OPTIONS;
}

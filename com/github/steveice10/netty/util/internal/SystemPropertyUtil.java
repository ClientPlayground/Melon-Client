package com.github.steveice10.netty.util.internal;

import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;

public final class SystemPropertyUtil {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(SystemPropertyUtil.class);
  
  public static boolean contains(String key) {
    return (get(key) != null);
  }
  
  public static String get(String key) {
    return get(key, null);
  }
  
  public static String get(final String key, String def) {
    if (key == null)
      throw new NullPointerException("key"); 
    if (key.isEmpty())
      throw new IllegalArgumentException("key must not be empty."); 
    String value = null;
    try {
      if (System.getSecurityManager() == null) {
        value = System.getProperty(key);
      } else {
        value = AccessController.<String>doPrivileged(new PrivilegedAction<String>() {
              public String run() {
                return System.getProperty(key);
              }
            });
      } 
    } catch (SecurityException e) {
      logger.warn("Unable to retrieve a system property '{}'; default values will be used.", key, e);
    } 
    if (value == null)
      return def; 
    return value;
  }
  
  public static boolean getBoolean(String key, boolean def) {
    String value = get(key);
    if (value == null)
      return def; 
    value = value.trim().toLowerCase();
    if (value.isEmpty())
      return def; 
    if ("true".equals(value) || "yes".equals(value) || "1".equals(value))
      return true; 
    if ("false".equals(value) || "no".equals(value) || "0".equals(value))
      return false; 
    logger.warn("Unable to parse the boolean system property '{}':{} - using the default value: {}", new Object[] { key, value, 
          
          Boolean.valueOf(def) });
    return def;
  }
  
  public static int getInt(String key, int def) {
    String value = get(key);
    if (value == null)
      return def; 
    value = value.trim();
    try {
      return Integer.parseInt(value);
    } catch (Exception exception) {
      logger.warn("Unable to parse the integer system property '{}':{} - using the default value: {}", new Object[] { key, value, 
            
            Integer.valueOf(def) });
      return def;
    } 
  }
  
  public static long getLong(String key, long def) {
    String value = get(key);
    if (value == null)
      return def; 
    value = value.trim();
    try {
      return Long.parseLong(value);
    } catch (Exception exception) {
      logger.warn("Unable to parse the long integer system property '{}':{} - using the default value: {}", new Object[] { key, value, 
            
            Long.valueOf(def) });
      return def;
    } 
  }
}

package com.github.steveice10.netty.util.internal;

import java.lang.reflect.AccessibleObject;

public final class ReflectionUtil {
  public static Throwable trySetAccessible(AccessibleObject object, boolean checkAccessible) {
    if (checkAccessible && !PlatformDependent0.isExplicitTryReflectionSetAccessible())
      return new UnsupportedOperationException("Reflective setAccessible(true) disabled"); 
    try {
      object.setAccessible(true);
      return null;
    } catch (SecurityException e) {
      return e;
    } catch (RuntimeException e) {
      return handleInaccessibleObjectException(e);
    } 
  }
  
  private static RuntimeException handleInaccessibleObjectException(RuntimeException e) {
    if ("java.lang.reflect.InaccessibleObjectException".equals(e.getClass().getName()))
      return e; 
    throw e;
  }
}

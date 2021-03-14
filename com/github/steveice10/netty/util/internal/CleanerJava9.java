package com.github.steveice10.netty.util.internal;

import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

final class CleanerJava9 implements Cleaner {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(CleanerJava9.class);
  
  private static final Method INVOKE_CLEANER;
  
  static {
    Method method;
    Throwable error;
  }
  
  static {
    if (PlatformDependent0.hasUnsafe()) {
      Object maybeInvokeMethod;
      ByteBuffer buffer = ByteBuffer.allocateDirect(1);
      try {
        Method m = PlatformDependent0.UNSAFE.getClass().getDeclaredMethod("invokeCleaner", new Class[] { ByteBuffer.class });
        m.invoke(PlatformDependent0.UNSAFE, new Object[] { buffer });
        maybeInvokeMethod = m;
      } catch (NoSuchMethodException e) {
        maybeInvokeMethod = e;
      } catch (InvocationTargetException e) {
        maybeInvokeMethod = e;
      } catch (IllegalAccessException e) {
        maybeInvokeMethod = e;
      } 
      if (maybeInvokeMethod instanceof Throwable) {
        method = null;
        error = (Throwable)maybeInvokeMethod;
      } else {
        method = (Method)maybeInvokeMethod;
        error = null;
      } 
    } else {
      method = null;
      error = new UnsupportedOperationException("sun.misc.Unsafe unavailable");
    } 
    if (error == null) {
      logger.debug("java.nio.ByteBuffer.cleaner(): available");
    } else {
      logger.debug("java.nio.ByteBuffer.cleaner(): unavailable", error);
    } 
    INVOKE_CLEANER = method;
  }
  
  static boolean isSupported() {
    return (INVOKE_CLEANER != null);
  }
  
  public void freeDirectBuffer(ByteBuffer buffer) {
    try {
      INVOKE_CLEANER.invoke(PlatformDependent0.UNSAFE, new Object[] { buffer });
    } catch (Throwable cause) {
      PlatformDependent0.throwException(cause);
    } 
  }
}

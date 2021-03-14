package com.github.steveice10.netty.util.internal;

import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

final class CleanerJava6 implements Cleaner {
  private static final long CLEANER_FIELD_OFFSET;
  
  private static final Method CLEAN_METHOD;
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(CleanerJava6.class);
  
  static {
    long fieldOffset = -1L;
    Method clean = null;
    Throwable error = null;
    if (PlatformDependent0.hasUnsafe()) {
      ByteBuffer direct = ByteBuffer.allocateDirect(1);
      try {
        Field cleanerField = direct.getClass().getDeclaredField("cleaner");
        fieldOffset = PlatformDependent0.objectFieldOffset(cleanerField);
        Object cleaner = PlatformDependent0.getObject(direct, fieldOffset);
        clean = cleaner.getClass().getDeclaredMethod("clean", new Class[0]);
        clean.invoke(cleaner, new Object[0]);
      } catch (Throwable t) {
        fieldOffset = -1L;
        clean = null;
        error = t;
      } 
    } else {
      error = new UnsupportedOperationException("sun.misc.Unsafe unavailable");
    } 
    if (error == null) {
      logger.debug("java.nio.ByteBuffer.cleaner(): available");
    } else {
      logger.debug("java.nio.ByteBuffer.cleaner(): unavailable", error);
    } 
    CLEANER_FIELD_OFFSET = fieldOffset;
    CLEAN_METHOD = clean;
  }
  
  static boolean isSupported() {
    return (CLEANER_FIELD_OFFSET != -1L);
  }
  
  public void freeDirectBuffer(ByteBuffer buffer) {
    if (!buffer.isDirect())
      return; 
    try {
      Object cleaner = PlatformDependent0.getObject(buffer, CLEANER_FIELD_OFFSET);
      if (cleaner != null)
        CLEAN_METHOD.invoke(cleaner, new Object[0]); 
    } catch (Throwable cause) {
      PlatformDependent0.throwException(cause);
    } 
  }
}

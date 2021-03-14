package com.github.steveice10.netty.util.internal;

import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;

public final class PromiseNotificationUtil {
  public static void tryCancel(Promise<?> p, InternalLogger logger) {
    if (!p.cancel(false) && logger != null) {
      Throwable err = p.cause();
      if (err == null) {
        logger.warn("Failed to cancel promise because it has succeeded already: {}", p);
      } else {
        logger.warn("Failed to cancel promise because it has failed already: {}, unnotified cause:", p, err);
      } 
    } 
  }
  
  public static <V> void trySuccess(Promise<? super V> p, V result, InternalLogger logger) {
    if (!p.trySuccess(result) && logger != null) {
      Throwable err = p.cause();
      if (err == null) {
        logger.warn("Failed to mark a promise as success because it has succeeded already: {}", p);
      } else {
        logger.warn("Failed to mark a promise as success because it has failed already: {}, unnotified cause:", p, err);
      } 
    } 
  }
  
  public static void tryFailure(Promise<?> p, Throwable cause, InternalLogger logger) {
    if (!p.tryFailure(cause) && logger != null) {
      Throwable err = p.cause();
      if (err == null) {
        logger.warn("Failed to mark a promise as failure because it has succeeded already: {}", p, cause);
      } else {
        logger.warn("Failed to mark a promise as failure because it has failed already: {}, unnotified cause: {}", new Object[] { p, 
              
              ThrowableUtil.stackTraceToString(err), cause });
      } 
    } 
  }
}

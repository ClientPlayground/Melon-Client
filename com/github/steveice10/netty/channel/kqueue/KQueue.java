package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.channel.unix.FileDescriptor;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;

public final class KQueue {
  private static final Throwable UNAVAILABILITY_CAUSE;
  
  static {
    Throwable cause = null;
    if (SystemPropertyUtil.getBoolean("com.github.steveice10.netty.transport.noNative", false)) {
      cause = new UnsupportedOperationException("Native transport was explicit disabled with -Dio.netty.transport.noNative=true");
    } else {
      FileDescriptor kqueueFd = null;
      try {
        kqueueFd = Native.newKQueue();
      } catch (Throwable t) {
        cause = t;
      } finally {
        if (kqueueFd != null)
          try {
            kqueueFd.close();
          } catch (Exception exception) {} 
      } 
    } 
    if (cause != null) {
      UNAVAILABILITY_CAUSE = cause;
    } else {
      UNAVAILABILITY_CAUSE = PlatformDependent.hasUnsafe() ? null : new IllegalStateException("sun.misc.Unsafe not available", PlatformDependent.getUnsafeUnavailabilityCause());
    } 
  }
  
  public static boolean isAvailable() {
    return (UNAVAILABILITY_CAUSE == null);
  }
  
  public static void ensureAvailability() {
    if (UNAVAILABILITY_CAUSE != null)
      throw (Error)(new UnsatisfiedLinkError("failed to load the required native library"))
        .initCause(UNAVAILABILITY_CAUSE); 
  }
  
  public static Throwable unavailabilityCause() {
    return UNAVAILABILITY_CAUSE;
  }
}

package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess;

public final class CircularArrayOffsetCalculator {
  public static <E> E[] allocate(int capacity) {
    return (E[])new Object[capacity];
  }
  
  public static long calcElementOffset(long index, long mask) {
    return UnsafeRefArrayAccess.REF_ARRAY_BASE + ((index & mask) << UnsafeRefArrayAccess.REF_ELEMENT_SHIFT);
  }
}

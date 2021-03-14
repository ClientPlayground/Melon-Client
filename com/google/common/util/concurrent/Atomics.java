package com.google.common.util.concurrent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.annotation.Nullable;

public final class Atomics {
  public static <V> AtomicReference<V> newReference() {
    return new AtomicReference<V>();
  }
  
  public static <V> AtomicReference<V> newReference(@Nullable V initialValue) {
    return new AtomicReference<V>(initialValue);
  }
  
  public static <E> AtomicReferenceArray<E> newReferenceArray(int length) {
    return new AtomicReferenceArray<E>(length);
  }
  
  public static <E> AtomicReferenceArray<E> newReferenceArray(E[] array) {
    return new AtomicReferenceArray<E>(array);
  }
}

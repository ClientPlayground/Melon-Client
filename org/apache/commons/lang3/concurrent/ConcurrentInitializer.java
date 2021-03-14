package org.apache.commons.lang3.concurrent;

public interface ConcurrentInitializer<T> {
  T get() throws ConcurrentException;
}

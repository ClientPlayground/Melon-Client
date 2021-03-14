package org.apache.commons.lang3.builder;

public interface Diffable<T> {
  DiffResult diff(T paramT);
}

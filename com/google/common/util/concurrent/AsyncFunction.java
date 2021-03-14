package com.google.common.util.concurrent;

public interface AsyncFunction<I, O> {
  ListenableFuture<O> apply(I paramI) throws Exception;
}

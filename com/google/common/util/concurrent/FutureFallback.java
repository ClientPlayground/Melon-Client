package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;

@Beta
public interface FutureFallback<V> {
  ListenableFuture<V> create(Throwable paramThrowable) throws Exception;
}

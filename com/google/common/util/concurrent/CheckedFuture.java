package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Beta
public interface CheckedFuture<V, X extends Exception> extends ListenableFuture<V> {
  V checkedGet() throws X;
  
  V checkedGet(long paramLong, TimeUnit paramTimeUnit) throws TimeoutException, X;
}

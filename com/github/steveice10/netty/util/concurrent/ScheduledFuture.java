package com.github.steveice10.netty.util.concurrent;

import java.util.concurrent.ScheduledFuture;

public interface ScheduledFuture<V> extends Future<V>, ScheduledFuture<V> {}

package com.github.steveice10.netty.util.concurrent;

import com.github.steveice10.netty.util.internal.InternalThreadLocalMap;

public class FastThreadLocalThread extends Thread {
  private final boolean cleanupFastThreadLocals;
  
  private InternalThreadLocalMap threadLocalMap;
  
  public FastThreadLocalThread() {
    this.cleanupFastThreadLocals = false;
  }
  
  public FastThreadLocalThread(Runnable target) {
    super(FastThreadLocalRunnable.wrap(target));
    this.cleanupFastThreadLocals = true;
  }
  
  public FastThreadLocalThread(ThreadGroup group, Runnable target) {
    super(group, FastThreadLocalRunnable.wrap(target));
    this.cleanupFastThreadLocals = true;
  }
  
  public FastThreadLocalThread(String name) {
    super(name);
    this.cleanupFastThreadLocals = false;
  }
  
  public FastThreadLocalThread(ThreadGroup group, String name) {
    super(group, name);
    this.cleanupFastThreadLocals = false;
  }
  
  public FastThreadLocalThread(Runnable target, String name) {
    super(FastThreadLocalRunnable.wrap(target), name);
    this.cleanupFastThreadLocals = true;
  }
  
  public FastThreadLocalThread(ThreadGroup group, Runnable target, String name) {
    super(group, FastThreadLocalRunnable.wrap(target), name);
    this.cleanupFastThreadLocals = true;
  }
  
  public FastThreadLocalThread(ThreadGroup group, Runnable target, String name, long stackSize) {
    super(group, FastThreadLocalRunnable.wrap(target), name, stackSize);
    this.cleanupFastThreadLocals = true;
  }
  
  public final InternalThreadLocalMap threadLocalMap() {
    return this.threadLocalMap;
  }
  
  public final void setThreadLocalMap(InternalThreadLocalMap threadLocalMap) {
    this.threadLocalMap = threadLocalMap;
  }
  
  public boolean willCleanupFastThreadLocals() {
    return this.cleanupFastThreadLocals;
  }
  
  public static boolean willCleanupFastThreadLocals(Thread thread) {
    return (thread instanceof FastThreadLocalThread && ((FastThreadLocalThread)thread)
      .willCleanupFastThreadLocals());
  }
}

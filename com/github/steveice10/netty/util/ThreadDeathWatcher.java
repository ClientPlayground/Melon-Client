package com.github.steveice10.netty.util;

import com.github.steveice10.netty.util.concurrent.DefaultThreadFactory;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Deprecated
public final class ThreadDeathWatcher {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ThreadDeathWatcher.class);
  
  static final ThreadFactory threadFactory;
  
  private static final Queue<Entry> pendingEntries = new ConcurrentLinkedQueue<Entry>();
  
  private static final Watcher watcher = new Watcher();
  
  private static final AtomicBoolean started = new AtomicBoolean();
  
  private static volatile Thread watcherThread;
  
  static {
    String poolName = "threadDeathWatcher";
    String serviceThreadPrefix = SystemPropertyUtil.get("com.github.steveice10.netty.serviceThreadPrefix");
    if (!StringUtil.isNullOrEmpty(serviceThreadPrefix))
      poolName = serviceThreadPrefix + poolName; 
    threadFactory = (ThreadFactory)new DefaultThreadFactory(poolName, true, 1, null);
  }
  
  public static void watch(Thread thread, Runnable task) {
    if (thread == null)
      throw new NullPointerException("thread"); 
    if (task == null)
      throw new NullPointerException("task"); 
    if (!thread.isAlive())
      throw new IllegalArgumentException("thread must be alive."); 
    schedule(thread, task, true);
  }
  
  public static void unwatch(Thread thread, Runnable task) {
    if (thread == null)
      throw new NullPointerException("thread"); 
    if (task == null)
      throw new NullPointerException("task"); 
    schedule(thread, task, false);
  }
  
  private static void schedule(Thread thread, Runnable task, boolean isWatch) {
    pendingEntries.add(new Entry(thread, task, isWatch));
    if (started.compareAndSet(false, true)) {
      final Thread watcherThread = threadFactory.newThread(watcher);
      AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
              watcherThread.setContextClassLoader(null);
              return null;
            }
          });
      watcherThread.start();
      ThreadDeathWatcher.watcherThread = watcherThread;
    } 
  }
  
  public static boolean awaitInactivity(long timeout, TimeUnit unit) throws InterruptedException {
    if (unit == null)
      throw new NullPointerException("unit"); 
    Thread watcherThread = ThreadDeathWatcher.watcherThread;
    if (watcherThread != null) {
      watcherThread.join(unit.toMillis(timeout));
      return !watcherThread.isAlive();
    } 
    return true;
  }
  
  private static final class Watcher implements Runnable {
    private final List<ThreadDeathWatcher.Entry> watchees = new ArrayList<ThreadDeathWatcher.Entry>();
    
    public void run() {
      while (true) {
        fetchWatchees();
        notifyWatchees();
        fetchWatchees();
        notifyWatchees();
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException interruptedException) {}
        if (this.watchees.isEmpty() && ThreadDeathWatcher.pendingEntries.isEmpty()) {
          boolean stopped = ThreadDeathWatcher.started.compareAndSet(true, false);
          assert stopped;
          if (ThreadDeathWatcher.pendingEntries.isEmpty())
            break; 
          if (!ThreadDeathWatcher.started.compareAndSet(false, true))
            break; 
        } 
      } 
    }
    
    private void fetchWatchees() {
      while (true) {
        ThreadDeathWatcher.Entry e = ThreadDeathWatcher.pendingEntries.poll();
        if (e == null)
          break; 
        if (e.isWatch) {
          this.watchees.add(e);
          continue;
        } 
        this.watchees.remove(e);
      } 
    }
    
    private void notifyWatchees() {
      List<ThreadDeathWatcher.Entry> watchees = this.watchees;
      for (int i = 0; i < watchees.size(); ) {
        ThreadDeathWatcher.Entry e = watchees.get(i);
        if (!e.thread.isAlive()) {
          watchees.remove(i);
          try {
            e.task.run();
          } catch (Throwable t) {
            ThreadDeathWatcher.logger.warn("Thread death watcher task raised an exception:", t);
          } 
          continue;
        } 
        i++;
      } 
    }
    
    private Watcher() {}
  }
  
  private static final class Entry {
    final Thread thread;
    
    final Runnable task;
    
    final boolean isWatch;
    
    Entry(Thread thread, Runnable task, boolean isWatch) {
      this.thread = thread;
      this.task = task;
      this.isWatch = isWatch;
    }
    
    public int hashCode() {
      return this.thread.hashCode() ^ this.task.hashCode();
    }
    
    public boolean equals(Object obj) {
      if (obj == this)
        return true; 
      if (!(obj instanceof Entry))
        return false; 
      Entry that = (Entry)obj;
      return (this.thread == that.thread && this.task == that.task);
    }
  }
}

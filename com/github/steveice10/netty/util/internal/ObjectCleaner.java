package com.github.steveice10.netty.util.internal;

import com.github.steveice10.netty.util.concurrent.FastThreadLocalThread;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ObjectCleaner {
  private static final int REFERENCE_QUEUE_POLL_TIMEOUT_MS = Math.max(500, SystemPropertyUtil.getInt("com.github.steveice10.netty.util.internal.ObjectCleaner.refQueuePollTimeout", 10000));
  
  static final String CLEANER_THREAD_NAME = ObjectCleaner.class.getSimpleName() + "Thread";
  
  private static final Set<AutomaticCleanerReference> LIVE_SET = new ConcurrentSet<AutomaticCleanerReference>();
  
  private static final ReferenceQueue<Object> REFERENCE_QUEUE = new ReferenceQueue();
  
  private static final AtomicBoolean CLEANER_RUNNING = new AtomicBoolean(false);
  
  private static final Runnable CLEANER_TASK = new Runnable() {
      public void run() {
        boolean interrupted = false;
        do {
          while (!ObjectCleaner.LIVE_SET.isEmpty()) {
            ObjectCleaner.AutomaticCleanerReference reference;
            try {
              reference = (ObjectCleaner.AutomaticCleanerReference)ObjectCleaner.REFERENCE_QUEUE.remove(ObjectCleaner.REFERENCE_QUEUE_POLL_TIMEOUT_MS);
            } catch (InterruptedException ex) {
              interrupted = true;
              continue;
            } 
            if (reference != null) {
              try {
                reference.cleanup();
              } catch (Throwable throwable) {}
              ObjectCleaner.LIVE_SET.remove(reference);
            } 
          } 
          ObjectCleaner.CLEANER_RUNNING.set(false);
        } while (!ObjectCleaner.LIVE_SET.isEmpty() && ObjectCleaner.CLEANER_RUNNING.compareAndSet(false, true));
        if (interrupted)
          Thread.currentThread().interrupt(); 
      }
    };
  
  public static void register(Object object, Runnable cleanupTask) {
    AutomaticCleanerReference reference = new AutomaticCleanerReference(object, ObjectUtil.<Runnable>checkNotNull(cleanupTask, "cleanupTask"));
    LIVE_SET.add(reference);
    if (CLEANER_RUNNING.compareAndSet(false, true)) {
      final FastThreadLocalThread cleanupThread = new FastThreadLocalThread(CLEANER_TASK);
      fastThreadLocalThread.setPriority(1);
      AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
              cleanupThread.setContextClassLoader(null);
              return null;
            }
          });
      fastThreadLocalThread.setName(CLEANER_THREAD_NAME);
      fastThreadLocalThread.setDaemon(true);
      fastThreadLocalThread.start();
    } 
  }
  
  public static int getLiveSetCount() {
    return LIVE_SET.size();
  }
  
  private static final class AutomaticCleanerReference extends WeakReference<Object> {
    private final Runnable cleanupTask;
    
    AutomaticCleanerReference(Object referent, Runnable cleanupTask) {
      super(referent, ObjectCleaner.REFERENCE_QUEUE);
      this.cleanupTask = cleanupTask;
    }
    
    void cleanup() {
      this.cleanupTask.run();
    }
    
    public Thread get() {
      return null;
    }
    
    public void clear() {
      ObjectCleaner.LIVE_SET.remove(this);
      super.clear();
    }
  }
}

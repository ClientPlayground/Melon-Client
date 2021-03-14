package com.github.steveice10.netty.util;

import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;

public class HashedWheelTimer implements Timer {
  static final InternalLogger logger = InternalLoggerFactory.getInstance(HashedWheelTimer.class);
  
  private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
  
  private static final AtomicBoolean WARNED_TOO_MANY_INSTANCES = new AtomicBoolean();
  
  private static final int INSTANCE_COUNT_LIMIT = 64;
  
  private static final ResourceLeakDetector<HashedWheelTimer> leakDetector = ResourceLeakDetectorFactory.instance()
    .newResourceLeakDetector(HashedWheelTimer.class, 1);
  
  private static final AtomicIntegerFieldUpdater<HashedWheelTimer> WORKER_STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimer.class, "workerState");
  
  private final ResourceLeakTracker<HashedWheelTimer> leak;
  
  private final Worker worker = new Worker();
  
  private final Thread workerThread;
  
  public static final int WORKER_STATE_INIT = 0;
  
  public static final int WORKER_STATE_STARTED = 1;
  
  public static final int WORKER_STATE_SHUTDOWN = 2;
  
  private volatile int workerState;
  
  private final long tickDuration;
  
  private final HashedWheelBucket[] wheel;
  
  private final int mask;
  
  private final CountDownLatch startTimeInitialized = new CountDownLatch(1);
  
  private final Queue<HashedWheelTimeout> timeouts = PlatformDependent.newMpscQueue();
  
  private final Queue<HashedWheelTimeout> cancelledTimeouts = PlatformDependent.newMpscQueue();
  
  private final AtomicLong pendingTimeouts = new AtomicLong(0L);
  
  private final long maxPendingTimeouts;
  
  private volatile long startTime;
  
  public HashedWheelTimer() {
    this(Executors.defaultThreadFactory());
  }
  
  public HashedWheelTimer(long tickDuration, TimeUnit unit) {
    this(Executors.defaultThreadFactory(), tickDuration, unit);
  }
  
  public HashedWheelTimer(long tickDuration, TimeUnit unit, int ticksPerWheel) {
    this(Executors.defaultThreadFactory(), tickDuration, unit, ticksPerWheel);
  }
  
  public HashedWheelTimer(ThreadFactory threadFactory) {
    this(threadFactory, 100L, TimeUnit.MILLISECONDS);
  }
  
  public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit) {
    this(threadFactory, tickDuration, unit, 512);
  }
  
  public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel) {
    this(threadFactory, tickDuration, unit, ticksPerWheel, true);
  }
  
  public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel, boolean leakDetection) {
    this(threadFactory, tickDuration, unit, ticksPerWheel, leakDetection, -1L);
  }
  
  public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel, boolean leakDetection, long maxPendingTimeouts) {
    if (threadFactory == null)
      throw new NullPointerException("threadFactory"); 
    if (unit == null)
      throw new NullPointerException("unit"); 
    if (tickDuration <= 0L)
      throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration); 
    if (ticksPerWheel <= 0)
      throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel); 
    this.wheel = createWheel(ticksPerWheel);
    this.mask = this.wheel.length - 1;
    this.tickDuration = unit.toNanos(tickDuration);
    if (this.tickDuration >= Long.MAX_VALUE / this.wheel.length)
      throw new IllegalArgumentException(String.format("tickDuration: %d (expected: 0 < tickDuration in nanos < %d", new Object[] { Long.valueOf(tickDuration), Long.valueOf(Long.MAX_VALUE / this.wheel.length) })); 
    this.workerThread = threadFactory.newThread(this.worker);
    this.leak = (leakDetection || !this.workerThread.isDaemon()) ? leakDetector.track(this) : null;
    this.maxPendingTimeouts = maxPendingTimeouts;
    if (INSTANCE_COUNTER.incrementAndGet() > 64 && WARNED_TOO_MANY_INSTANCES
      .compareAndSet(false, true))
      reportTooManyInstances(); 
  }
  
  protected void finalize() throws Throwable {
    try {
      super.finalize();
    } finally {
      if (WORKER_STATE_UPDATER.getAndSet(this, 2) != 2)
        INSTANCE_COUNTER.decrementAndGet(); 
    } 
  }
  
  private static HashedWheelBucket[] createWheel(int ticksPerWheel) {
    if (ticksPerWheel <= 0)
      throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel); 
    if (ticksPerWheel > 1073741824)
      throw new IllegalArgumentException("ticksPerWheel may not be greater than 2^30: " + ticksPerWheel); 
    ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);
    HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
    for (int i = 0; i < wheel.length; i++)
      wheel[i] = new HashedWheelBucket(); 
    return wheel;
  }
  
  private static int normalizeTicksPerWheel(int ticksPerWheel) {
    int normalizedTicksPerWheel = 1;
    while (normalizedTicksPerWheel < ticksPerWheel)
      normalizedTicksPerWheel <<= 1; 
    return normalizedTicksPerWheel;
  }
  
  public void start() {
    switch (WORKER_STATE_UPDATER.get((T)this)) {
      case 0:
        if (WORKER_STATE_UPDATER.compareAndSet(this, 0, 1))
          this.workerThread.start(); 
        break;
      case 1:
        break;
      case 2:
        throw new IllegalStateException("cannot be started once stopped");
      default:
        throw new Error("Invalid WorkerState");
    } 
    while (this.startTime == 0L) {
      try {
        this.startTimeInitialized.await();
      } catch (InterruptedException interruptedException) {}
    } 
  }
  
  public Set<Timeout> stop() {
    if (Thread.currentThread() == this.workerThread)
      throw new IllegalStateException(HashedWheelTimer.class
          .getSimpleName() + ".stop() cannot be called from " + TimerTask.class
          
          .getSimpleName()); 
    if (!WORKER_STATE_UPDATER.compareAndSet(this, 1, 2)) {
      if (WORKER_STATE_UPDATER.getAndSet(this, 2) != 2) {
        INSTANCE_COUNTER.decrementAndGet();
        if (this.leak != null) {
          boolean closed = this.leak.close(this);
          assert closed;
        } 
      } 
      return Collections.emptySet();
    } 
    try {
      boolean interrupted = false;
      while (this.workerThread.isAlive()) {
        this.workerThread.interrupt();
        try {
          this.workerThread.join(100L);
        } catch (InterruptedException ignored) {
          interrupted = true;
        } 
      } 
      if (interrupted)
        Thread.currentThread().interrupt(); 
    } finally {
      INSTANCE_COUNTER.decrementAndGet();
      if (this.leak != null) {
        boolean closed = this.leak.close(this);
        assert closed;
      } 
    } 
    return this.worker.unprocessedTimeouts();
  }
  
  public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
    if (task == null)
      throw new NullPointerException("task"); 
    if (unit == null)
      throw new NullPointerException("unit"); 
    long pendingTimeoutsCount = this.pendingTimeouts.incrementAndGet();
    if (this.maxPendingTimeouts > 0L && pendingTimeoutsCount > this.maxPendingTimeouts) {
      this.pendingTimeouts.decrementAndGet();
      throw new RejectedExecutionException("Number of pending timeouts (" + pendingTimeoutsCount + ") is greater than or equal to maximum allowed pending timeouts (" + this.maxPendingTimeouts + ")");
    } 
    start();
    long deadline = System.nanoTime() + unit.toNanos(delay) - this.startTime;
    if (delay > 0L && deadline < 0L)
      deadline = Long.MAX_VALUE; 
    HashedWheelTimeout timeout = new HashedWheelTimeout(this, task, deadline);
    this.timeouts.add(timeout);
    return timeout;
  }
  
  public long pendingTimeouts() {
    return this.pendingTimeouts.get();
  }
  
  private static void reportTooManyInstances() {
    String resourceType = StringUtil.simpleClassName(HashedWheelTimer.class);
    logger.error("You are creating too many " + resourceType + " instances. " + resourceType + " is a shared resource that must be reused across the JVM,so that only a few instances are created.");
  }
  
  private final class Worker implements Runnable {
    private final Set<Timeout> unprocessedTimeouts = new HashSet<Timeout>();
    
    private long tick;
    
    public void run() {
      HashedWheelTimer.this.startTime = System.nanoTime();
      if (HashedWheelTimer.this.startTime == 0L)
        HashedWheelTimer.this.startTime = 1L; 
      HashedWheelTimer.this.startTimeInitialized.countDown();
      do {
        long deadline = waitForNextTick();
        if (deadline <= 0L)
          continue; 
        int idx = (int)(this.tick & HashedWheelTimer.this.mask);
        processCancelledTasks();
        HashedWheelTimer.HashedWheelBucket bucket = HashedWheelTimer.this.wheel[idx];
        transferTimeoutsToBuckets();
        bucket.expireTimeouts(deadline);
        this.tick++;
      } while (HashedWheelTimer.WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == 1);
      for (HashedWheelTimer.HashedWheelBucket bucket : HashedWheelTimer.this.wheel)
        bucket.clearTimeouts(this.unprocessedTimeouts); 
      while (true) {
        HashedWheelTimer.HashedWheelTimeout timeout = HashedWheelTimer.this.timeouts.poll();
        if (timeout == null)
          break; 
        if (!timeout.isCancelled())
          this.unprocessedTimeouts.add(timeout); 
      } 
      processCancelledTasks();
    }
    
    private void transferTimeoutsToBuckets() {
      for (int i = 0; i < 100000; i++) {
        HashedWheelTimer.HashedWheelTimeout timeout = HashedWheelTimer.this.timeouts.poll();
        if (timeout == null)
          break; 
        if (timeout.state() != 1) {
          long calculated = timeout.deadline / HashedWheelTimer.this.tickDuration;
          timeout.remainingRounds = (calculated - this.tick) / HashedWheelTimer.this.wheel.length;
          long ticks = Math.max(calculated, this.tick);
          int stopIndex = (int)(ticks & HashedWheelTimer.this.mask);
          HashedWheelTimer.HashedWheelBucket bucket = HashedWheelTimer.this.wheel[stopIndex];
          bucket.addTimeout(timeout);
        } 
      } 
    }
    
    private void processCancelledTasks() {
      while (true) {
        HashedWheelTimer.HashedWheelTimeout timeout = HashedWheelTimer.this.cancelledTimeouts.poll();
        if (timeout == null)
          break; 
        try {
          timeout.remove();
        } catch (Throwable t) {
          if (HashedWheelTimer.logger.isWarnEnabled())
            HashedWheelTimer.logger.warn("An exception was thrown while process a cancellation task", t); 
        } 
      } 
    }
    
    private long waitForNextTick() {
      long deadline = HashedWheelTimer.this.tickDuration * (this.tick + 1L);
      while (true) {
        long currentTime = System.nanoTime() - HashedWheelTimer.this.startTime;
        long sleepTimeMs = (deadline - currentTime + 999999L) / 1000000L;
        if (sleepTimeMs <= 0L) {
          if (currentTime == Long.MIN_VALUE)
            return -9223372036854775807L; 
          return currentTime;
        } 
        if (PlatformDependent.isWindows())
          sleepTimeMs = sleepTimeMs / 10L * 10L; 
        try {
          Thread.sleep(sleepTimeMs);
        } catch (InterruptedException ignored) {
          if (HashedWheelTimer.WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == 2)
            return Long.MIN_VALUE; 
        } 
      } 
    }
    
    public Set<Timeout> unprocessedTimeouts() {
      return Collections.unmodifiableSet(this.unprocessedTimeouts);
    }
    
    private Worker() {}
  }
  
  private static final class HashedWheelTimeout implements Timeout {
    private static final int ST_INIT = 0;
    
    private static final int ST_CANCELLED = 1;
    
    private static final int ST_EXPIRED = 2;
    
    private static final AtomicIntegerFieldUpdater<HashedWheelTimeout> STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimeout.class, "state");
    
    private final HashedWheelTimer timer;
    
    private final TimerTask task;
    
    private final long deadline;
    
    private volatile int state = 0;
    
    long remainingRounds;
    
    HashedWheelTimeout next;
    
    HashedWheelTimeout prev;
    
    HashedWheelTimer.HashedWheelBucket bucket;
    
    HashedWheelTimeout(HashedWheelTimer timer, TimerTask task, long deadline) {
      this.timer = timer;
      this.task = task;
      this.deadline = deadline;
    }
    
    public Timer timer() {
      return this.timer;
    }
    
    public TimerTask task() {
      return this.task;
    }
    
    public boolean cancel() {
      if (!compareAndSetState(0, 1))
        return false; 
      this.timer.cancelledTimeouts.add(this);
      return true;
    }
    
    void remove() {
      HashedWheelTimer.HashedWheelBucket bucket = this.bucket;
      if (bucket != null) {
        bucket.remove(this);
      } else {
        this.timer.pendingTimeouts.decrementAndGet();
      } 
    }
    
    public boolean compareAndSetState(int expected, int state) {
      return STATE_UPDATER.compareAndSet(this, expected, state);
    }
    
    public int state() {
      return this.state;
    }
    
    public boolean isCancelled() {
      return (state() == 1);
    }
    
    public boolean isExpired() {
      return (state() == 2);
    }
    
    public void expire() {
      if (!compareAndSetState(0, 2))
        return; 
      try {
        this.task.run(this);
      } catch (Throwable t) {
        if (HashedWheelTimer.logger.isWarnEnabled())
          HashedWheelTimer.logger.warn("An exception was thrown by " + TimerTask.class.getSimpleName() + '.', t); 
      } 
    }
    
    public String toString() {
      long currentTime = System.nanoTime();
      long remaining = this.deadline - currentTime + this.timer.startTime;
      StringBuilder buf = (new StringBuilder(192)).append(StringUtil.simpleClassName(this)).append('(').append("deadline: ");
      if (remaining > 0L) {
        buf.append(remaining)
          .append(" ns later");
      } else if (remaining < 0L) {
        buf.append(-remaining)
          .append(" ns ago");
      } else {
        buf.append("now");
      } 
      if (isCancelled())
        buf.append(", cancelled"); 
      return buf.append(", task: ")
        .append(task())
        .append(')')
        .toString();
    }
  }
  
  private static final class HashedWheelBucket {
    private HashedWheelTimer.HashedWheelTimeout head;
    
    private HashedWheelTimer.HashedWheelTimeout tail;
    
    private HashedWheelBucket() {}
    
    public void addTimeout(HashedWheelTimer.HashedWheelTimeout timeout) {
      assert timeout.bucket == null;
      timeout.bucket = this;
      if (this.head == null) {
        this.head = this.tail = timeout;
      } else {
        this.tail.next = timeout;
        timeout.prev = this.tail;
        this.tail = timeout;
      } 
    }
    
    public void expireTimeouts(long deadline) {
      HashedWheelTimer.HashedWheelTimeout timeout = this.head;
      while (timeout != null) {
        HashedWheelTimer.HashedWheelTimeout next = timeout.next;
        if (timeout.remainingRounds <= 0L) {
          next = remove(timeout);
          if (timeout.deadline <= deadline) {
            timeout.expire();
          } else {
            throw new IllegalStateException(String.format("timeout.deadline (%d) > deadline (%d)", new Object[] { Long.valueOf(HashedWheelTimer.HashedWheelTimeout.access$800(timeout)), Long.valueOf(deadline) }));
          } 
        } else if (timeout.isCancelled()) {
          next = remove(timeout);
        } else {
          timeout.remainingRounds--;
        } 
        timeout = next;
      } 
    }
    
    public HashedWheelTimer.HashedWheelTimeout remove(HashedWheelTimer.HashedWheelTimeout timeout) {
      HashedWheelTimer.HashedWheelTimeout next = timeout.next;
      if (timeout.prev != null)
        timeout.prev.next = next; 
      if (timeout.next != null)
        timeout.next.prev = timeout.prev; 
      if (timeout == this.head) {
        if (timeout == this.tail) {
          this.tail = null;
          this.head = null;
        } else {
          this.head = next;
        } 
      } else if (timeout == this.tail) {
        this.tail = timeout.prev;
      } 
      timeout.prev = null;
      timeout.next = null;
      timeout.bucket = null;
      timeout.timer.pendingTimeouts.decrementAndGet();
      return next;
    }
    
    public void clearTimeouts(Set<Timeout> set) {
      while (true) {
        HashedWheelTimer.HashedWheelTimeout timeout = pollTimeout();
        if (timeout == null)
          return; 
        if (timeout.isExpired() || timeout.isCancelled())
          continue; 
        set.add(timeout);
      } 
    }
    
    private HashedWheelTimer.HashedWheelTimeout pollTimeout() {
      HashedWheelTimer.HashedWheelTimeout head = this.head;
      if (head == null)
        return null; 
      HashedWheelTimer.HashedWheelTimeout next = head.next;
      if (next == null) {
        this.tail = this.head = null;
      } else {
        this.head = next;
        next.prev = null;
      } 
      head.next = null;
      head.prev = null;
      head.bucket = null;
      return head;
    }
  }
}

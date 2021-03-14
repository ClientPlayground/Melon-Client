package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.channel.EventLoopGroup;
import com.github.steveice10.netty.channel.SelectStrategy;
import com.github.steveice10.netty.channel.SingleThreadEventLoop;
import com.github.steveice10.netty.channel.unix.FileDescriptor;
import com.github.steveice10.netty.channel.unix.IovArray;
import com.github.steveice10.netty.util.IntSupplier;
import com.github.steveice10.netty.util.concurrent.RejectedExecutionHandler;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

final class KQueueEventLoop extends SingleThreadEventLoop {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(KQueueEventLoop.class);
  
  private static final AtomicIntegerFieldUpdater<KQueueEventLoop> WAKEN_UP_UPDATER = AtomicIntegerFieldUpdater.newUpdater(KQueueEventLoop.class, "wakenUp");
  
  private static final int KQUEUE_WAKE_UP_IDENT = 0;
  
  private final NativeLongArray jniChannelPointers;
  
  private final boolean allowGrowing;
  
  private final FileDescriptor kqueueFd;
  
  private final KQueueEventArray changeList;
  
  private final KQueueEventArray eventList;
  
  private final SelectStrategy selectStrategy;
  
  static {
    KQueue.ensureAvailability();
  }
  
  private final IovArray iovArray = new IovArray();
  
  private final IntSupplier selectNowSupplier = new IntSupplier() {
      public int get() throws Exception {
        return KQueueEventLoop.this.kqueueWaitNow();
      }
    };
  
  private final Callable<Integer> pendingTasksCallable = new Callable<Integer>() {
      public Integer call() throws Exception {
        return Integer.valueOf(KQueueEventLoop.this.pendingTasks());
      }
    };
  
  private volatile int wakenUp;
  
  private volatile int ioRatio = 50;
  
  static final long MAX_SCHEDULED_DAYS = 1095L;
  
  KQueueEventLoop(EventLoopGroup parent, Executor executor, int maxEvents, SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler) {
    super(parent, executor, false, DEFAULT_MAX_PENDING_TASKS, rejectedExecutionHandler);
    this.selectStrategy = (SelectStrategy)ObjectUtil.checkNotNull(strategy, "strategy");
    this.kqueueFd = Native.newKQueue();
    if (maxEvents == 0) {
      this.allowGrowing = true;
      maxEvents = 4096;
    } else {
      this.allowGrowing = false;
    } 
    this.changeList = new KQueueEventArray(maxEvents);
    this.eventList = new KQueueEventArray(maxEvents);
    this.jniChannelPointers = new NativeLongArray(4096);
    int result = Native.keventAddUserEvent(this.kqueueFd.intValue(), 0);
    if (result < 0) {
      cleanup();
      throw new IllegalStateException("kevent failed to add user event with errno: " + -result);
    } 
  }
  
  void evSet(AbstractKQueueChannel ch, short filter, short flags, int fflags) {
    this.changeList.evSet(ch, filter, flags, fflags);
  }
  
  void remove(AbstractKQueueChannel ch) throws IOException {
    assert inEventLoop();
    if (ch.jniSelfPtr == 0L)
      return; 
    this.jniChannelPointers.add(ch.jniSelfPtr);
    ch.jniSelfPtr = 0L;
  }
  
  IovArray cleanArray() {
    this.iovArray.clear();
    return this.iovArray;
  }
  
  protected void wakeup(boolean inEventLoop) {
    if (!inEventLoop && WAKEN_UP_UPDATER.compareAndSet(this, 0, 1))
      wakeup(); 
  }
  
  private void wakeup() {
    Native.keventTriggerUserEvent(this.kqueueFd.intValue(), 0);
  }
  
  private int kqueueWait(boolean oldWakeup) throws IOException {
    if (oldWakeup && hasTasks())
      return kqueueWaitNow(); 
    long totalDelay = delayNanos(System.nanoTime());
    int delaySeconds = (int)Math.min(totalDelay / 1000000000L, 2147483647L);
    return kqueueWait(delaySeconds, (int)Math.min(totalDelay - delaySeconds * 1000000000L, 2147483647L));
  }
  
  private int kqueueWaitNow() throws IOException {
    return kqueueWait(0, 0);
  }
  
  private int kqueueWait(int timeoutSec, int timeoutNs) throws IOException {
    deleteJniChannelPointers();
    int numEvents = Native.keventWait(this.kqueueFd.intValue(), this.changeList, this.eventList, timeoutSec, timeoutNs);
    this.changeList.clear();
    return numEvents;
  }
  
  private void deleteJniChannelPointers() {
    if (!this.jniChannelPointers.isEmpty()) {
      KQueueEventArray.deleteGlobalRefs(this.jniChannelPointers.memoryAddress(), this.jniChannelPointers.memoryAddressEnd());
      this.jniChannelPointers.clear();
    } 
  }
  
  private void processReady(int ready) {
    for (int i = 0; i < ready; i++) {
      short filter = this.eventList.filter(i);
      short flags = this.eventList.flags(i);
      if (filter == Native.EVFILT_USER || (flags & Native.EV_ERROR) != 0) {
        assert filter != Native.EVFILT_USER || (filter == Native.EVFILT_USER && this.eventList
          .fd(i) == 0);
      } else {
        AbstractKQueueChannel channel = this.eventList.channel(i);
        if (channel == null) {
          logger.warn("events[{}]=[{}, {}] had no channel!", new Object[] { Integer.valueOf(i), Integer.valueOf(this.eventList.fd(i)), Short.valueOf(filter) });
        } else {
          AbstractKQueueChannel.AbstractKQueueUnsafe unsafe = (AbstractKQueueChannel.AbstractKQueueUnsafe)channel.unsafe();
          if (filter == Native.EVFILT_WRITE) {
            unsafe.writeReady();
          } else if (filter == Native.EVFILT_READ) {
            unsafe.readReady(this.eventList.data(i));
          } else if (filter == Native.EVFILT_SOCK && (this.eventList.fflags(i) & Native.NOTE_RDHUP) != 0) {
            unsafe.readEOF();
          } 
          if ((flags & Native.EV_EOF) != 0)
            unsafe.readEOF(); 
        } 
      } 
    } 
  }
  
  protected void run() {
    // Byte code:
    //   0: aload_0
    //   1: getfield selectStrategy : Lcom/github/steveice10/netty/channel/SelectStrategy;
    //   4: aload_0
    //   5: getfield selectNowSupplier : Lcom/github/steveice10/netty/util/IntSupplier;
    //   8: aload_0
    //   9: invokevirtual hasTasks : ()Z
    //   12: invokeinterface calculateStrategy : (Lcom/github/steveice10/netty/util/IntSupplier;Z)I
    //   17: istore_1
    //   18: iload_1
    //   19: lookupswitch default -> 81, -2 -> 44, -1 -> 47
    //   44: goto -> 0
    //   47: aload_0
    //   48: getstatic com/github/steveice10/netty/channel/kqueue/KQueueEventLoop.WAKEN_UP_UPDATER : Ljava/util/concurrent/atomic/AtomicIntegerFieldUpdater;
    //   51: aload_0
    //   52: iconst_0
    //   53: invokevirtual getAndSet : (Ljava/lang/Object;I)I
    //   56: iconst_1
    //   57: if_icmpne -> 64
    //   60: iconst_1
    //   61: goto -> 65
    //   64: iconst_0
    //   65: invokespecial kqueueWait : (Z)I
    //   68: istore_1
    //   69: aload_0
    //   70: getfield wakenUp : I
    //   73: iconst_1
    //   74: if_icmpne -> 81
    //   77: aload_0
    //   78: invokespecial wakeup : ()V
    //   81: aload_0
    //   82: getfield ioRatio : I
    //   85: istore_2
    //   86: iload_2
    //   87: bipush #100
    //   89: if_icmpne -> 120
    //   92: iload_1
    //   93: ifle -> 101
    //   96: aload_0
    //   97: iload_1
    //   98: invokespecial processReady : (I)V
    //   101: aload_0
    //   102: invokevirtual runAllTasks : ()Z
    //   105: pop
    //   106: goto -> 117
    //   109: astore_3
    //   110: aload_0
    //   111: invokevirtual runAllTasks : ()Z
    //   114: pop
    //   115: aload_3
    //   116: athrow
    //   117: goto -> 190
    //   120: invokestatic nanoTime : ()J
    //   123: lstore #4
    //   125: iload_1
    //   126: ifle -> 134
    //   129: aload_0
    //   130: iload_1
    //   131: invokespecial processReady : (I)V
    //   134: invokestatic nanoTime : ()J
    //   137: lload #4
    //   139: lsub
    //   140: lstore #6
    //   142: aload_0
    //   143: lload #6
    //   145: bipush #100
    //   147: iload_2
    //   148: isub
    //   149: i2l
    //   150: lmul
    //   151: iload_2
    //   152: i2l
    //   153: ldiv
    //   154: invokevirtual runAllTasks : (J)Z
    //   157: pop
    //   158: goto -> 190
    //   161: astore #8
    //   163: invokestatic nanoTime : ()J
    //   166: lload #4
    //   168: lsub
    //   169: lstore #9
    //   171: aload_0
    //   172: lload #9
    //   174: bipush #100
    //   176: iload_2
    //   177: isub
    //   178: i2l
    //   179: lmul
    //   180: iload_2
    //   181: i2l
    //   182: ldiv
    //   183: invokevirtual runAllTasks : (J)Z
    //   186: pop
    //   187: aload #8
    //   189: athrow
    //   190: aload_0
    //   191: getfield allowGrowing : Z
    //   194: ifeq -> 216
    //   197: iload_1
    //   198: aload_0
    //   199: getfield eventList : Lcom/github/steveice10/netty/channel/kqueue/KQueueEventArray;
    //   202: invokevirtual capacity : ()I
    //   205: if_icmpne -> 216
    //   208: aload_0
    //   209: getfield eventList : Lcom/github/steveice10/netty/channel/kqueue/KQueueEventArray;
    //   212: iconst_0
    //   213: invokevirtual realloc : (Z)V
    //   216: goto -> 224
    //   219: astore_1
    //   220: aload_1
    //   221: invokestatic handleLoopException : (Ljava/lang/Throwable;)V
    //   224: aload_0
    //   225: invokevirtual isShuttingDown : ()Z
    //   228: ifeq -> 245
    //   231: aload_0
    //   232: invokespecial closeAll : ()V
    //   235: aload_0
    //   236: invokevirtual confirmShutdown : ()Z
    //   239: ifeq -> 245
    //   242: goto -> 256
    //   245: goto -> 0
    //   248: astore_1
    //   249: aload_1
    //   250: invokestatic handleLoopException : (Ljava/lang/Throwable;)V
    //   253: goto -> 0
    //   256: return
    // Line number table:
    //   Java source line number -> byte code offset
    //   #216	-> 0
    //   #217	-> 18
    //   #219	-> 44
    //   #221	-> 47
    //   #251	-> 69
    //   #252	-> 77
    //   #258	-> 81
    //   #259	-> 86
    //   #261	-> 92
    //   #262	-> 96
    //   #265	-> 101
    //   #266	-> 106
    //   #265	-> 109
    //   #266	-> 115
    //   #268	-> 120
    //   #271	-> 125
    //   #272	-> 129
    //   #275	-> 134
    //   #276	-> 142
    //   #277	-> 158
    //   #275	-> 161
    //   #276	-> 171
    //   #277	-> 187
    //   #279	-> 190
    //   #281	-> 208
    //   #285	-> 216
    //   #283	-> 219
    //   #284	-> 220
    //   #288	-> 224
    //   #289	-> 231
    //   #290	-> 235
    //   #291	-> 242
    //   #296	-> 245
    //   #294	-> 248
    //   #295	-> 249
    //   #296	-> 253
    //   #298	-> 256
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   142	16	6	ioTime	J
    //   171	16	9	ioTime	J
    //   125	65	4	ioStartTime	J
    //   18	198	1	strategy	I
    //   86	130	2	ioRatio	I
    //   220	4	1	t	Ljava/lang/Throwable;
    //   249	4	1	t	Ljava/lang/Throwable;
    //   0	257	0	this	Lcom/github/steveice10/netty/channel/kqueue/KQueueEventLoop;
    // Exception table:
    //   from	to	target	type
    //   0	44	219	java/lang/Throwable
    //   47	216	219	java/lang/Throwable
    //   92	101	109	finally
    //   125	134	161	finally
    //   161	163	161	finally
    //   224	242	248	java/lang/Throwable
  }
  
  protected Queue<Runnable> newTaskQueue(int maxPendingTasks) {
    return (maxPendingTasks == Integer.MAX_VALUE) ? PlatformDependent.newMpscQueue() : 
      PlatformDependent.newMpscQueue(maxPendingTasks);
  }
  
  public int pendingTasks() {
    return inEventLoop() ? super.pendingTasks() : ((Integer)submit(this.pendingTasksCallable).syncUninterruptibly().getNow()).intValue();
  }
  
  public int getIoRatio() {
    return this.ioRatio;
  }
  
  public void setIoRatio(int ioRatio) {
    if (ioRatio <= 0 || ioRatio > 100)
      throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)"); 
    this.ioRatio = ioRatio;
  }
  
  protected void cleanup() {
    try {
      try {
        this.kqueueFd.close();
      } catch (IOException e) {
        logger.warn("Failed to close the kqueue fd.", e);
      } 
    } finally {
      deleteJniChannelPointers();
      this.jniChannelPointers.free();
      this.changeList.free();
      this.eventList.free();
    } 
  }
  
  private void closeAll() {
    try {
      kqueueWaitNow();
    } catch (IOException iOException) {}
  }
  
  private static void handleLoopException(Throwable t) {
    logger.warn("Unexpected exception in the selector loop.", t);
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException interruptedException) {}
  }
  
  protected void validateScheduled(long amount, TimeUnit unit) {
    long days = unit.toDays(amount);
    if (days > 1095L)
      throw new IllegalArgumentException("days: " + days + " (expected: < " + 1095L + ')'); 
  }
}

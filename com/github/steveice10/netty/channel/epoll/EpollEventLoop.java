package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.channel.EventLoopGroup;
import com.github.steveice10.netty.channel.SelectStrategy;
import com.github.steveice10.netty.channel.SingleThreadEventLoop;
import com.github.steveice10.netty.channel.unix.FileDescriptor;
import com.github.steveice10.netty.channel.unix.IovArray;
import com.github.steveice10.netty.util.IntSupplier;
import com.github.steveice10.netty.util.collection.IntObjectHashMap;
import com.github.steveice10.netty.util.collection.IntObjectMap;
import com.github.steveice10.netty.util.concurrent.RejectedExecutionHandler;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

final class EpollEventLoop extends SingleThreadEventLoop {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(EpollEventLoop.class);
  
  private static final AtomicIntegerFieldUpdater<EpollEventLoop> WAKEN_UP_UPDATER = AtomicIntegerFieldUpdater.newUpdater(EpollEventLoop.class, "wakenUp");
  
  private final FileDescriptor epollFd;
  
  private final FileDescriptor eventFd;
  
  private final FileDescriptor timerFd;
  
  static {
    Epoll.ensureAvailability();
  }
  
  private final IntObjectMap<AbstractEpollChannel> channels = (IntObjectMap<AbstractEpollChannel>)new IntObjectHashMap(4096);
  
  private final boolean allowGrowing;
  
  private final EpollEventArray events;
  
  private final IovArray iovArray = new IovArray();
  
  private final SelectStrategy selectStrategy;
  
  private final IntSupplier selectNowSupplier = new IntSupplier() {
      public int get() throws Exception {
        return EpollEventLoop.this.epollWaitNow();
      }
    };
  
  private final Callable<Integer> pendingTasksCallable = new Callable<Integer>() {
      public Integer call() throws Exception {
        return Integer.valueOf(EpollEventLoop.this.pendingTasks());
      }
    };
  
  private volatile int wakenUp;
  
  private volatile int ioRatio = 50;
  
  static final long MAX_SCHEDULED_DAYS = TimeUnit.SECONDS.toDays(999999999L);
  
  EpollEventLoop(EventLoopGroup parent, Executor executor, int maxEvents, SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler) {
    super(parent, executor, false, DEFAULT_MAX_PENDING_TASKS, rejectedExecutionHandler);
    this.selectStrategy = (SelectStrategy)ObjectUtil.checkNotNull(strategy, "strategy");
    if (maxEvents == 0) {
      this.allowGrowing = true;
      this.events = new EpollEventArray(4096);
    } else {
      this.allowGrowing = false;
      this.events = new EpollEventArray(maxEvents);
    } 
    boolean success = false;
    FileDescriptor epollFd = null;
    FileDescriptor eventFd = null;
    FileDescriptor timerFd = null;
    try {
      this.epollFd = epollFd = Native.newEpollCreate();
      this.eventFd = eventFd = Native.newEventFd();
      try {
        Native.epollCtlAdd(epollFd.intValue(), eventFd.intValue(), Native.EPOLLIN);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to add eventFd filedescriptor to epoll", e);
      } 
      this.timerFd = timerFd = Native.newTimerFd();
      try {
        Native.epollCtlAdd(epollFd.intValue(), timerFd.intValue(), Native.EPOLLIN | Native.EPOLLET);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to add timerFd filedescriptor to epoll", e);
      } 
      success = true;
    } finally {
      if (!success) {
        if (epollFd != null)
          try {
            epollFd.close();
          } catch (Exception exception) {} 
        if (eventFd != null)
          try {
            eventFd.close();
          } catch (Exception exception) {} 
        if (timerFd != null)
          try {
            timerFd.close();
          } catch (Exception exception) {} 
      } 
    } 
  }
  
  IovArray cleanArray() {
    this.iovArray.clear();
    return this.iovArray;
  }
  
  protected void wakeup(boolean inEventLoop) {
    if (!inEventLoop && WAKEN_UP_UPDATER.compareAndSet(this, 0, 1))
      Native.eventFdWrite(this.eventFd.intValue(), 1L); 
  }
  
  void add(AbstractEpollChannel ch) throws IOException {
    assert inEventLoop();
    int fd = ch.socket.intValue();
    Native.epollCtlAdd(this.epollFd.intValue(), fd, ch.flags);
    this.channels.put(fd, ch);
  }
  
  void modify(AbstractEpollChannel ch) throws IOException {
    assert inEventLoop();
    Native.epollCtlMod(this.epollFd.intValue(), ch.socket.intValue(), ch.flags);
  }
  
  void remove(AbstractEpollChannel ch) throws IOException {
    assert inEventLoop();
    if (ch.isOpen()) {
      int fd = ch.socket.intValue();
      if (this.channels.remove(fd) != null)
        Native.epollCtlDel(this.epollFd.intValue(), ch.fd().intValue()); 
    } 
  }
  
  protected Queue<Runnable> newTaskQueue(int maxPendingTasks) {
    return (maxPendingTasks == Integer.MAX_VALUE) ? PlatformDependent.newMpscQueue() : 
      PlatformDependent.newMpscQueue(maxPendingTasks);
  }
  
  public int pendingTasks() {
    if (inEventLoop())
      return super.pendingTasks(); 
    return ((Integer)submit(this.pendingTasksCallable).syncUninterruptibly().getNow()).intValue();
  }
  
  public int getIoRatio() {
    return this.ioRatio;
  }
  
  public void setIoRatio(int ioRatio) {
    if (ioRatio <= 0 || ioRatio > 100)
      throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)"); 
    this.ioRatio = ioRatio;
  }
  
  private int epollWait(boolean oldWakeup) throws IOException {
    if (oldWakeup && hasTasks())
      return epollWaitNow(); 
    long totalDelay = delayNanos(System.nanoTime());
    int delaySeconds = (int)Math.min(totalDelay / 1000000000L, 2147483647L);
    return Native.epollWait(this.epollFd, this.events, this.timerFd, delaySeconds, 
        (int)Math.min(totalDelay - delaySeconds * 1000000000L, 2147483647L));
  }
  
  private int epollWaitNow() throws IOException {
    return Native.epollWait(this.epollFd, this.events, this.timerFd, 0, 0);
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
    //   19: lookupswitch default -> 88, -2 -> 44, -1 -> 47
    //   44: goto -> 0
    //   47: aload_0
    //   48: getstatic com/github/steveice10/netty/channel/epoll/EpollEventLoop.WAKEN_UP_UPDATER : Ljava/util/concurrent/atomic/AtomicIntegerFieldUpdater;
    //   51: aload_0
    //   52: iconst_0
    //   53: invokevirtual getAndSet : (Ljava/lang/Object;I)I
    //   56: iconst_1
    //   57: if_icmpne -> 64
    //   60: iconst_1
    //   61: goto -> 65
    //   64: iconst_0
    //   65: invokespecial epollWait : (Z)I
    //   68: istore_1
    //   69: aload_0
    //   70: getfield wakenUp : I
    //   73: iconst_1
    //   74: if_icmpne -> 88
    //   77: aload_0
    //   78: getfield eventFd : Lcom/github/steveice10/netty/channel/unix/FileDescriptor;
    //   81: invokevirtual intValue : ()I
    //   84: lconst_1
    //   85: invokestatic eventFdWrite : (IJ)V
    //   88: aload_0
    //   89: getfield ioRatio : I
    //   92: istore_2
    //   93: iload_2
    //   94: bipush #100
    //   96: if_icmpne -> 131
    //   99: iload_1
    //   100: ifle -> 112
    //   103: aload_0
    //   104: aload_0
    //   105: getfield events : Lcom/github/steveice10/netty/channel/epoll/EpollEventArray;
    //   108: iload_1
    //   109: invokespecial processReady : (Lcom/github/steveice10/netty/channel/epoll/EpollEventArray;I)V
    //   112: aload_0
    //   113: invokevirtual runAllTasks : ()Z
    //   116: pop
    //   117: goto -> 128
    //   120: astore_3
    //   121: aload_0
    //   122: invokevirtual runAllTasks : ()Z
    //   125: pop
    //   126: aload_3
    //   127: athrow
    //   128: goto -> 205
    //   131: invokestatic nanoTime : ()J
    //   134: lstore #4
    //   136: iload_1
    //   137: ifle -> 149
    //   140: aload_0
    //   141: aload_0
    //   142: getfield events : Lcom/github/steveice10/netty/channel/epoll/EpollEventArray;
    //   145: iload_1
    //   146: invokespecial processReady : (Lcom/github/steveice10/netty/channel/epoll/EpollEventArray;I)V
    //   149: invokestatic nanoTime : ()J
    //   152: lload #4
    //   154: lsub
    //   155: lstore #6
    //   157: aload_0
    //   158: lload #6
    //   160: bipush #100
    //   162: iload_2
    //   163: isub
    //   164: i2l
    //   165: lmul
    //   166: iload_2
    //   167: i2l
    //   168: ldiv
    //   169: invokevirtual runAllTasks : (J)Z
    //   172: pop
    //   173: goto -> 205
    //   176: astore #8
    //   178: invokestatic nanoTime : ()J
    //   181: lload #4
    //   183: lsub
    //   184: lstore #9
    //   186: aload_0
    //   187: lload #9
    //   189: bipush #100
    //   191: iload_2
    //   192: isub
    //   193: i2l
    //   194: lmul
    //   195: iload_2
    //   196: i2l
    //   197: ldiv
    //   198: invokevirtual runAllTasks : (J)Z
    //   201: pop
    //   202: aload #8
    //   204: athrow
    //   205: aload_0
    //   206: getfield allowGrowing : Z
    //   209: ifeq -> 230
    //   212: iload_1
    //   213: aload_0
    //   214: getfield events : Lcom/github/steveice10/netty/channel/epoll/EpollEventArray;
    //   217: invokevirtual length : ()I
    //   220: if_icmpne -> 230
    //   223: aload_0
    //   224: getfield events : Lcom/github/steveice10/netty/channel/epoll/EpollEventArray;
    //   227: invokevirtual increase : ()V
    //   230: goto -> 238
    //   233: astore_1
    //   234: aload_1
    //   235: invokestatic handleLoopException : (Ljava/lang/Throwable;)V
    //   238: aload_0
    //   239: invokevirtual isShuttingDown : ()Z
    //   242: ifeq -> 259
    //   245: aload_0
    //   246: invokespecial closeAll : ()V
    //   249: aload_0
    //   250: invokevirtual confirmShutdown : ()Z
    //   253: ifeq -> 259
    //   256: goto -> 270
    //   259: goto -> 0
    //   262: astore_1
    //   263: aload_1
    //   264: invokestatic handleLoopException : (Ljava/lang/Throwable;)V
    //   267: goto -> 0
    //   270: return
    // Line number table:
    //   Java source line number -> byte code offset
    //   #251	-> 0
    //   #252	-> 18
    //   #254	-> 44
    //   #256	-> 47
    //   #286	-> 69
    //   #287	-> 77
    //   #293	-> 88
    //   #294	-> 93
    //   #296	-> 99
    //   #297	-> 103
    //   #301	-> 112
    //   #302	-> 117
    //   #301	-> 120
    //   #302	-> 126
    //   #304	-> 131
    //   #307	-> 136
    //   #308	-> 140
    //   #312	-> 149
    //   #313	-> 157
    //   #314	-> 173
    //   #312	-> 176
    //   #313	-> 186
    //   #314	-> 202
    //   #316	-> 205
    //   #318	-> 223
    //   #322	-> 230
    //   #320	-> 233
    //   #321	-> 234
    //   #325	-> 238
    //   #326	-> 245
    //   #327	-> 249
    //   #328	-> 256
    //   #333	-> 259
    //   #331	-> 262
    //   #332	-> 263
    //   #333	-> 267
    //   #335	-> 270
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   157	16	6	ioTime	J
    //   186	16	9	ioTime	J
    //   136	69	4	ioStartTime	J
    //   18	212	1	strategy	I
    //   93	137	2	ioRatio	I
    //   234	4	1	t	Ljava/lang/Throwable;
    //   263	4	1	t	Ljava/lang/Throwable;
    //   0	271	0	this	Lcom/github/steveice10/netty/channel/epoll/EpollEventLoop;
    // Exception table:
    //   from	to	target	type
    //   0	44	233	java/lang/Throwable
    //   47	230	233	java/lang/Throwable
    //   99	112	120	finally
    //   136	149	176	finally
    //   176	178	176	finally
    //   238	256	262	java/lang/Throwable
  }
  
  private static void handleLoopException(Throwable t) {
    logger.warn("Unexpected exception in the selector loop.", t);
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException interruptedException) {}
  }
  
  private void closeAll() {
    try {
      epollWaitNow();
    } catch (IOException iOException) {}
    Collection<AbstractEpollChannel> array = new ArrayList<AbstractEpollChannel>(this.channels.size());
    for (AbstractEpollChannel channel : this.channels.values())
      array.add(channel); 
    for (AbstractEpollChannel ch : array)
      ch.unsafe().close(ch.unsafe().voidPromise()); 
  }
  
  private void processReady(EpollEventArray events, int ready) {
    for (int i = 0; i < ready; i++) {
      int fd = events.fd(i);
      if (fd == this.eventFd.intValue()) {
        Native.eventFdRead(fd);
      } else if (fd == this.timerFd.intValue()) {
        Native.timerFdRead(fd);
      } else {
        long ev = events.events(i);
        AbstractEpollChannel ch = (AbstractEpollChannel)this.channels.get(fd);
        if (ch != null) {
          AbstractEpollChannel.AbstractEpollUnsafe unsafe = (AbstractEpollChannel.AbstractEpollUnsafe)ch.unsafe();
          if ((ev & (Native.EPOLLERR | Native.EPOLLOUT)) != 0L)
            unsafe.epollOutReady(); 
          if ((ev & (Native.EPOLLERR | Native.EPOLLIN)) != 0L)
            unsafe.epollInReady(); 
          if ((ev & Native.EPOLLRDHUP) != 0L)
            unsafe.epollRdHupReady(); 
        } else {
          try {
            Native.epollCtlDel(this.epollFd.intValue(), fd);
          } catch (IOException iOException) {}
        } 
      } 
    } 
  }
  
  protected void cleanup() {
    try {
      try {
        this.epollFd.close();
      } catch (IOException e) {
        logger.warn("Failed to close the epoll fd.", e);
      } 
      try {
        this.eventFd.close();
      } catch (IOException e) {
        logger.warn("Failed to close the event fd.", e);
      } 
      try {
        this.timerFd.close();
      } catch (IOException e) {
        logger.warn("Failed to close the timer fd.", e);
      } 
    } finally {
      this.iovArray.release();
      this.events.free();
    } 
  }
  
  protected void validateScheduled(long amount, TimeUnit unit) {
    long days = unit.toDays(amount);
    if (days > MAX_SCHEDULED_DAYS)
      throw new IllegalArgumentException("days: " + days + " (expected: < " + MAX_SCHEDULED_DAYS + ')'); 
  }
}

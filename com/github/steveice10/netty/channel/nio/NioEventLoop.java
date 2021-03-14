package com.github.steveice10.netty.channel.nio;

import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.EventLoopException;
import com.github.steveice10.netty.channel.EventLoopGroup;
import com.github.steveice10.netty.channel.SelectStrategy;
import com.github.steveice10.netty.channel.SingleThreadEventLoop;
import com.github.steveice10.netty.util.IntSupplier;
import com.github.steveice10.netty.util.concurrent.RejectedExecutionHandler;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.ReflectionUtil;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NioEventLoop extends SingleThreadEventLoop {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioEventLoop.class);
  
  private static final int CLEANUP_INTERVAL = 256;
  
  private static final boolean DISABLE_KEYSET_OPTIMIZATION = SystemPropertyUtil.getBoolean("com.github.steveice10.netty.noKeySetOptimization", false);
  
  private static final int MIN_PREMATURE_SELECTOR_RETURNS = 3;
  
  private static final int SELECTOR_AUTO_REBUILD_THRESHOLD;
  
  private final IntSupplier selectNowSupplier = new IntSupplier() {
      public int get() throws Exception {
        return NioEventLoop.this.selectNow();
      }
    };
  
  private final Callable<Integer> pendingTasksCallable = new Callable<Integer>() {
      public Integer call() throws Exception {
        return Integer.valueOf(NioEventLoop.this.pendingTasks());
      }
    };
  
  static final long MAX_SCHEDULED_DAYS = 1095L;
  
  private Selector selector;
  
  private Selector unwrappedSelector;
  
  private SelectedSelectionKeySet selectedKeys;
  
  private final SelectorProvider provider;
  
  static {
    String key = "sun.nio.ch.bugLevel";
    String buglevel = SystemPropertyUtil.get("sun.nio.ch.bugLevel");
    if (buglevel == null)
      try {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
              public Void run() {
                System.setProperty("sun.nio.ch.bugLevel", "");
                return null;
              }
            });
      } catch (SecurityException e) {
        logger.debug("Unable to get/set System Property: sun.nio.ch.bugLevel", e);
      }  
    int selectorAutoRebuildThreshold = SystemPropertyUtil.getInt("com.github.steveice10.netty.selectorAutoRebuildThreshold", 512);
    if (selectorAutoRebuildThreshold < 3)
      selectorAutoRebuildThreshold = 0; 
    SELECTOR_AUTO_REBUILD_THRESHOLD = selectorAutoRebuildThreshold;
    if (logger.isDebugEnabled()) {
      logger.debug("-Dio.netty.noKeySetOptimization: {}", Boolean.valueOf(DISABLE_KEYSET_OPTIMIZATION));
      logger.debug("-Dio.netty.selectorAutoRebuildThreshold: {}", Integer.valueOf(SELECTOR_AUTO_REBUILD_THRESHOLD));
    } 
  }
  
  private final AtomicBoolean wakenUp = new AtomicBoolean();
  
  private final SelectStrategy selectStrategy;
  
  private volatile int ioRatio = 50;
  
  private int cancelledKeys;
  
  private boolean needsToSelectAgain;
  
  NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider, SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler) {
    super((EventLoopGroup)parent, executor, false, DEFAULT_MAX_PENDING_TASKS, rejectedExecutionHandler);
    if (selectorProvider == null)
      throw new NullPointerException("selectorProvider"); 
    if (strategy == null)
      throw new NullPointerException("selectStrategy"); 
    this.provider = selectorProvider;
    SelectorTuple selectorTuple = openSelector();
    this.selector = selectorTuple.selector;
    this.unwrappedSelector = selectorTuple.unwrappedSelector;
    this.selectStrategy = strategy;
  }
  
  private static final class SelectorTuple {
    final Selector unwrappedSelector;
    
    final Selector selector;
    
    SelectorTuple(Selector unwrappedSelector) {
      this.unwrappedSelector = unwrappedSelector;
      this.selector = unwrappedSelector;
    }
    
    SelectorTuple(Selector unwrappedSelector, Selector selector) {
      this.unwrappedSelector = unwrappedSelector;
      this.selector = selector;
    }
  }
  
  private SelectorTuple openSelector() {
    final Selector unwrappedSelector;
    try {
      unwrappedSelector = this.provider.openSelector();
    } catch (IOException e) {
      throw new ChannelException("failed to open a new selector", e);
    } 
    if (DISABLE_KEYSET_OPTIMIZATION)
      return new SelectorTuple(unwrappedSelector); 
    final SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();
    Object maybeSelectorImplClass = AccessController.doPrivileged(new PrivilegedAction() {
          public Object run() {
            try {
              return Class.forName("sun.nio.ch.SelectorImpl", false, 
                  
                  PlatformDependent.getSystemClassLoader());
            } catch (Throwable cause) {
              return cause;
            } 
          }
        });
    if (!(maybeSelectorImplClass instanceof Class) || 
      
      !((Class)maybeSelectorImplClass).isAssignableFrom(unwrappedSelector.getClass())) {
      if (maybeSelectorImplClass instanceof Throwable) {
        Throwable t = (Throwable)maybeSelectorImplClass;
        logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, t);
      } 
      return new SelectorTuple(unwrappedSelector);
    } 
    final Class<?> selectorImplClass = (Class)maybeSelectorImplClass;
    Object maybeException = AccessController.doPrivileged(new PrivilegedAction() {
          public Object run() {
            try {
              Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
              Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");
              Throwable cause = ReflectionUtil.trySetAccessible(selectedKeysField, true);
              if (cause != null)
                return cause; 
              cause = ReflectionUtil.trySetAccessible(publicSelectedKeysField, true);
              if (cause != null)
                return cause; 
              selectedKeysField.set(unwrappedSelector, selectedKeySet);
              publicSelectedKeysField.set(unwrappedSelector, selectedKeySet);
              return null;
            } catch (NoSuchFieldException e) {
              return e;
            } catch (IllegalAccessException e) {
              return e;
            } 
          }
        });
    if (maybeException instanceof Exception) {
      this.selectedKeys = null;
      Exception e = (Exception)maybeException;
      logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, e);
      return new SelectorTuple(unwrappedSelector);
    } 
    this.selectedKeys = selectedKeySet;
    logger.trace("instrumented a special java.util.Set into: {}", unwrappedSelector);
    return new SelectorTuple(unwrappedSelector, new SelectedSelectionKeySetSelector(unwrappedSelector, selectedKeySet));
  }
  
  public SelectorProvider selectorProvider() {
    return this.provider;
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
  
  public void register(SelectableChannel ch, int interestOps, NioTask<?> task) {
    if (ch == null)
      throw new NullPointerException("ch"); 
    if (interestOps == 0)
      throw new IllegalArgumentException("interestOps must be non-zero."); 
    if ((interestOps & (ch.validOps() ^ 0xFFFFFFFF)) != 0)
      throw new IllegalArgumentException("invalid interestOps: " + interestOps + "(validOps: " + ch
          .validOps() + ')'); 
    if (task == null)
      throw new NullPointerException("task"); 
    if (isShutdown())
      throw new IllegalStateException("event loop shut down"); 
    try {
      ch.register(this.selector, interestOps, task);
    } catch (Exception e) {
      throw new EventLoopException("failed to register a channel", e);
    } 
  }
  
  public int getIoRatio() {
    return this.ioRatio;
  }
  
  public void setIoRatio(int ioRatio) {
    if (ioRatio <= 0 || ioRatio > 100)
      throw new IllegalArgumentException("ioRatio: " + ioRatio + " (expected: 0 < ioRatio <= 100)"); 
    this.ioRatio = ioRatio;
  }
  
  public void rebuildSelector() {
    if (!inEventLoop()) {
      execute(new Runnable() {
            public void run() {
              NioEventLoop.this.rebuildSelector0();
            }
          });
      return;
    } 
    rebuildSelector0();
  }
  
  private void rebuildSelector0() {
    SelectorTuple newSelectorTuple;
    Selector oldSelector = this.selector;
    if (oldSelector == null)
      return; 
    try {
      newSelectorTuple = openSelector();
    } catch (Exception e) {
      logger.warn("Failed to create a new Selector.", e);
      return;
    } 
    int nChannels = 0;
    for (SelectionKey key : oldSelector.keys()) {
      Object a = key.attachment();
      try {
        if (!key.isValid() || key.channel().keyFor(newSelectorTuple.unwrappedSelector) != null)
          continue; 
        int interestOps = key.interestOps();
        key.cancel();
        SelectionKey newKey = key.channel().register(newSelectorTuple.unwrappedSelector, interestOps, a);
        if (a instanceof AbstractNioChannel)
          ((AbstractNioChannel)a).selectionKey = newKey; 
        nChannels++;
      } catch (Exception e) {
        logger.warn("Failed to re-register a Channel to the new Selector.", e);
        if (a instanceof AbstractNioChannel) {
          AbstractNioChannel ch = (AbstractNioChannel)a;
          ch.unsafe().close(ch.unsafe().voidPromise());
          continue;
        } 
        NioTask<SelectableChannel> task = (NioTask<SelectableChannel>)a;
        invokeChannelUnregistered(task, key, e);
      } 
    } 
    this.selector = newSelectorTuple.selector;
    this.unwrappedSelector = newSelectorTuple.unwrappedSelector;
    try {
      oldSelector.close();
    } catch (Throwable t) {
      if (logger.isWarnEnabled())
        logger.warn("Failed to close the old Selector.", t); 
    } 
    logger.info("Migrated " + nChannels + " channel(s) to the new Selector.");
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
    //   17: lookupswitch default -> 77, -2 -> 44, -1 -> 47
    //   44: goto -> 0
    //   47: aload_0
    //   48: aload_0
    //   49: getfield wakenUp : Ljava/util/concurrent/atomic/AtomicBoolean;
    //   52: iconst_0
    //   53: invokevirtual getAndSet : (Z)Z
    //   56: invokespecial select : (Z)V
    //   59: aload_0
    //   60: getfield wakenUp : Ljava/util/concurrent/atomic/AtomicBoolean;
    //   63: invokevirtual get : ()Z
    //   66: ifeq -> 77
    //   69: aload_0
    //   70: getfield selector : Ljava/nio/channels/Selector;
    //   73: invokevirtual wakeup : ()Ljava/nio/channels/Selector;
    //   76: pop
    //   77: aload_0
    //   78: iconst_0
    //   79: putfield cancelledKeys : I
    //   82: aload_0
    //   83: iconst_0
    //   84: putfield needsToSelectAgain : Z
    //   87: aload_0
    //   88: getfield ioRatio : I
    //   91: istore_1
    //   92: iload_1
    //   93: bipush #100
    //   95: if_icmpne -> 121
    //   98: aload_0
    //   99: invokespecial processSelectedKeys : ()V
    //   102: aload_0
    //   103: invokevirtual runAllTasks : ()Z
    //   106: pop
    //   107: goto -> 118
    //   110: astore_2
    //   111: aload_0
    //   112: invokevirtual runAllTasks : ()Z
    //   115: pop
    //   116: aload_2
    //   117: athrow
    //   118: goto -> 183
    //   121: invokestatic nanoTime : ()J
    //   124: lstore_3
    //   125: aload_0
    //   126: invokespecial processSelectedKeys : ()V
    //   129: invokestatic nanoTime : ()J
    //   132: lload_3
    //   133: lsub
    //   134: lstore #5
    //   136: aload_0
    //   137: lload #5
    //   139: bipush #100
    //   141: iload_1
    //   142: isub
    //   143: i2l
    //   144: lmul
    //   145: iload_1
    //   146: i2l
    //   147: ldiv
    //   148: invokevirtual runAllTasks : (J)Z
    //   151: pop
    //   152: goto -> 183
    //   155: astore #7
    //   157: invokestatic nanoTime : ()J
    //   160: lload_3
    //   161: lsub
    //   162: lstore #8
    //   164: aload_0
    //   165: lload #8
    //   167: bipush #100
    //   169: iload_1
    //   170: isub
    //   171: i2l
    //   172: lmul
    //   173: iload_1
    //   174: i2l
    //   175: ldiv
    //   176: invokevirtual runAllTasks : (J)Z
    //   179: pop
    //   180: aload #7
    //   182: athrow
    //   183: goto -> 191
    //   186: astore_1
    //   187: aload_1
    //   188: invokestatic handleLoopException : (Ljava/lang/Throwable;)V
    //   191: aload_0
    //   192: invokevirtual isShuttingDown : ()Z
    //   195: ifeq -> 210
    //   198: aload_0
    //   199: invokespecial closeAll : ()V
    //   202: aload_0
    //   203: invokevirtual confirmShutdown : ()Z
    //   206: ifeq -> 210
    //   209: return
    //   210: goto -> 0
    //   213: astore_1
    //   214: aload_1
    //   215: invokestatic handleLoopException : (Ljava/lang/Throwable;)V
    //   218: goto -> 0
    // Line number table:
    //   Java source line number -> byte code offset
    //   #407	-> 0
    //   #409	-> 44
    //   #411	-> 47
    //   #441	-> 59
    //   #442	-> 69
    //   #448	-> 77
    //   #449	-> 82
    //   #450	-> 87
    //   #451	-> 92
    //   #453	-> 98
    //   #456	-> 102
    //   #457	-> 107
    //   #456	-> 110
    //   #457	-> 116
    //   #459	-> 121
    //   #461	-> 125
    //   #464	-> 129
    //   #465	-> 136
    //   #466	-> 152
    //   #464	-> 155
    //   #465	-> 164
    //   #466	-> 180
    //   #470	-> 183
    //   #468	-> 186
    //   #469	-> 187
    //   #473	-> 191
    //   #474	-> 198
    //   #475	-> 202
    //   #476	-> 209
    //   #481	-> 210
    //   #479	-> 213
    //   #480	-> 214
    //   #481	-> 218
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   136	16	5	ioTime	J
    //   164	16	8	ioTime	J
    //   125	58	3	ioStartTime	J
    //   92	91	1	ioRatio	I
    //   187	4	1	t	Ljava/lang/Throwable;
    //   214	4	1	t	Ljava/lang/Throwable;
    //   0	221	0	this	Lcom/github/steveice10/netty/channel/nio/NioEventLoop;
    // Exception table:
    //   from	to	target	type
    //   0	44	186	java/lang/Throwable
    //   47	183	186	java/lang/Throwable
    //   98	102	110	finally
    //   125	129	155	finally
    //   155	157	155	finally
    //   191	209	213	java/lang/Throwable
  }
  
  private static void handleLoopException(Throwable t) {
    logger.warn("Unexpected exception in the selector loop.", t);
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException interruptedException) {}
  }
  
  private void processSelectedKeys() {
    if (this.selectedKeys != null) {
      processSelectedKeysOptimized();
    } else {
      processSelectedKeysPlain(this.selector.selectedKeys());
    } 
  }
  
  protected void cleanup() {
    try {
      this.selector.close();
    } catch (IOException e) {
      logger.warn("Failed to close a selector.", e);
    } 
  }
  
  void cancel(SelectionKey key) {
    key.cancel();
    this.cancelledKeys++;
    if (this.cancelledKeys >= 256) {
      this.cancelledKeys = 0;
      this.needsToSelectAgain = true;
    } 
  }
  
  protected Runnable pollTask() {
    Runnable task = super.pollTask();
    if (this.needsToSelectAgain)
      selectAgain(); 
    return task;
  }
  
  private void processSelectedKeysPlain(Set<SelectionKey> selectedKeys) {
    if (selectedKeys.isEmpty())
      return; 
    Iterator<SelectionKey> i = selectedKeys.iterator();
    while (true) {
      SelectionKey k = i.next();
      Object a = k.attachment();
      i.remove();
      if (a instanceof AbstractNioChannel) {
        processSelectedKey(k, (AbstractNioChannel)a);
      } else {
        NioTask<SelectableChannel> task = (NioTask<SelectableChannel>)a;
        processSelectedKey(k, task);
      } 
      if (!i.hasNext())
        break; 
      if (this.needsToSelectAgain) {
        selectAgain();
        selectedKeys = this.selector.selectedKeys();
        if (selectedKeys.isEmpty())
          break; 
        i = selectedKeys.iterator();
      } 
    } 
  }
  
  private void processSelectedKeysOptimized() {
    for (int i = 0; i < this.selectedKeys.size; i++) {
      SelectionKey k = this.selectedKeys.keys[i];
      this.selectedKeys.keys[i] = null;
      Object a = k.attachment();
      if (a instanceof AbstractNioChannel) {
        processSelectedKey(k, (AbstractNioChannel)a);
      } else {
        NioTask<SelectableChannel> task = (NioTask<SelectableChannel>)a;
        processSelectedKey(k, task);
      } 
      if (this.needsToSelectAgain) {
        this.selectedKeys.reset(i + 1);
        selectAgain();
        i = -1;
      } 
    } 
  }
  
  private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
    AbstractNioChannel.NioUnsafe unsafe = ch.unsafe();
    if (!k.isValid()) {
      NioEventLoop nioEventLoop;
      try {
        nioEventLoop = ch.eventLoop();
      } catch (Throwable ignored) {
        return;
      } 
      if (nioEventLoop != this || nioEventLoop == null)
        return; 
      unsafe.close(unsafe.voidPromise());
      return;
    } 
    try {
      int readyOps = k.readyOps();
      if ((readyOps & 0x8) != 0) {
        int ops = k.interestOps();
        ops &= 0xFFFFFFF7;
        k.interestOps(ops);
        unsafe.finishConnect();
      } 
      if ((readyOps & 0x4) != 0)
        ch.unsafe().forceFlush(); 
      if ((readyOps & 0x11) != 0 || readyOps == 0)
        unsafe.read(); 
    } catch (CancelledKeyException ignored) {
      EventLoop eventLoop;
      unsafe.close(unsafe.voidPromise());
    } 
  }
  
  private static void processSelectedKey(SelectionKey k, NioTask<SelectableChannel> task) {
    int state = 0;
    try {
      task.channelReady(k.channel(), k);
      state = 1;
    } catch (Exception e) {
      k.cancel();
      invokeChannelUnregistered(task, k, e);
      state = 2;
    } finally {
      switch (state) {
        case 0:
          k.cancel();
          invokeChannelUnregistered(task, k, (Throwable)null);
          break;
        case 1:
          if (!k.isValid())
            invokeChannelUnregistered(task, k, (Throwable)null); 
          break;
      } 
    } 
  }
  
  private void closeAll() {
    selectAgain();
    Set<SelectionKey> keys = this.selector.keys();
    Collection<AbstractNioChannel> channels = new ArrayList<AbstractNioChannel>(keys.size());
    for (SelectionKey k : keys) {
      Object a = k.attachment();
      if (a instanceof AbstractNioChannel) {
        channels.add((AbstractNioChannel)a);
        continue;
      } 
      k.cancel();
      NioTask<SelectableChannel> task = (NioTask<SelectableChannel>)a;
      invokeChannelUnregistered(task, k, (Throwable)null);
    } 
    for (AbstractNioChannel ch : channels)
      ch.unsafe().close(ch.unsafe().voidPromise()); 
  }
  
  private static void invokeChannelUnregistered(NioTask<SelectableChannel> task, SelectionKey k, Throwable cause) {
    try {
      task.channelUnregistered(k.channel(), cause);
    } catch (Exception e) {
      logger.warn("Unexpected exception while running NioTask.channelUnregistered()", e);
    } 
  }
  
  protected void wakeup(boolean inEventLoop) {
    if (!inEventLoop && this.wakenUp.compareAndSet(false, true))
      this.selector.wakeup(); 
  }
  
  Selector unwrappedSelector() {
    return this.unwrappedSelector;
  }
  
  int selectNow() throws IOException {
    try {
      return this.selector.selectNow();
    } finally {
      if (this.wakenUp.get())
        this.selector.wakeup(); 
    } 
  }
  
  private void select(boolean oldWakenUp) throws IOException {
    Selector selector = this.selector;
    try {
      int selectCnt = 0;
      long currentTimeNanos = System.nanoTime();
      long selectDeadLineNanos = currentTimeNanos + delayNanos(currentTimeNanos);
      while (true) {
        long timeoutMillis = (selectDeadLineNanos - currentTimeNanos + 500000L) / 1000000L;
        if (timeoutMillis <= 0L) {
          if (selectCnt == 0) {
            selector.selectNow();
            selectCnt = 1;
          } 
          break;
        } 
        if (hasTasks() && this.wakenUp.compareAndSet(false, true)) {
          selector.selectNow();
          selectCnt = 1;
          break;
        } 
        int selectedKeys = selector.select(timeoutMillis);
        selectCnt++;
        if (selectedKeys != 0 || oldWakenUp || this.wakenUp.get() || hasTasks() || hasScheduledTasks())
          break; 
        if (Thread.interrupted()) {
          if (logger.isDebugEnabled())
            logger.debug("Selector.select() returned prematurely because Thread.currentThread().interrupt() was called. Use NioEventLoop.shutdownGracefully() to shutdown the NioEventLoop."); 
          selectCnt = 1;
          break;
        } 
        long time = System.nanoTime();
        if (time - TimeUnit.MILLISECONDS.toNanos(timeoutMillis) >= currentTimeNanos) {
          selectCnt = 1;
        } else if (SELECTOR_AUTO_REBUILD_THRESHOLD > 0 && selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD) {
          logger.warn("Selector.select() returned prematurely {} times in a row; rebuilding Selector {}.", 
              
              Integer.valueOf(selectCnt), selector);
          rebuildSelector();
          selector = this.selector;
          selector.selectNow();
          selectCnt = 1;
          break;
        } 
        currentTimeNanos = time;
      } 
      if (selectCnt > 3 && 
        logger.isDebugEnabled())
        logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.", 
            Integer.valueOf(selectCnt - 1), selector); 
    } catch (CancelledKeyException e) {
      if (logger.isDebugEnabled())
        logger.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector {} - JDK bug?", selector, e); 
    } 
  }
  
  private void selectAgain() {
    this.needsToSelectAgain = false;
    try {
      this.selector.selectNow();
    } catch (Throwable t) {
      logger.warn("Failed to update SelectionKeys.", t);
    } 
  }
  
  protected void validateScheduled(long amount, TimeUnit unit) {
    long days = unit.toDays(amount);
    if (days > 1095L)
      throw new IllegalArgumentException("days: " + days + " (expected: < " + 1095L + ')'); 
  }
}

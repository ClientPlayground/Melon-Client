package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.util.Attribute;
import com.github.steveice10.netty.util.AttributeKey;
import com.github.steveice10.netty.util.DefaultAttributeMap;
import com.github.steveice10.netty.util.Recycler;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.ResourceLeakHint;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PromiseNotificationUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

abstract class AbstractChannelHandlerContext extends DefaultAttributeMap implements ChannelHandlerContext, ResourceLeakHint {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractChannelHandlerContext.class);
  
  volatile AbstractChannelHandlerContext next;
  
  volatile AbstractChannelHandlerContext prev;
  
  private static final AtomicIntegerFieldUpdater<AbstractChannelHandlerContext> HANDLER_STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AbstractChannelHandlerContext.class, "handlerState");
  
  private static final int ADD_PENDING = 1;
  
  private static final int ADD_COMPLETE = 2;
  
  private static final int REMOVE_COMPLETE = 3;
  
  private static final int INIT = 0;
  
  private final boolean inbound;
  
  private final boolean outbound;
  
  private final DefaultChannelPipeline pipeline;
  
  private final String name;
  
  private final boolean ordered;
  
  final EventExecutor executor;
  
  private ChannelFuture succeededFuture;
  
  private Runnable invokeChannelReadCompleteTask;
  
  private Runnable invokeReadTask;
  
  private Runnable invokeChannelWritableStateChangedTask;
  
  private Runnable invokeFlushTask;
  
  private volatile int handlerState = 0;
  
  AbstractChannelHandlerContext(DefaultChannelPipeline pipeline, EventExecutor executor, String name, boolean inbound, boolean outbound) {
    this.name = (String)ObjectUtil.checkNotNull(name, "name");
    this.pipeline = pipeline;
    this.executor = executor;
    this.inbound = inbound;
    this.outbound = outbound;
    this.ordered = (executor == null || executor instanceof com.github.steveice10.netty.util.concurrent.OrderedEventExecutor);
  }
  
  public Channel channel() {
    return this.pipeline.channel();
  }
  
  public ChannelPipeline pipeline() {
    return this.pipeline;
  }
  
  public ByteBufAllocator alloc() {
    return channel().config().getAllocator();
  }
  
  public EventExecutor executor() {
    if (this.executor == null)
      return (EventExecutor)channel().eventLoop(); 
    return this.executor;
  }
  
  public String name() {
    return this.name;
  }
  
  public ChannelHandlerContext fireChannelRegistered() {
    invokeChannelRegistered(findContextInbound());
    return this;
  }
  
  static void invokeChannelRegistered(final AbstractChannelHandlerContext next) {
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeChannelRegistered();
    } else {
      executor.execute(new Runnable() {
            public void run() {
              next.invokeChannelRegistered();
            }
          });
    } 
  }
  
  private void invokeChannelRegistered() {
    if (invokeHandler()) {
      try {
        ((ChannelInboundHandler)handler()).channelRegistered(this);
      } catch (Throwable t) {
        notifyHandlerException(t);
      } 
    } else {
      fireChannelRegistered();
    } 
  }
  
  public ChannelHandlerContext fireChannelUnregistered() {
    invokeChannelUnregistered(findContextInbound());
    return this;
  }
  
  static void invokeChannelUnregistered(final AbstractChannelHandlerContext next) {
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeChannelUnregistered();
    } else {
      executor.execute(new Runnable() {
            public void run() {
              next.invokeChannelUnregistered();
            }
          });
    } 
  }
  
  private void invokeChannelUnregistered() {
    if (invokeHandler()) {
      try {
        ((ChannelInboundHandler)handler()).channelUnregistered(this);
      } catch (Throwable t) {
        notifyHandlerException(t);
      } 
    } else {
      fireChannelUnregistered();
    } 
  }
  
  public ChannelHandlerContext fireChannelActive() {
    invokeChannelActive(findContextInbound());
    return this;
  }
  
  static void invokeChannelActive(final AbstractChannelHandlerContext next) {
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeChannelActive();
    } else {
      executor.execute(new Runnable() {
            public void run() {
              next.invokeChannelActive();
            }
          });
    } 
  }
  
  private void invokeChannelActive() {
    if (invokeHandler()) {
      try {
        ((ChannelInboundHandler)handler()).channelActive(this);
      } catch (Throwable t) {
        notifyHandlerException(t);
      } 
    } else {
      fireChannelActive();
    } 
  }
  
  public ChannelHandlerContext fireChannelInactive() {
    invokeChannelInactive(findContextInbound());
    return this;
  }
  
  static void invokeChannelInactive(final AbstractChannelHandlerContext next) {
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeChannelInactive();
    } else {
      executor.execute(new Runnable() {
            public void run() {
              next.invokeChannelInactive();
            }
          });
    } 
  }
  
  private void invokeChannelInactive() {
    if (invokeHandler()) {
      try {
        ((ChannelInboundHandler)handler()).channelInactive(this);
      } catch (Throwable t) {
        notifyHandlerException(t);
      } 
    } else {
      fireChannelInactive();
    } 
  }
  
  public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
    invokeExceptionCaught(this.next, cause);
    return this;
  }
  
  static void invokeExceptionCaught(final AbstractChannelHandlerContext next, final Throwable cause) {
    ObjectUtil.checkNotNull(cause, "cause");
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeExceptionCaught(cause);
    } else {
      try {
        executor.execute(new Runnable() {
              public void run() {
                next.invokeExceptionCaught(cause);
              }
            });
      } catch (Throwable t) {
        if (logger.isWarnEnabled()) {
          logger.warn("Failed to submit an exceptionCaught() event.", t);
          logger.warn("The exceptionCaught() event that was failed to submit was:", cause);
        } 
      } 
    } 
  }
  
  private void invokeExceptionCaught(Throwable cause) {
    if (invokeHandler()) {
      try {
        handler().exceptionCaught(this, cause);
      } catch (Throwable error) {
        if (logger.isDebugEnabled()) {
          logger.debug("An exception {}was thrown by a user handler's exceptionCaught() method while handling the following exception:", 
              
              ThrowableUtil.stackTraceToString(error), cause);
        } else if (logger.isWarnEnabled()) {
          logger.warn("An exception '{}' [enable DEBUG level for full stacktrace] was thrown by a user handler's exceptionCaught() method while handling the following exception:", error, cause);
        } 
      } 
    } else {
      fireExceptionCaught(cause);
    } 
  }
  
  public ChannelHandlerContext fireUserEventTriggered(Object event) {
    invokeUserEventTriggered(findContextInbound(), event);
    return this;
  }
  
  static void invokeUserEventTriggered(final AbstractChannelHandlerContext next, final Object event) {
    ObjectUtil.checkNotNull(event, "event");
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeUserEventTriggered(event);
    } else {
      executor.execute(new Runnable() {
            public void run() {
              next.invokeUserEventTriggered(event);
            }
          });
    } 
  }
  
  private void invokeUserEventTriggered(Object event) {
    if (invokeHandler()) {
      try {
        ((ChannelInboundHandler)handler()).userEventTriggered(this, event);
      } catch (Throwable t) {
        notifyHandlerException(t);
      } 
    } else {
      fireUserEventTriggered(event);
    } 
  }
  
  public ChannelHandlerContext fireChannelRead(Object msg) {
    invokeChannelRead(findContextInbound(), msg);
    return this;
  }
  
  static void invokeChannelRead(final AbstractChannelHandlerContext next, Object msg) {
    final Object m = next.pipeline.touch(ObjectUtil.checkNotNull(msg, "msg"), next);
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeChannelRead(m);
    } else {
      executor.execute(new Runnable() {
            public void run() {
              next.invokeChannelRead(m);
            }
          });
    } 
  }
  
  private void invokeChannelRead(Object msg) {
    if (invokeHandler()) {
      try {
        ((ChannelInboundHandler)handler()).channelRead(this, msg);
      } catch (Throwable t) {
        notifyHandlerException(t);
      } 
    } else {
      fireChannelRead(msg);
    } 
  }
  
  public ChannelHandlerContext fireChannelReadComplete() {
    invokeChannelReadComplete(findContextInbound());
    return this;
  }
  
  static void invokeChannelReadComplete(final AbstractChannelHandlerContext next) {
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeChannelReadComplete();
    } else {
      Runnable task = next.invokeChannelReadCompleteTask;
      if (task == null)
        next.invokeChannelReadCompleteTask = task = new Runnable() {
            public void run() {
              next.invokeChannelReadComplete();
            }
          }; 
      executor.execute(task);
    } 
  }
  
  private void invokeChannelReadComplete() {
    if (invokeHandler()) {
      try {
        ((ChannelInboundHandler)handler()).channelReadComplete(this);
      } catch (Throwable t) {
        notifyHandlerException(t);
      } 
    } else {
      fireChannelReadComplete();
    } 
  }
  
  public ChannelHandlerContext fireChannelWritabilityChanged() {
    invokeChannelWritabilityChanged(findContextInbound());
    return this;
  }
  
  static void invokeChannelWritabilityChanged(final AbstractChannelHandlerContext next) {
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeChannelWritabilityChanged();
    } else {
      Runnable task = next.invokeChannelWritableStateChangedTask;
      if (task == null)
        next.invokeChannelWritableStateChangedTask = task = new Runnable() {
            public void run() {
              next.invokeChannelWritabilityChanged();
            }
          }; 
      executor.execute(task);
    } 
  }
  
  private void invokeChannelWritabilityChanged() {
    if (invokeHandler()) {
      try {
        ((ChannelInboundHandler)handler()).channelWritabilityChanged(this);
      } catch (Throwable t) {
        notifyHandlerException(t);
      } 
    } else {
      fireChannelWritabilityChanged();
    } 
  }
  
  public ChannelFuture bind(SocketAddress localAddress) {
    return bind(localAddress, newPromise());
  }
  
  public ChannelFuture connect(SocketAddress remoteAddress) {
    return connect(remoteAddress, newPromise());
  }
  
  public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
    return connect(remoteAddress, localAddress, newPromise());
  }
  
  public ChannelFuture disconnect() {
    return disconnect(newPromise());
  }
  
  public ChannelFuture close() {
    return close(newPromise());
  }
  
  public ChannelFuture deregister() {
    return deregister(newPromise());
  }
  
  public ChannelFuture bind(final SocketAddress localAddress, final ChannelPromise promise) {
    if (localAddress == null)
      throw new NullPointerException("localAddress"); 
    if (isNotValidPromise(promise, false))
      return promise; 
    final AbstractChannelHandlerContext next = findContextOutbound();
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeBind(localAddress, promise);
    } else {
      safeExecute(executor, new Runnable() {
            public void run() {
              next.invokeBind(localAddress, promise);
            }
          }promise, null);
    } 
    return promise;
  }
  
  private void invokeBind(SocketAddress localAddress, ChannelPromise promise) {
    if (invokeHandler()) {
      try {
        ((ChannelOutboundHandler)handler()).bind(this, localAddress, promise);
      } catch (Throwable t) {
        notifyOutboundHandlerException(t, promise);
      } 
    } else {
      bind(localAddress, promise);
    } 
  }
  
  public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
    return connect(remoteAddress, null, promise);
  }
  
  public ChannelFuture connect(final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) {
    if (remoteAddress == null)
      throw new NullPointerException("remoteAddress"); 
    if (isNotValidPromise(promise, false))
      return promise; 
    final AbstractChannelHandlerContext next = findContextOutbound();
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeConnect(remoteAddress, localAddress, promise);
    } else {
      safeExecute(executor, new Runnable() {
            public void run() {
              next.invokeConnect(remoteAddress, localAddress, promise);
            }
          }promise, null);
    } 
    return promise;
  }
  
  private void invokeConnect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
    if (invokeHandler()) {
      try {
        ((ChannelOutboundHandler)handler()).connect(this, remoteAddress, localAddress, promise);
      } catch (Throwable t) {
        notifyOutboundHandlerException(t, promise);
      } 
    } else {
      connect(remoteAddress, localAddress, promise);
    } 
  }
  
  public ChannelFuture disconnect(final ChannelPromise promise) {
    if (isNotValidPromise(promise, false))
      return promise; 
    final AbstractChannelHandlerContext next = findContextOutbound();
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      if (!channel().metadata().hasDisconnect()) {
        next.invokeClose(promise);
      } else {
        next.invokeDisconnect(promise);
      } 
    } else {
      safeExecute(executor, new Runnable() {
            public void run() {
              if (!AbstractChannelHandlerContext.this.channel().metadata().hasDisconnect()) {
                next.invokeClose(promise);
              } else {
                next.invokeDisconnect(promise);
              } 
            }
          },  promise, null);
    } 
    return promise;
  }
  
  private void invokeDisconnect(ChannelPromise promise) {
    if (invokeHandler()) {
      try {
        ((ChannelOutboundHandler)handler()).disconnect(this, promise);
      } catch (Throwable t) {
        notifyOutboundHandlerException(t, promise);
      } 
    } else {
      disconnect(promise);
    } 
  }
  
  public ChannelFuture close(final ChannelPromise promise) {
    if (isNotValidPromise(promise, false))
      return promise; 
    final AbstractChannelHandlerContext next = findContextOutbound();
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeClose(promise);
    } else {
      safeExecute(executor, new Runnable() {
            public void run() {
              next.invokeClose(promise);
            }
          },  promise, null);
    } 
    return promise;
  }
  
  private void invokeClose(ChannelPromise promise) {
    if (invokeHandler()) {
      try {
        ((ChannelOutboundHandler)handler()).close(this, promise);
      } catch (Throwable t) {
        notifyOutboundHandlerException(t, promise);
      } 
    } else {
      close(promise);
    } 
  }
  
  public ChannelFuture deregister(final ChannelPromise promise) {
    if (isNotValidPromise(promise, false))
      return promise; 
    final AbstractChannelHandlerContext next = findContextOutbound();
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeDeregister(promise);
    } else {
      safeExecute(executor, new Runnable() {
            public void run() {
              next.invokeDeregister(promise);
            }
          },  promise, null);
    } 
    return promise;
  }
  
  private void invokeDeregister(ChannelPromise promise) {
    if (invokeHandler()) {
      try {
        ((ChannelOutboundHandler)handler()).deregister(this, promise);
      } catch (Throwable t) {
        notifyOutboundHandlerException(t, promise);
      } 
    } else {
      deregister(promise);
    } 
  }
  
  public ChannelHandlerContext read() {
    final AbstractChannelHandlerContext next = findContextOutbound();
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeRead();
    } else {
      Runnable task = next.invokeReadTask;
      if (task == null)
        next.invokeReadTask = task = new Runnable() {
            public void run() {
              next.invokeRead();
            }
          }; 
      executor.execute(task);
    } 
    return this;
  }
  
  private void invokeRead() {
    if (invokeHandler()) {
      try {
        ((ChannelOutboundHandler)handler()).read(this);
      } catch (Throwable t) {
        notifyHandlerException(t);
      } 
    } else {
      read();
    } 
  }
  
  public ChannelFuture write(Object msg) {
    return write(msg, newPromise());
  }
  
  public ChannelFuture write(Object msg, ChannelPromise promise) {
    if (msg == null)
      throw new NullPointerException("msg"); 
    try {
      if (isNotValidPromise(promise, true)) {
        ReferenceCountUtil.release(msg);
        return promise;
      } 
    } catch (RuntimeException e) {
      ReferenceCountUtil.release(msg);
      throw e;
    } 
    write(msg, false, promise);
    return promise;
  }
  
  private void invokeWrite(Object msg, ChannelPromise promise) {
    if (invokeHandler()) {
      invokeWrite0(msg, promise);
    } else {
      write(msg, promise);
    } 
  }
  
  private void invokeWrite0(Object msg, ChannelPromise promise) {
    try {
      ((ChannelOutboundHandler)handler()).write(this, msg, promise);
    } catch (Throwable t) {
      notifyOutboundHandlerException(t, promise);
    } 
  }
  
  public ChannelHandlerContext flush() {
    final AbstractChannelHandlerContext next = findContextOutbound();
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      next.invokeFlush();
    } else {
      Runnable task = next.invokeFlushTask;
      if (task == null)
        next.invokeFlushTask = task = new Runnable() {
            public void run() {
              next.invokeFlush();
            }
          }; 
      safeExecute(executor, task, channel().voidPromise(), null);
    } 
    return this;
  }
  
  private void invokeFlush() {
    if (invokeHandler()) {
      invokeFlush0();
    } else {
      flush();
    } 
  }
  
  private void invokeFlush0() {
    try {
      ((ChannelOutboundHandler)handler()).flush(this);
    } catch (Throwable t) {
      notifyHandlerException(t);
    } 
  }
  
  public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
    if (msg == null)
      throw new NullPointerException("msg"); 
    if (isNotValidPromise(promise, true)) {
      ReferenceCountUtil.release(msg);
      return promise;
    } 
    write(msg, true, promise);
    return promise;
  }
  
  private void invokeWriteAndFlush(Object msg, ChannelPromise promise) {
    if (invokeHandler()) {
      invokeWrite0(msg, promise);
      invokeFlush0();
    } else {
      writeAndFlush(msg, promise);
    } 
  }
  
  private void write(Object msg, boolean flush, ChannelPromise promise) {
    AbstractChannelHandlerContext next = findContextOutbound();
    Object m = this.pipeline.touch(msg, next);
    EventExecutor executor = next.executor();
    if (executor.inEventLoop()) {
      if (flush) {
        next.invokeWriteAndFlush(m, promise);
      } else {
        next.invokeWrite(m, promise);
      } 
    } else {
      AbstractWriteTask task;
      if (flush) {
        task = WriteAndFlushTask.newInstance(next, m, promise);
      } else {
        task = WriteTask.newInstance(next, m, promise);
      } 
      safeExecute(executor, task, promise, m);
    } 
  }
  
  public ChannelFuture writeAndFlush(Object msg) {
    return writeAndFlush(msg, newPromise());
  }
  
  private static void notifyOutboundHandlerException(Throwable cause, ChannelPromise promise) {
    PromiseNotificationUtil.tryFailure(promise, cause, (promise instanceof VoidChannelPromise) ? null : logger);
  }
  
  private void notifyHandlerException(Throwable cause) {
    if (inExceptionCaught(cause)) {
      if (logger.isWarnEnabled())
        logger.warn("An exception was thrown by a user handler while handling an exceptionCaught event", cause); 
      return;
    } 
    invokeExceptionCaught(cause);
  }
  
  private static boolean inExceptionCaught(Throwable cause) {
    do {
      StackTraceElement[] trace = cause.getStackTrace();
      if (trace != null)
        for (StackTraceElement t : trace) {
          if (t == null)
            break; 
          if ("exceptionCaught".equals(t.getMethodName()))
            return true; 
        }  
      cause = cause.getCause();
    } while (cause != null);
    return false;
  }
  
  public ChannelPromise newPromise() {
    return new DefaultChannelPromise(channel(), executor());
  }
  
  public ChannelProgressivePromise newProgressivePromise() {
    return new DefaultChannelProgressivePromise(channel(), executor());
  }
  
  public ChannelFuture newSucceededFuture() {
    ChannelFuture succeededFuture = this.succeededFuture;
    if (succeededFuture == null)
      this.succeededFuture = succeededFuture = new SucceededChannelFuture(channel(), executor()); 
    return succeededFuture;
  }
  
  public ChannelFuture newFailedFuture(Throwable cause) {
    return new FailedChannelFuture(channel(), executor(), cause);
  }
  
  private boolean isNotValidPromise(ChannelPromise promise, boolean allowVoidPromise) {
    if (promise == null)
      throw new NullPointerException("promise"); 
    if (promise.isDone()) {
      if (promise.isCancelled())
        return true; 
      throw new IllegalArgumentException("promise already done: " + promise);
    } 
    if (promise.channel() != channel())
      throw new IllegalArgumentException(String.format("promise.channel does not match: %s (expected: %s)", new Object[] { promise
              .channel(), channel() })); 
    if (promise.getClass() == DefaultChannelPromise.class)
      return false; 
    if (!allowVoidPromise && promise instanceof VoidChannelPromise)
      throw new IllegalArgumentException(
          StringUtil.simpleClassName(VoidChannelPromise.class) + " not allowed for this operation"); 
    if (promise instanceof AbstractChannel.CloseFuture)
      throw new IllegalArgumentException(
          StringUtil.simpleClassName(AbstractChannel.CloseFuture.class) + " not allowed in a pipeline"); 
    return false;
  }
  
  private AbstractChannelHandlerContext findContextInbound() {
    AbstractChannelHandlerContext ctx = this;
    while (true) {
      ctx = ctx.next;
      if (ctx.inbound)
        return ctx; 
    } 
  }
  
  private AbstractChannelHandlerContext findContextOutbound() {
    AbstractChannelHandlerContext ctx = this;
    while (true) {
      ctx = ctx.prev;
      if (ctx.outbound)
        return ctx; 
    } 
  }
  
  public ChannelPromise voidPromise() {
    return channel().voidPromise();
  }
  
  final void setRemoved() {
    this.handlerState = 3;
  }
  
  final void setAddComplete() {
    int oldState;
    do {
      oldState = this.handlerState;
    } while (oldState != 3 && !HANDLER_STATE_UPDATER.compareAndSet(this, oldState, 2));
  }
  
  final void setAddPending() {
    boolean updated = HANDLER_STATE_UPDATER.compareAndSet(this, 0, 1);
    assert updated;
  }
  
  private boolean invokeHandler() {
    int handlerState = this.handlerState;
    return (handlerState == 2 || (!this.ordered && handlerState == 1));
  }
  
  public boolean isRemoved() {
    return (this.handlerState == 3);
  }
  
  public <T> Attribute<T> attr(AttributeKey<T> key) {
    return channel().attr(key);
  }
  
  public <T> boolean hasAttr(AttributeKey<T> key) {
    return channel().hasAttr(key);
  }
  
  private static void safeExecute(EventExecutor executor, Runnable runnable, ChannelPromise promise, Object msg) {
    try {
      executor.execute(runnable);
    } catch (Throwable cause) {
      try {
        promise.setFailure(cause);
      } finally {
        if (msg != null)
          ReferenceCountUtil.release(msg); 
      } 
    } 
  }
  
  public String toHintString() {
    return '\'' + this.name + "' will handle the message from this point.";
  }
  
  public String toString() {
    return StringUtil.simpleClassName(ChannelHandlerContext.class) + '(' + this.name + ", " + channel() + ')';
  }
  
  static abstract class AbstractWriteTask implements Runnable {
    private static final boolean ESTIMATE_TASK_SIZE_ON_SUBMIT = SystemPropertyUtil.getBoolean("com.github.steveice10.netty.transport.estimateSizeOnSubmit", true);
    
    private static final int WRITE_TASK_OVERHEAD = SystemPropertyUtil.getInt("com.github.steveice10.netty.transport.writeTaskSizeOverhead", 48);
    
    private final Recycler.Handle<AbstractWriteTask> handle;
    
    private AbstractChannelHandlerContext ctx;
    
    private Object msg;
    
    private ChannelPromise promise;
    
    private int size;
    
    private AbstractWriteTask(Recycler.Handle<? extends AbstractWriteTask> handle) {
      this.handle = (Recycler.Handle)handle;
    }
    
    protected static void init(AbstractWriteTask task, AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
      task.ctx = ctx;
      task.msg = msg;
      task.promise = promise;
      if (ESTIMATE_TASK_SIZE_ON_SUBMIT) {
        task.size = ctx.pipeline.estimatorHandle().size(msg) + WRITE_TASK_OVERHEAD;
        ctx.pipeline.incrementPendingOutboundBytes(task.size);
      } else {
        task.size = 0;
      } 
    }
    
    public final void run() {
      try {
        if (ESTIMATE_TASK_SIZE_ON_SUBMIT)
          this.ctx.pipeline.decrementPendingOutboundBytes(this.size); 
        write(this.ctx, this.msg, this.promise);
      } finally {
        this.ctx = null;
        this.msg = null;
        this.promise = null;
        this.handle.recycle(this);
      } 
    }
    
    protected void write(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
      ctx.invokeWrite(msg, promise);
    }
  }
  
  static final class WriteTask extends AbstractWriteTask implements SingleThreadEventLoop.NonWakeupRunnable {
    private static final Recycler<WriteTask> RECYCLER = new Recycler<WriteTask>() {
        protected AbstractChannelHandlerContext.WriteTask newObject(Recycler.Handle<AbstractChannelHandlerContext.WriteTask> handle) {
          return new AbstractChannelHandlerContext.WriteTask(handle);
        }
      };
    
    private static WriteTask newInstance(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
      WriteTask task = (WriteTask)RECYCLER.get();
      init(task, ctx, msg, promise);
      return task;
    }
    
    private WriteTask(Recycler.Handle<WriteTask> handle) {
      super(handle);
    }
  }
  
  static final class WriteAndFlushTask extends AbstractWriteTask {
    private static final Recycler<WriteAndFlushTask> RECYCLER = new Recycler<WriteAndFlushTask>() {
        protected AbstractChannelHandlerContext.WriteAndFlushTask newObject(Recycler.Handle<AbstractChannelHandlerContext.WriteAndFlushTask> handle) {
          return new AbstractChannelHandlerContext.WriteAndFlushTask(handle);
        }
      };
    
    private static WriteAndFlushTask newInstance(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
      WriteAndFlushTask task = (WriteAndFlushTask)RECYCLER.get();
      init(task, ctx, msg, promise);
      return task;
    }
    
    private WriteAndFlushTask(Recycler.Handle<WriteAndFlushTask> handle) {
      super(handle);
    }
    
    public void write(AbstractChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
      super.write(ctx, msg, promise);
      ctx.invokeFlush();
    }
  }
}

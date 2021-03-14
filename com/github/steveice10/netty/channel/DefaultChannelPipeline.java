package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.ResourceLeakDetector;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.EventExecutorGroup;
import com.github.steveice10.netty.util.concurrent.FastThreadLocal;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class DefaultChannelPipeline implements ChannelPipeline {
  static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelPipeline.class);
  
  private static final String HEAD_NAME = generateName0(HeadContext.class);
  
  private static final String TAIL_NAME = generateName0(TailContext.class);
  
  private static final FastThreadLocal<Map<Class<?>, String>> nameCaches = new FastThreadLocal<Map<Class<?>, String>>() {
      protected Map<Class<?>, String> initialValue() throws Exception {
        return new WeakHashMap<Class<?>, String>();
      }
    };
  
  private static final AtomicReferenceFieldUpdater<DefaultChannelPipeline, MessageSizeEstimator.Handle> ESTIMATOR = AtomicReferenceFieldUpdater.newUpdater(DefaultChannelPipeline.class, MessageSizeEstimator.Handle.class, "estimatorHandle");
  
  final AbstractChannelHandlerContext head;
  
  final AbstractChannelHandlerContext tail;
  
  private final Channel channel;
  
  private final ChannelFuture succeededFuture;
  
  private final VoidChannelPromise voidPromise;
  
  private final boolean touch = ResourceLeakDetector.isEnabled();
  
  private Map<EventExecutorGroup, EventExecutor> childExecutors;
  
  private volatile MessageSizeEstimator.Handle estimatorHandle;
  
  private boolean firstRegistration = true;
  
  private PendingHandlerCallback pendingHandlerCallbackHead;
  
  private boolean registered;
  
  protected DefaultChannelPipeline(Channel channel) {
    this.channel = (Channel)ObjectUtil.checkNotNull(channel, "channel");
    this.succeededFuture = new SucceededChannelFuture(channel, null);
    this.voidPromise = new VoidChannelPromise(channel, true);
    this.tail = new TailContext(this);
    this.head = new HeadContext(this);
    this.head.next = this.tail;
    this.tail.prev = this.head;
  }
  
  final MessageSizeEstimator.Handle estimatorHandle() {
    MessageSizeEstimator.Handle handle = this.estimatorHandle;
    if (handle == null) {
      handle = this.channel.config().getMessageSizeEstimator().newHandle();
      if (!ESTIMATOR.compareAndSet(this, null, handle))
        handle = this.estimatorHandle; 
    } 
    return handle;
  }
  
  final Object touch(Object msg, AbstractChannelHandlerContext next) {
    return this.touch ? ReferenceCountUtil.touch(msg, next) : msg;
  }
  
  private AbstractChannelHandlerContext newContext(EventExecutorGroup group, String name, ChannelHandler handler) {
    return new DefaultChannelHandlerContext(this, childExecutor(group), name, handler);
  }
  
  private EventExecutor childExecutor(EventExecutorGroup group) {
    if (group == null)
      return null; 
    Boolean pinEventExecutor = this.channel.config().<Boolean>getOption(ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP);
    if (pinEventExecutor != null && !pinEventExecutor.booleanValue())
      return group.next(); 
    Map<EventExecutorGroup, EventExecutor> childExecutors = this.childExecutors;
    if (childExecutors == null)
      childExecutors = this.childExecutors = new IdentityHashMap<EventExecutorGroup, EventExecutor>(4); 
    EventExecutor childExecutor = childExecutors.get(group);
    if (childExecutor == null) {
      childExecutor = group.next();
      childExecutors.put(group, childExecutor);
    } 
    return childExecutor;
  }
  
  public final Channel channel() {
    return this.channel;
  }
  
  public final ChannelPipeline addFirst(String name, ChannelHandler handler) {
    return addFirst(null, name, handler);
  }
  
  public final ChannelPipeline addFirst(EventExecutorGroup group, String name, ChannelHandler handler) {
    final AbstractChannelHandlerContext newCtx;
    synchronized (this) {
      checkMultiplicity(handler);
      name = filterName(name, handler);
      newCtx = newContext(group, name, handler);
      addFirst0(newCtx);
      if (!this.registered) {
        newCtx.setAddPending();
        callHandlerCallbackLater(newCtx, true);
        return this;
      } 
      EventExecutor executor = newCtx.executor();
      if (!executor.inEventLoop()) {
        newCtx.setAddPending();
        executor.execute(new Runnable() {
              public void run() {
                DefaultChannelPipeline.this.callHandlerAdded0(newCtx);
              }
            });
        return this;
      } 
    } 
    callHandlerAdded0(newCtx);
    return this;
  }
  
  private void addFirst0(AbstractChannelHandlerContext newCtx) {
    AbstractChannelHandlerContext nextCtx = this.head.next;
    newCtx.prev = this.head;
    newCtx.next = nextCtx;
    this.head.next = newCtx;
    nextCtx.prev = newCtx;
  }
  
  public final ChannelPipeline addLast(String name, ChannelHandler handler) {
    return addLast(null, name, handler);
  }
  
  public final ChannelPipeline addLast(EventExecutorGroup group, String name, ChannelHandler handler) {
    final AbstractChannelHandlerContext newCtx;
    synchronized (this) {
      checkMultiplicity(handler);
      newCtx = newContext(group, filterName(name, handler), handler);
      addLast0(newCtx);
      if (!this.registered) {
        newCtx.setAddPending();
        callHandlerCallbackLater(newCtx, true);
        return this;
      } 
      EventExecutor executor = newCtx.executor();
      if (!executor.inEventLoop()) {
        newCtx.setAddPending();
        executor.execute(new Runnable() {
              public void run() {
                DefaultChannelPipeline.this.callHandlerAdded0(newCtx);
              }
            });
        return this;
      } 
    } 
    callHandlerAdded0(newCtx);
    return this;
  }
  
  private void addLast0(AbstractChannelHandlerContext newCtx) {
    AbstractChannelHandlerContext prev = this.tail.prev;
    newCtx.prev = prev;
    newCtx.next = this.tail;
    prev.next = newCtx;
    this.tail.prev = newCtx;
  }
  
  public final ChannelPipeline addBefore(String baseName, String name, ChannelHandler handler) {
    return addBefore(null, baseName, name, handler);
  }
  
  public final ChannelPipeline addBefore(EventExecutorGroup group, String baseName, String name, ChannelHandler handler) {
    final AbstractChannelHandlerContext newCtx;
    synchronized (this) {
      checkMultiplicity(handler);
      name = filterName(name, handler);
      AbstractChannelHandlerContext ctx = getContextOrDie(baseName);
      newCtx = newContext(group, name, handler);
      addBefore0(ctx, newCtx);
      if (!this.registered) {
        newCtx.setAddPending();
        callHandlerCallbackLater(newCtx, true);
        return this;
      } 
      EventExecutor executor = newCtx.executor();
      if (!executor.inEventLoop()) {
        newCtx.setAddPending();
        executor.execute(new Runnable() {
              public void run() {
                DefaultChannelPipeline.this.callHandlerAdded0(newCtx);
              }
            });
        return this;
      } 
    } 
    callHandlerAdded0(newCtx);
    return this;
  }
  
  private static void addBefore0(AbstractChannelHandlerContext ctx, AbstractChannelHandlerContext newCtx) {
    newCtx.prev = ctx.prev;
    newCtx.next = ctx;
    ctx.prev.next = newCtx;
    ctx.prev = newCtx;
  }
  
  private String filterName(String name, ChannelHandler handler) {
    if (name == null)
      return generateName(handler); 
    checkDuplicateName(name);
    return name;
  }
  
  public final ChannelPipeline addAfter(String baseName, String name, ChannelHandler handler) {
    return addAfter(null, baseName, name, handler);
  }
  
  public final ChannelPipeline addAfter(EventExecutorGroup group, String baseName, String name, ChannelHandler handler) {
    final AbstractChannelHandlerContext newCtx;
    synchronized (this) {
      checkMultiplicity(handler);
      name = filterName(name, handler);
      AbstractChannelHandlerContext ctx = getContextOrDie(baseName);
      newCtx = newContext(group, name, handler);
      addAfter0(ctx, newCtx);
      if (!this.registered) {
        newCtx.setAddPending();
        callHandlerCallbackLater(newCtx, true);
        return this;
      } 
      EventExecutor executor = newCtx.executor();
      if (!executor.inEventLoop()) {
        newCtx.setAddPending();
        executor.execute(new Runnable() {
              public void run() {
                DefaultChannelPipeline.this.callHandlerAdded0(newCtx);
              }
            });
        return this;
      } 
    } 
    callHandlerAdded0(newCtx);
    return this;
  }
  
  private static void addAfter0(AbstractChannelHandlerContext ctx, AbstractChannelHandlerContext newCtx) {
    newCtx.prev = ctx;
    newCtx.next = ctx.next;
    ctx.next.prev = newCtx;
    ctx.next = newCtx;
  }
  
  public final ChannelPipeline addFirst(ChannelHandler handler) {
    return addFirst((String)null, handler);
  }
  
  public final ChannelPipeline addFirst(ChannelHandler... handlers) {
    return addFirst((EventExecutorGroup)null, handlers);
  }
  
  public final ChannelPipeline addFirst(EventExecutorGroup executor, ChannelHandler... handlers) {
    if (handlers == null)
      throw new NullPointerException("handlers"); 
    if (handlers.length == 0 || handlers[0] == null)
      return this; 
    int size;
    for (size = 1; size < handlers.length && 
      handlers[size] != null; size++);
    for (int i = size - 1; i >= 0; i--) {
      ChannelHandler h = handlers[i];
      addFirst(executor, null, h);
    } 
    return this;
  }
  
  public final ChannelPipeline addLast(ChannelHandler handler) {
    return addLast((String)null, handler);
  }
  
  public final ChannelPipeline addLast(ChannelHandler... handlers) {
    return addLast((EventExecutorGroup)null, handlers);
  }
  
  public final ChannelPipeline addLast(EventExecutorGroup executor, ChannelHandler... handlers) {
    if (handlers == null)
      throw new NullPointerException("handlers"); 
    for (ChannelHandler h : handlers) {
      if (h == null)
        break; 
      addLast(executor, null, h);
    } 
    return this;
  }
  
  private String generateName(ChannelHandler handler) {
    Map<Class<?>, String> cache = (Map<Class<?>, String>)nameCaches.get();
    Class<?> handlerType = handler.getClass();
    String name = cache.get(handlerType);
    if (name == null) {
      name = generateName0(handlerType);
      cache.put(handlerType, name);
    } 
    if (context0(name) != null) {
      String baseName = name.substring(0, name.length() - 1);
      for (int i = 1;; i++) {
        String newName = baseName + i;
        if (context0(newName) == null) {
          name = newName;
          break;
        } 
      } 
    } 
    return name;
  }
  
  private static String generateName0(Class<?> handlerType) {
    return StringUtil.simpleClassName(handlerType) + "#0";
  }
  
  public final ChannelPipeline remove(ChannelHandler handler) {
    remove(getContextOrDie(handler));
    return this;
  }
  
  public final ChannelHandler remove(String name) {
    return remove(getContextOrDie(name)).handler();
  }
  
  public final <T extends ChannelHandler> T remove(Class<T> handlerType) {
    return (T)remove(getContextOrDie(handlerType)).handler();
  }
  
  public final <T extends ChannelHandler> T removeIfExists(String name) {
    return removeIfExists(context(name));
  }
  
  public final <T extends ChannelHandler> T removeIfExists(Class<T> handlerType) {
    return removeIfExists(context(handlerType));
  }
  
  public final <T extends ChannelHandler> T removeIfExists(ChannelHandler handler) {
    return removeIfExists(context(handler));
  }
  
  private <T extends ChannelHandler> T removeIfExists(ChannelHandlerContext ctx) {
    if (ctx == null)
      return null; 
    return (T)remove((AbstractChannelHandlerContext)ctx).handler();
  }
  
  private AbstractChannelHandlerContext remove(final AbstractChannelHandlerContext ctx) {
    assert ctx != this.head && ctx != this.tail;
    synchronized (this) {
      remove0(ctx);
      if (!this.registered) {
        callHandlerCallbackLater(ctx, false);
        return ctx;
      } 
      EventExecutor executor = ctx.executor();
      if (!executor.inEventLoop()) {
        executor.execute(new Runnable() {
              public void run() {
                DefaultChannelPipeline.this.callHandlerRemoved0(ctx);
              }
            });
        return ctx;
      } 
    } 
    callHandlerRemoved0(ctx);
    return ctx;
  }
  
  private static void remove0(AbstractChannelHandlerContext ctx) {
    AbstractChannelHandlerContext prev = ctx.prev;
    AbstractChannelHandlerContext next = ctx.next;
    prev.next = next;
    next.prev = prev;
  }
  
  public final ChannelHandler removeFirst() {
    if (this.head.next == this.tail)
      throw new NoSuchElementException(); 
    return remove(this.head.next).handler();
  }
  
  public final ChannelHandler removeLast() {
    if (this.head.next == this.tail)
      throw new NoSuchElementException(); 
    return remove(this.tail.prev).handler();
  }
  
  public final ChannelPipeline replace(ChannelHandler oldHandler, String newName, ChannelHandler newHandler) {
    replace(getContextOrDie(oldHandler), newName, newHandler);
    return this;
  }
  
  public final ChannelHandler replace(String oldName, String newName, ChannelHandler newHandler) {
    return replace(getContextOrDie(oldName), newName, newHandler);
  }
  
  public final <T extends ChannelHandler> T replace(Class<T> oldHandlerType, String newName, ChannelHandler newHandler) {
    return (T)replace(getContextOrDie(oldHandlerType), newName, newHandler);
  }
  
  private ChannelHandler replace(final AbstractChannelHandlerContext ctx, String newName, ChannelHandler newHandler) {
    final AbstractChannelHandlerContext newCtx;
    assert ctx != this.head && ctx != this.tail;
    synchronized (this) {
      checkMultiplicity(newHandler);
      if (newName == null) {
        newName = generateName(newHandler);
      } else {
        boolean sameName = ctx.name().equals(newName);
        if (!sameName)
          checkDuplicateName(newName); 
      } 
      newCtx = newContext((EventExecutorGroup)ctx.executor, newName, newHandler);
      replace0(ctx, newCtx);
      if (!this.registered) {
        callHandlerCallbackLater(newCtx, true);
        callHandlerCallbackLater(ctx, false);
        return ctx.handler();
      } 
      EventExecutor executor = ctx.executor();
      if (!executor.inEventLoop()) {
        executor.execute(new Runnable() {
              public void run() {
                DefaultChannelPipeline.this.callHandlerAdded0(newCtx);
                DefaultChannelPipeline.this.callHandlerRemoved0(ctx);
              }
            });
        return ctx.handler();
      } 
    } 
    callHandlerAdded0(newCtx);
    callHandlerRemoved0(ctx);
    return ctx.handler();
  }
  
  private static void replace0(AbstractChannelHandlerContext oldCtx, AbstractChannelHandlerContext newCtx) {
    AbstractChannelHandlerContext prev = oldCtx.prev;
    AbstractChannelHandlerContext next = oldCtx.next;
    newCtx.prev = prev;
    newCtx.next = next;
    prev.next = newCtx;
    next.prev = newCtx;
    oldCtx.prev = newCtx;
    oldCtx.next = newCtx;
  }
  
  private static void checkMultiplicity(ChannelHandler handler) {
    if (handler instanceof ChannelHandlerAdapter) {
      ChannelHandlerAdapter h = (ChannelHandlerAdapter)handler;
      if (!h.isSharable() && h.added)
        throw new ChannelPipelineException(h
            .getClass().getName() + " is not a @Sharable handler, so can't be added or removed multiple times."); 
      h.added = true;
    } 
  }
  
  private void callHandlerAdded0(AbstractChannelHandlerContext ctx) {
    try {
      ctx.setAddComplete();
      ctx.handler().handlerAdded(ctx);
    } catch (Throwable t) {
      boolean removed = false;
      try {
        remove0(ctx);
        try {
          ctx.handler().handlerRemoved(ctx);
        } finally {
          ctx.setRemoved();
        } 
        removed = true;
      } catch (Throwable t2) {
        if (logger.isWarnEnabled())
          logger.warn("Failed to remove a handler: " + ctx.name(), t2); 
      } 
      if (removed) {
        fireExceptionCaught(new ChannelPipelineException(ctx
              .handler().getClass().getName() + ".handlerAdded() has thrown an exception; removed.", t));
      } else {
        fireExceptionCaught(new ChannelPipelineException(ctx
              .handler().getClass().getName() + ".handlerAdded() has thrown an exception; also failed to remove.", t));
      } 
    } 
  }
  
  private void callHandlerRemoved0(AbstractChannelHandlerContext ctx) {
    try {
      try {
        ctx.handler().handlerRemoved(ctx);
      } finally {
        ctx.setRemoved();
      } 
    } catch (Throwable t) {
      fireExceptionCaught(new ChannelPipelineException(ctx
            .handler().getClass().getName() + ".handlerRemoved() has thrown an exception.", t));
    } 
  }
  
  final void invokeHandlerAddedIfNeeded() {
    assert this.channel.eventLoop().inEventLoop();
    if (this.firstRegistration) {
      this.firstRegistration = false;
      callHandlerAddedForAllHandlers();
    } 
  }
  
  public final ChannelHandler first() {
    ChannelHandlerContext first = firstContext();
    if (first == null)
      return null; 
    return first.handler();
  }
  
  public final ChannelHandlerContext firstContext() {
    AbstractChannelHandlerContext first = this.head.next;
    if (first == this.tail)
      return null; 
    return this.head.next;
  }
  
  public final ChannelHandler last() {
    AbstractChannelHandlerContext last = this.tail.prev;
    if (last == this.head)
      return null; 
    return last.handler();
  }
  
  public final ChannelHandlerContext lastContext() {
    AbstractChannelHandlerContext last = this.tail.prev;
    if (last == this.head)
      return null; 
    return last;
  }
  
  public final ChannelHandler get(String name) {
    ChannelHandlerContext ctx = context(name);
    if (ctx == null)
      return null; 
    return ctx.handler();
  }
  
  public final <T extends ChannelHandler> T get(Class<T> handlerType) {
    ChannelHandlerContext ctx = context(handlerType);
    if (ctx == null)
      return null; 
    return (T)ctx.handler();
  }
  
  public final ChannelHandlerContext context(String name) {
    if (name == null)
      throw new NullPointerException("name"); 
    return context0(name);
  }
  
  public final ChannelHandlerContext context(ChannelHandler handler) {
    if (handler == null)
      throw new NullPointerException("handler"); 
    AbstractChannelHandlerContext ctx = this.head.next;
    while (true) {
      if (ctx == null)
        return null; 
      if (ctx.handler() == handler)
        return ctx; 
      ctx = ctx.next;
    } 
  }
  
  public final ChannelHandlerContext context(Class<? extends ChannelHandler> handlerType) {
    if (handlerType == null)
      throw new NullPointerException("handlerType"); 
    AbstractChannelHandlerContext ctx = this.head.next;
    while (true) {
      if (ctx == null)
        return null; 
      if (handlerType.isAssignableFrom(ctx.handler().getClass()))
        return ctx; 
      ctx = ctx.next;
    } 
  }
  
  public final List<String> names() {
    List<String> list = new ArrayList<String>();
    AbstractChannelHandlerContext ctx = this.head.next;
    while (true) {
      if (ctx == null)
        return list; 
      list.add(ctx.name());
      ctx = ctx.next;
    } 
  }
  
  public final Map<String, ChannelHandler> toMap() {
    Map<String, ChannelHandler> map = new LinkedHashMap<String, ChannelHandler>();
    AbstractChannelHandlerContext ctx = this.head.next;
    while (true) {
      if (ctx == this.tail)
        return map; 
      map.put(ctx.name(), ctx.handler());
      ctx = ctx.next;
    } 
  }
  
  public final Iterator<Map.Entry<String, ChannelHandler>> iterator() {
    return toMap().entrySet().iterator();
  }
  
  public final String toString() {
    StringBuilder buf = (new StringBuilder()).append(StringUtil.simpleClassName(this)).append('{');
    AbstractChannelHandlerContext ctx = this.head.next;
    while (ctx != this.tail) {
      buf.append('(')
        .append(ctx.name())
        .append(" = ")
        .append(ctx.handler().getClass().getName())
        .append(')');
      ctx = ctx.next;
      if (ctx == this.tail)
        break; 
      buf.append(", ");
    } 
    buf.append('}');
    return buf.toString();
  }
  
  public final ChannelPipeline fireChannelRegistered() {
    AbstractChannelHandlerContext.invokeChannelRegistered(this.head);
    return this;
  }
  
  public final ChannelPipeline fireChannelUnregistered() {
    AbstractChannelHandlerContext.invokeChannelUnregistered(this.head);
    return this;
  }
  
  private synchronized void destroy() {
    destroyUp(this.head.next, false);
  }
  
  private void destroyUp(AbstractChannelHandlerContext ctx, boolean inEventLoop) {
    Thread currentThread = Thread.currentThread();
    AbstractChannelHandlerContext tail = this.tail;
    while (true) {
      if (ctx == tail) {
        destroyDown(currentThread, tail.prev, inEventLoop);
        break;
      } 
      EventExecutor executor = ctx.executor();
      if (!inEventLoop && !executor.inEventLoop(currentThread)) {
        final AbstractChannelHandlerContext finalCtx = ctx;
        executor.execute(new Runnable() {
              public void run() {
                DefaultChannelPipeline.this.destroyUp(finalCtx, true);
              }
            });
        break;
      } 
      ctx = ctx.next;
      inEventLoop = false;
    } 
  }
  
  private void destroyDown(Thread currentThread, AbstractChannelHandlerContext ctx, boolean inEventLoop) {
    AbstractChannelHandlerContext head = this.head;
    while (ctx != head) {
      EventExecutor executor = ctx.executor();
      if (inEventLoop || executor.inEventLoop(currentThread)) {
        synchronized (this) {
          remove0(ctx);
        } 
        callHandlerRemoved0(ctx);
      } else {
        final AbstractChannelHandlerContext finalCtx = ctx;
        executor.execute(new Runnable() {
              public void run() {
                DefaultChannelPipeline.this.destroyDown(Thread.currentThread(), finalCtx, true);
              }
            });
        break;
      } 
      ctx = ctx.prev;
      inEventLoop = false;
    } 
  }
  
  public final ChannelPipeline fireChannelActive() {
    AbstractChannelHandlerContext.invokeChannelActive(this.head);
    return this;
  }
  
  public final ChannelPipeline fireChannelInactive() {
    AbstractChannelHandlerContext.invokeChannelInactive(this.head);
    return this;
  }
  
  public final ChannelPipeline fireExceptionCaught(Throwable cause) {
    AbstractChannelHandlerContext.invokeExceptionCaught(this.head, cause);
    return this;
  }
  
  public final ChannelPipeline fireUserEventTriggered(Object event) {
    AbstractChannelHandlerContext.invokeUserEventTriggered(this.head, event);
    return this;
  }
  
  public final ChannelPipeline fireChannelRead(Object msg) {
    AbstractChannelHandlerContext.invokeChannelRead(this.head, msg);
    return this;
  }
  
  public final ChannelPipeline fireChannelReadComplete() {
    AbstractChannelHandlerContext.invokeChannelReadComplete(this.head);
    return this;
  }
  
  public final ChannelPipeline fireChannelWritabilityChanged() {
    AbstractChannelHandlerContext.invokeChannelWritabilityChanged(this.head);
    return this;
  }
  
  public final ChannelFuture bind(SocketAddress localAddress) {
    return this.tail.bind(localAddress);
  }
  
  public final ChannelFuture connect(SocketAddress remoteAddress) {
    return this.tail.connect(remoteAddress);
  }
  
  public final ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
    return this.tail.connect(remoteAddress, localAddress);
  }
  
  public final ChannelFuture disconnect() {
    return this.tail.disconnect();
  }
  
  public final ChannelFuture close() {
    return this.tail.close();
  }
  
  public final ChannelFuture deregister() {
    return this.tail.deregister();
  }
  
  public final ChannelPipeline flush() {
    this.tail.flush();
    return this;
  }
  
  public final ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
    return this.tail.bind(localAddress, promise);
  }
  
  public final ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
    return this.tail.connect(remoteAddress, promise);
  }
  
  public final ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
    return this.tail.connect(remoteAddress, localAddress, promise);
  }
  
  public final ChannelFuture disconnect(ChannelPromise promise) {
    return this.tail.disconnect(promise);
  }
  
  public final ChannelFuture close(ChannelPromise promise) {
    return this.tail.close(promise);
  }
  
  public final ChannelFuture deregister(ChannelPromise promise) {
    return this.tail.deregister(promise);
  }
  
  public final ChannelPipeline read() {
    this.tail.read();
    return this;
  }
  
  public final ChannelFuture write(Object msg) {
    return this.tail.write(msg);
  }
  
  public final ChannelFuture write(Object msg, ChannelPromise promise) {
    return this.tail.write(msg, promise);
  }
  
  public final ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
    return this.tail.writeAndFlush(msg, promise);
  }
  
  public final ChannelFuture writeAndFlush(Object msg) {
    return this.tail.writeAndFlush(msg);
  }
  
  public final ChannelPromise newPromise() {
    return new DefaultChannelPromise(this.channel);
  }
  
  public final ChannelProgressivePromise newProgressivePromise() {
    return new DefaultChannelProgressivePromise(this.channel);
  }
  
  public final ChannelFuture newSucceededFuture() {
    return this.succeededFuture;
  }
  
  public final ChannelFuture newFailedFuture(Throwable cause) {
    return new FailedChannelFuture(this.channel, null, cause);
  }
  
  public final ChannelPromise voidPromise() {
    return this.voidPromise;
  }
  
  private void checkDuplicateName(String name) {
    if (context0(name) != null)
      throw new IllegalArgumentException("Duplicate handler name: " + name); 
  }
  
  private AbstractChannelHandlerContext context0(String name) {
    AbstractChannelHandlerContext context = this.head.next;
    while (context != this.tail) {
      if (context.name().equals(name))
        return context; 
      context = context.next;
    } 
    return null;
  }
  
  private AbstractChannelHandlerContext getContextOrDie(String name) {
    AbstractChannelHandlerContext ctx = (AbstractChannelHandlerContext)context(name);
    if (ctx == null)
      throw new NoSuchElementException(name); 
    return ctx;
  }
  
  private AbstractChannelHandlerContext getContextOrDie(ChannelHandler handler) {
    AbstractChannelHandlerContext ctx = (AbstractChannelHandlerContext)context(handler);
    if (ctx == null)
      throw new NoSuchElementException(handler.getClass().getName()); 
    return ctx;
  }
  
  private AbstractChannelHandlerContext getContextOrDie(Class<? extends ChannelHandler> handlerType) {
    AbstractChannelHandlerContext ctx = (AbstractChannelHandlerContext)context(handlerType);
    if (ctx == null)
      throw new NoSuchElementException(handlerType.getName()); 
    return ctx;
  }
  
  private void callHandlerAddedForAllHandlers() {
    PendingHandlerCallback pendingHandlerCallbackHead;
    synchronized (this) {
      assert !this.registered;
      this.registered = true;
      pendingHandlerCallbackHead = this.pendingHandlerCallbackHead;
      this.pendingHandlerCallbackHead = null;
    } 
    PendingHandlerCallback task = pendingHandlerCallbackHead;
    while (task != null) {
      task.execute();
      task = task.next;
    } 
  }
  
  private void callHandlerCallbackLater(AbstractChannelHandlerContext ctx, boolean added) {
    assert !this.registered;
    PendingHandlerCallback task = added ? new PendingHandlerAddedTask(ctx) : new PendingHandlerRemovedTask(ctx);
    PendingHandlerCallback pending = this.pendingHandlerCallbackHead;
    if (pending == null) {
      this.pendingHandlerCallbackHead = task;
    } else {
      while (pending.next != null)
        pending = pending.next; 
      pending.next = task;
    } 
  }
  
  protected void onUnhandledInboundException(Throwable cause) {
    try {
      logger.warn("An exceptionCaught() event was fired, and it reached at the tail of the pipeline. It usually means the last handler in the pipeline did not handle the exception.", cause);
    } finally {
      ReferenceCountUtil.release(cause);
    } 
  }
  
  protected void onUnhandledInboundChannelActive() {}
  
  protected void onUnhandledInboundChannelInactive() {}
  
  protected void onUnhandledInboundMessage(Object msg) {
    try {
      logger.debug("Discarded inbound message {} that reached at the tail of the pipeline. Please check your pipeline configuration.", msg);
    } finally {
      ReferenceCountUtil.release(msg);
    } 
  }
  
  protected void onUnhandledInboundChannelReadComplete() {}
  
  protected void onUnhandledInboundUserEventTriggered(Object evt) {
    ReferenceCountUtil.release(evt);
  }
  
  protected void onUnhandledChannelWritabilityChanged() {}
  
  protected void incrementPendingOutboundBytes(long size) {
    ChannelOutboundBuffer buffer = this.channel.unsafe().outboundBuffer();
    if (buffer != null)
      buffer.incrementPendingOutboundBytes(size); 
  }
  
  protected void decrementPendingOutboundBytes(long size) {
    ChannelOutboundBuffer buffer = this.channel.unsafe().outboundBuffer();
    if (buffer != null)
      buffer.decrementPendingOutboundBytes(size); 
  }
  
  final class TailContext extends AbstractChannelHandlerContext implements ChannelInboundHandler {
    TailContext(DefaultChannelPipeline pipeline) {
      super(pipeline, (EventExecutor)null, DefaultChannelPipeline.TAIL_NAME, true, false);
      setAddComplete();
    }
    
    public ChannelHandler handler() {
      return this;
    }
    
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {}
    
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {}
    
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      DefaultChannelPipeline.this.onUnhandledInboundChannelActive();
    }
    
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      DefaultChannelPipeline.this.onUnhandledInboundChannelInactive();
    }
    
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
      DefaultChannelPipeline.this.onUnhandledChannelWritabilityChanged();
    }
    
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {}
    
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {}
    
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      DefaultChannelPipeline.this.onUnhandledInboundUserEventTriggered(evt);
    }
    
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      DefaultChannelPipeline.this.onUnhandledInboundException(cause);
    }
    
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      DefaultChannelPipeline.this.onUnhandledInboundMessage(msg);
    }
    
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      DefaultChannelPipeline.this.onUnhandledInboundChannelReadComplete();
    }
  }
  
  final class HeadContext extends AbstractChannelHandlerContext implements ChannelOutboundHandler, ChannelInboundHandler {
    private final Channel.Unsafe unsafe;
    
    HeadContext(DefaultChannelPipeline pipeline) {
      super(pipeline, (EventExecutor)null, DefaultChannelPipeline.HEAD_NAME, false, true);
      this.unsafe = pipeline.channel().unsafe();
      setAddComplete();
    }
    
    public ChannelHandler handler() {
      return this;
    }
    
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {}
    
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {}
    
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
      this.unsafe.bind(localAddress, promise);
    }
    
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
      this.unsafe.connect(remoteAddress, localAddress, promise);
    }
    
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
      this.unsafe.disconnect(promise);
    }
    
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
      this.unsafe.close(promise);
    }
    
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
      this.unsafe.deregister(promise);
    }
    
    public void read(ChannelHandlerContext ctx) {
      this.unsafe.beginRead();
    }
    
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      this.unsafe.write(msg, promise);
    }
    
    public void flush(ChannelHandlerContext ctx) throws Exception {
      this.unsafe.flush();
    }
    
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      ctx.fireExceptionCaught(cause);
    }
    
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
      DefaultChannelPipeline.this.invokeHandlerAddedIfNeeded();
      ctx.fireChannelRegistered();
    }
    
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
      ctx.fireChannelUnregistered();
      if (!DefaultChannelPipeline.this.channel.isOpen())
        DefaultChannelPipeline.this.destroy(); 
    }
    
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      ctx.fireChannelActive();
      readIfIsAutoRead();
    }
    
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      ctx.fireChannelInactive();
    }
    
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      ctx.fireChannelRead(msg);
    }
    
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      ctx.fireChannelReadComplete();
      readIfIsAutoRead();
    }
    
    private void readIfIsAutoRead() {
      if (DefaultChannelPipeline.this.channel.config().isAutoRead())
        DefaultChannelPipeline.this.channel.read(); 
    }
    
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      ctx.fireUserEventTriggered(evt);
    }
    
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
      ctx.fireChannelWritabilityChanged();
    }
  }
  
  private static abstract class PendingHandlerCallback implements Runnable {
    final AbstractChannelHandlerContext ctx;
    
    PendingHandlerCallback next;
    
    PendingHandlerCallback(AbstractChannelHandlerContext ctx) {
      this.ctx = ctx;
    }
    
    abstract void execute();
  }
  
  private final class PendingHandlerAddedTask extends PendingHandlerCallback {
    PendingHandlerAddedTask(AbstractChannelHandlerContext ctx) {
      super(ctx);
    }
    
    public void run() {
      DefaultChannelPipeline.this.callHandlerAdded0(this.ctx);
    }
    
    void execute() {
      EventExecutor executor = this.ctx.executor();
      if (executor.inEventLoop()) {
        DefaultChannelPipeline.this.callHandlerAdded0(this.ctx);
      } else {
        try {
          executor.execute(this);
        } catch (RejectedExecutionException e) {
          if (DefaultChannelPipeline.logger.isWarnEnabled())
            DefaultChannelPipeline.logger.warn("Can't invoke handlerAdded() as the EventExecutor {} rejected it, removing handler {}.", new Object[] { executor, this.ctx
                  
                  .name(), e }); 
          DefaultChannelPipeline.remove0(this.ctx);
          this.ctx.setRemoved();
        } 
      } 
    }
  }
  
  private final class PendingHandlerRemovedTask extends PendingHandlerCallback {
    PendingHandlerRemovedTask(AbstractChannelHandlerContext ctx) {
      super(ctx);
    }
    
    public void run() {
      DefaultChannelPipeline.this.callHandlerRemoved0(this.ctx);
    }
    
    void execute() {
      EventExecutor executor = this.ctx.executor();
      if (executor.inEventLoop()) {
        DefaultChannelPipeline.this.callHandlerRemoved0(this.ctx);
      } else {
        try {
          executor.execute(this);
        } catch (RejectedExecutionException e) {
          if (DefaultChannelPipeline.logger.isWarnEnabled())
            DefaultChannelPipeline.logger.warn("Can't invoke handlerRemoved() as the EventExecutor {} rejected it, removing handler {}.", new Object[] { executor, this.ctx
                  
                  .name(), e }); 
          this.ctx.setRemoved();
        } 
      } 
    }
  }
}

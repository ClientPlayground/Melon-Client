package com.github.steveice10.netty.channel.embedded;

import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelId;
import com.github.steveice10.netty.channel.ChannelInitializer;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.DefaultChannelConfig;
import com.github.steveice10.netty.channel.DefaultChannelPipeline;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.RecyclableArrayList;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Queue;

public class EmbeddedChannel extends AbstractChannel {
  private static final SocketAddress LOCAL_ADDRESS = new EmbeddedSocketAddress();
  
  private static final SocketAddress REMOTE_ADDRESS = new EmbeddedSocketAddress();
  
  private static final ChannelHandler[] EMPTY_HANDLERS = new ChannelHandler[0];
  
  private enum State {
    OPEN, ACTIVE, CLOSED;
  }
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(EmbeddedChannel.class);
  
  private static final ChannelMetadata METADATA_NO_DISCONNECT = new ChannelMetadata(false);
  
  private static final ChannelMetadata METADATA_DISCONNECT = new ChannelMetadata(true);
  
  private final EmbeddedEventLoop loop = new EmbeddedEventLoop();
  
  private final ChannelFutureListener recordExceptionListener = new ChannelFutureListener() {
      public void operationComplete(ChannelFuture future) throws Exception {
        EmbeddedChannel.this.recordException(future);
      }
    };
  
  private final ChannelMetadata metadata;
  
  private final ChannelConfig config;
  
  private Queue<Object> inboundMessages;
  
  private Queue<Object> outboundMessages;
  
  private Throwable lastException;
  
  private State state;
  
  public EmbeddedChannel() {
    this(EMPTY_HANDLERS);
  }
  
  public EmbeddedChannel(ChannelId channelId) {
    this(channelId, EMPTY_HANDLERS);
  }
  
  public EmbeddedChannel(ChannelHandler... handlers) {
    this(EmbeddedChannelId.INSTANCE, handlers);
  }
  
  public EmbeddedChannel(boolean hasDisconnect, ChannelHandler... handlers) {
    this(EmbeddedChannelId.INSTANCE, hasDisconnect, handlers);
  }
  
  public EmbeddedChannel(boolean register, boolean hasDisconnect, ChannelHandler... handlers) {
    this(EmbeddedChannelId.INSTANCE, register, hasDisconnect, handlers);
  }
  
  public EmbeddedChannel(ChannelId channelId, ChannelHandler... handlers) {
    this(channelId, false, handlers);
  }
  
  public EmbeddedChannel(ChannelId channelId, boolean hasDisconnect, ChannelHandler... handlers) {
    this(channelId, true, hasDisconnect, handlers);
  }
  
  public EmbeddedChannel(ChannelId channelId, boolean register, boolean hasDisconnect, ChannelHandler... handlers) {
    super(null, channelId);
    this.metadata = metadata(hasDisconnect);
    this.config = (ChannelConfig)new DefaultChannelConfig((Channel)this);
    setup(register, handlers);
  }
  
  public EmbeddedChannel(ChannelId channelId, boolean hasDisconnect, ChannelConfig config, ChannelHandler... handlers) {
    super(null, channelId);
    this.metadata = metadata(hasDisconnect);
    this.config = (ChannelConfig)ObjectUtil.checkNotNull(config, "config");
    setup(true, handlers);
  }
  
  private static ChannelMetadata metadata(boolean hasDisconnect) {
    return hasDisconnect ? METADATA_DISCONNECT : METADATA_NO_DISCONNECT;
  }
  
  private void setup(boolean register, ChannelHandler... handlers) {
    ObjectUtil.checkNotNull(handlers, "handlers");
    ChannelPipeline p = pipeline();
    p.addLast(new ChannelHandler[] { (ChannelHandler)new ChannelInitializer<Channel>() {
            protected void initChannel(Channel ch) throws Exception {
              ChannelPipeline pipeline = ch.pipeline();
              for (ChannelHandler h : handlers) {
                if (h == null)
                  break; 
                pipeline.addLast(new ChannelHandler[] { h });
              } 
            }
          } });
    if (register) {
      ChannelFuture future = this.loop.register((Channel)this);
      assert future.isDone();
    } 
  }
  
  public void register() throws Exception {
    ChannelFuture future = this.loop.register((Channel)this);
    assert future.isDone();
    Throwable cause = future.cause();
    if (cause != null)
      PlatformDependent.throwException(cause); 
  }
  
  protected final DefaultChannelPipeline newChannelPipeline() {
    return new EmbeddedChannelPipeline(this);
  }
  
  public ChannelMetadata metadata() {
    return this.metadata;
  }
  
  public ChannelConfig config() {
    return this.config;
  }
  
  public boolean isOpen() {
    return (this.state != State.CLOSED);
  }
  
  public boolean isActive() {
    return (this.state == State.ACTIVE);
  }
  
  public Queue<Object> inboundMessages() {
    if (this.inboundMessages == null)
      this.inboundMessages = new ArrayDeque(); 
    return this.inboundMessages;
  }
  
  @Deprecated
  public Queue<Object> lastInboundBuffer() {
    return inboundMessages();
  }
  
  public Queue<Object> outboundMessages() {
    if (this.outboundMessages == null)
      this.outboundMessages = new ArrayDeque(); 
    return this.outboundMessages;
  }
  
  @Deprecated
  public Queue<Object> lastOutboundBuffer() {
    return outboundMessages();
  }
  
  public <T> T readInbound() {
    T message = (T)poll(this.inboundMessages);
    if (message != null)
      ReferenceCountUtil.touch(message, "Caller of readInbound() will handle the message from this point"); 
    return message;
  }
  
  public <T> T readOutbound() {
    T message = (T)poll(this.outboundMessages);
    if (message != null)
      ReferenceCountUtil.touch(message, "Caller of readOutbound() will handle the message from this point."); 
    return message;
  }
  
  public boolean writeInbound(Object... msgs) {
    ensureOpen();
    if (msgs.length == 0)
      return isNotEmpty(this.inboundMessages); 
    ChannelPipeline p = pipeline();
    for (Object m : msgs)
      p.fireChannelRead(m); 
    flushInbound(false, voidPromise());
    return isNotEmpty(this.inboundMessages);
  }
  
  public ChannelFuture writeOneInbound(Object msg) {
    return writeOneInbound(msg, newPromise());
  }
  
  public ChannelFuture writeOneInbound(Object msg, ChannelPromise promise) {
    if (checkOpen(true))
      pipeline().fireChannelRead(msg); 
    return checkException(promise);
  }
  
  public EmbeddedChannel flushInbound() {
    flushInbound(true, voidPromise());
    return this;
  }
  
  private ChannelFuture flushInbound(boolean recordException, ChannelPromise promise) {
    if (checkOpen(recordException)) {
      pipeline().fireChannelReadComplete();
      runPendingTasks();
    } 
    return checkException(promise);
  }
  
  public boolean writeOutbound(Object... msgs) {
    ensureOpen();
    if (msgs.length == 0)
      return isNotEmpty(this.outboundMessages); 
    RecyclableArrayList futures = RecyclableArrayList.newInstance(msgs.length);
    try {
      for (Object m : msgs) {
        if (m == null)
          break; 
        futures.add(write(m));
      } 
      flushOutbound0();
      int size = futures.size();
      for (int i = 0; i < size; i++) {
        ChannelFuture future = (ChannelFuture)futures.get(i);
        if (future.isDone()) {
          recordException(future);
        } else {
          future.addListener((GenericFutureListener)this.recordExceptionListener);
        } 
      } 
      checkException();
      return isNotEmpty(this.outboundMessages);
    } finally {
      futures.recycle();
    } 
  }
  
  public ChannelFuture writeOneOutbound(Object msg) {
    return writeOneOutbound(msg, newPromise());
  }
  
  public ChannelFuture writeOneOutbound(Object msg, ChannelPromise promise) {
    if (checkOpen(true))
      return write(msg, promise); 
    return checkException(promise);
  }
  
  public EmbeddedChannel flushOutbound() {
    if (checkOpen(true))
      flushOutbound0(); 
    checkException(voidPromise());
    return this;
  }
  
  private void flushOutbound0() {
    runPendingTasks();
    flush();
  }
  
  public boolean finish() {
    return finish(false);
  }
  
  public boolean finishAndReleaseAll() {
    return finish(true);
  }
  
  private boolean finish(boolean releaseAll) {
    close();
    try {
      checkException();
      return (isNotEmpty(this.inboundMessages) || isNotEmpty(this.outboundMessages));
    } finally {
      if (releaseAll) {
        releaseAll(this.inboundMessages);
        releaseAll(this.outboundMessages);
      } 
    } 
  }
  
  public boolean releaseInbound() {
    return releaseAll(this.inboundMessages);
  }
  
  public boolean releaseOutbound() {
    return releaseAll(this.outboundMessages);
  }
  
  private static boolean releaseAll(Queue<Object> queue) {
    if (isNotEmpty(queue)) {
      while (true) {
        Object msg = queue.poll();
        if (msg == null)
          break; 
        ReferenceCountUtil.release(msg);
      } 
      return true;
    } 
    return false;
  }
  
  private void finishPendingTasks(boolean cancel) {
    runPendingTasks();
    if (cancel)
      this.loop.cancelScheduledTasks(); 
  }
  
  public final ChannelFuture close() {
    return close(newPromise());
  }
  
  public final ChannelFuture disconnect() {
    return disconnect(newPromise());
  }
  
  public final ChannelFuture close(ChannelPromise promise) {
    runPendingTasks();
    ChannelFuture future = super.close(promise);
    finishPendingTasks(true);
    return future;
  }
  
  public final ChannelFuture disconnect(ChannelPromise promise) {
    ChannelFuture future = super.disconnect(promise);
    finishPendingTasks(!this.metadata.hasDisconnect());
    return future;
  }
  
  private static boolean isNotEmpty(Queue<Object> queue) {
    return (queue != null && !queue.isEmpty());
  }
  
  private static Object poll(Queue<Object> queue) {
    return (queue != null) ? queue.poll() : null;
  }
  
  public void runPendingTasks() {
    try {
      this.loop.runTasks();
    } catch (Exception e) {
      recordException(e);
    } 
    try {
      this.loop.runScheduledTasks();
    } catch (Exception e) {
      recordException(e);
    } 
  }
  
  public long runScheduledPendingTasks() {
    try {
      return this.loop.runScheduledTasks();
    } catch (Exception e) {
      recordException(e);
      return this.loop.nextScheduledTask();
    } 
  }
  
  private void recordException(ChannelFuture future) {
    if (!future.isSuccess())
      recordException(future.cause()); 
  }
  
  private void recordException(Throwable cause) {
    if (this.lastException == null) {
      this.lastException = cause;
    } else {
      logger.warn("More than one exception was raised. Will report only the first one and log others.", cause);
    } 
  }
  
  private ChannelFuture checkException(ChannelPromise promise) {
    Throwable t = this.lastException;
    if (t != null) {
      this.lastException = null;
      if (promise.isVoid())
        PlatformDependent.throwException(t); 
      return (ChannelFuture)promise.setFailure(t);
    } 
    return (ChannelFuture)promise.setSuccess();
  }
  
  public void checkException() {
    checkException(voidPromise());
  }
  
  private boolean checkOpen(boolean recordException) {
    if (!isOpen()) {
      if (recordException)
        recordException(new ClosedChannelException()); 
      return false;
    } 
    return true;
  }
  
  protected final void ensureOpen() {
    if (!checkOpen(true))
      checkException(); 
  }
  
  protected boolean isCompatible(EventLoop loop) {
    return loop instanceof EmbeddedEventLoop;
  }
  
  protected SocketAddress localAddress0() {
    return isActive() ? LOCAL_ADDRESS : null;
  }
  
  protected SocketAddress remoteAddress0() {
    return isActive() ? REMOTE_ADDRESS : null;
  }
  
  protected void doRegister() throws Exception {
    this.state = State.ACTIVE;
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {}
  
  protected void doDisconnect() throws Exception {
    if (!this.metadata.hasDisconnect())
      doClose(); 
  }
  
  protected void doClose() throws Exception {
    this.state = State.CLOSED;
  }
  
  protected void doBeginRead() throws Exception {}
  
  protected AbstractChannel.AbstractUnsafe newUnsafe() {
    return new EmbeddedUnsafe();
  }
  
  public Channel.Unsafe unsafe() {
    return ((EmbeddedUnsafe)super.unsafe()).wrapped;
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    while (true) {
      Object msg = in.current();
      if (msg == null)
        break; 
      ReferenceCountUtil.retain(msg);
      handleOutboundMessage(msg);
      in.remove();
    } 
  }
  
  protected void handleOutboundMessage(Object msg) {
    outboundMessages().add(msg);
  }
  
  protected void handleInboundMessage(Object msg) {
    inboundMessages().add(msg);
  }
  
  private final class EmbeddedUnsafe extends AbstractChannel.AbstractUnsafe {
    final Channel.Unsafe wrapped;
    
    private EmbeddedUnsafe() {
      super(EmbeddedChannel.this);
      this.wrapped = new Channel.Unsafe() {
          public RecvByteBufAllocator.Handle recvBufAllocHandle() {
            return EmbeddedChannel.EmbeddedUnsafe.this.recvBufAllocHandle();
          }
          
          public SocketAddress localAddress() {
            return EmbeddedChannel.EmbeddedUnsafe.this.localAddress();
          }
          
          public SocketAddress remoteAddress() {
            return EmbeddedChannel.EmbeddedUnsafe.this.remoteAddress();
          }
          
          public void register(EventLoop eventLoop, ChannelPromise promise) {
            EmbeddedChannel.EmbeddedUnsafe.this.register(eventLoop, promise);
            EmbeddedChannel.this.runPendingTasks();
          }
          
          public void bind(SocketAddress localAddress, ChannelPromise promise) {
            EmbeddedChannel.EmbeddedUnsafe.this.bind(localAddress, promise);
            EmbeddedChannel.this.runPendingTasks();
          }
          
          public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            EmbeddedChannel.EmbeddedUnsafe.this.connect(remoteAddress, localAddress, promise);
            EmbeddedChannel.this.runPendingTasks();
          }
          
          public void disconnect(ChannelPromise promise) {
            EmbeddedChannel.EmbeddedUnsafe.this.disconnect(promise);
            EmbeddedChannel.this.runPendingTasks();
          }
          
          public void close(ChannelPromise promise) {
            EmbeddedChannel.EmbeddedUnsafe.this.close(promise);
            EmbeddedChannel.this.runPendingTasks();
          }
          
          public void closeForcibly() {
            EmbeddedChannel.EmbeddedUnsafe.this.closeForcibly();
            EmbeddedChannel.this.runPendingTasks();
          }
          
          public void deregister(ChannelPromise promise) {
            EmbeddedChannel.EmbeddedUnsafe.this.deregister(promise);
            EmbeddedChannel.this.runPendingTasks();
          }
          
          public void beginRead() {
            EmbeddedChannel.EmbeddedUnsafe.this.beginRead();
            EmbeddedChannel.this.runPendingTasks();
          }
          
          public void write(Object msg, ChannelPromise promise) {
            EmbeddedChannel.EmbeddedUnsafe.this.write(msg, promise);
            EmbeddedChannel.this.runPendingTasks();
          }
          
          public void flush() {
            EmbeddedChannel.EmbeddedUnsafe.this.flush();
            EmbeddedChannel.this.runPendingTasks();
          }
          
          public ChannelPromise voidPromise() {
            return EmbeddedChannel.EmbeddedUnsafe.this.voidPromise();
          }
          
          public ChannelOutboundBuffer outboundBuffer() {
            return EmbeddedChannel.EmbeddedUnsafe.this.outboundBuffer();
          }
        };
    }
    
    public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
      safeSetSuccess(promise);
    }
  }
  
  private final class EmbeddedChannelPipeline extends DefaultChannelPipeline {
    EmbeddedChannelPipeline(EmbeddedChannel channel) {
      super((Channel)channel);
    }
    
    protected void onUnhandledInboundException(Throwable cause) {
      EmbeddedChannel.this.recordException(cause);
    }
    
    protected void onUnhandledInboundMessage(Object msg) {
      EmbeddedChannel.this.handleInboundMessage(msg);
    }
  }
}

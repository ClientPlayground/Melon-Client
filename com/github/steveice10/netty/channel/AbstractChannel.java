package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.socket.ChannelOutputShutdownEvent;
import com.github.steveice10.netty.channel.socket.ChannelOutputShutdownException;
import com.github.steveice10.netty.util.DefaultAttributeMap;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public abstract class AbstractChannel extends DefaultAttributeMap implements Channel {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractChannel.class);
  
  private static final ClosedChannelException FLUSH0_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "flush0()");
  
  private static final ClosedChannelException ENSURE_OPEN_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "ensureOpen(...)");
  
  private static final ClosedChannelException CLOSE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "close(...)");
  
  private static final ClosedChannelException WRITE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractUnsafe.class, "write(...)");
  
  private static final NotYetConnectedException FLUSH0_NOT_YET_CONNECTED_EXCEPTION = (NotYetConnectedException)ThrowableUtil.unknownStackTrace(new NotYetConnectedException(), AbstractUnsafe.class, "flush0()");
  
  private final Channel parent;
  
  private final ChannelId id;
  
  private final Channel.Unsafe unsafe;
  
  private final DefaultChannelPipeline pipeline;
  
  private final VoidChannelPromise unsafeVoidPromise = new VoidChannelPromise(this, false);
  
  private final CloseFuture closeFuture = new CloseFuture(this);
  
  private volatile SocketAddress localAddress;
  
  private volatile SocketAddress remoteAddress;
  
  private volatile EventLoop eventLoop;
  
  private volatile boolean registered;
  
  private boolean closeInitiated;
  
  private boolean strValActive;
  
  private String strVal;
  
  protected AbstractChannel(Channel parent) {
    this.parent = parent;
    this.id = newId();
    this.unsafe = newUnsafe();
    this.pipeline = newChannelPipeline();
  }
  
  protected AbstractChannel(Channel parent, ChannelId id) {
    this.parent = parent;
    this.id = id;
    this.unsafe = newUnsafe();
    this.pipeline = newChannelPipeline();
  }
  
  public final ChannelId id() {
    return this.id;
  }
  
  protected ChannelId newId() {
    return DefaultChannelId.newInstance();
  }
  
  protected DefaultChannelPipeline newChannelPipeline() {
    return new DefaultChannelPipeline(this);
  }
  
  public boolean isWritable() {
    ChannelOutboundBuffer buf = this.unsafe.outboundBuffer();
    return (buf != null && buf.isWritable());
  }
  
  public long bytesBeforeUnwritable() {
    ChannelOutboundBuffer buf = this.unsafe.outboundBuffer();
    return (buf != null) ? buf.bytesBeforeUnwritable() : 0L;
  }
  
  public long bytesBeforeWritable() {
    ChannelOutboundBuffer buf = this.unsafe.outboundBuffer();
    return (buf != null) ? buf.bytesBeforeWritable() : Long.MAX_VALUE;
  }
  
  public Channel parent() {
    return this.parent;
  }
  
  public ChannelPipeline pipeline() {
    return this.pipeline;
  }
  
  public ByteBufAllocator alloc() {
    return config().getAllocator();
  }
  
  public EventLoop eventLoop() {
    EventLoop eventLoop = this.eventLoop;
    if (eventLoop == null)
      throw new IllegalStateException("channel not registered to an event loop"); 
    return eventLoop;
  }
  
  public SocketAddress localAddress() {
    SocketAddress localAddress = this.localAddress;
    if (localAddress == null)
      try {
        this.localAddress = localAddress = unsafe().localAddress();
      } catch (Throwable t) {
        return null;
      }  
    return localAddress;
  }
  
  @Deprecated
  protected void invalidateLocalAddress() {
    this.localAddress = null;
  }
  
  public SocketAddress remoteAddress() {
    SocketAddress remoteAddress = this.remoteAddress;
    if (remoteAddress == null)
      try {
        this.remoteAddress = remoteAddress = unsafe().remoteAddress();
      } catch (Throwable t) {
        return null;
      }  
    return remoteAddress;
  }
  
  @Deprecated
  protected void invalidateRemoteAddress() {
    this.remoteAddress = null;
  }
  
  public boolean isRegistered() {
    return this.registered;
  }
  
  public ChannelFuture bind(SocketAddress localAddress) {
    return this.pipeline.bind(localAddress);
  }
  
  public ChannelFuture connect(SocketAddress remoteAddress) {
    return this.pipeline.connect(remoteAddress);
  }
  
  public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
    return this.pipeline.connect(remoteAddress, localAddress);
  }
  
  public ChannelFuture disconnect() {
    return this.pipeline.disconnect();
  }
  
  public ChannelFuture close() {
    return this.pipeline.close();
  }
  
  public ChannelFuture deregister() {
    return this.pipeline.deregister();
  }
  
  public Channel flush() {
    this.pipeline.flush();
    return this;
  }
  
  public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
    return this.pipeline.bind(localAddress, promise);
  }
  
  public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
    return this.pipeline.connect(remoteAddress, promise);
  }
  
  public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
    return this.pipeline.connect(remoteAddress, localAddress, promise);
  }
  
  public ChannelFuture disconnect(ChannelPromise promise) {
    return this.pipeline.disconnect(promise);
  }
  
  public ChannelFuture close(ChannelPromise promise) {
    return this.pipeline.close(promise);
  }
  
  public ChannelFuture deregister(ChannelPromise promise) {
    return this.pipeline.deregister(promise);
  }
  
  public Channel read() {
    this.pipeline.read();
    return this;
  }
  
  public ChannelFuture write(Object msg) {
    return this.pipeline.write(msg);
  }
  
  public ChannelFuture write(Object msg, ChannelPromise promise) {
    return this.pipeline.write(msg, promise);
  }
  
  public ChannelFuture writeAndFlush(Object msg) {
    return this.pipeline.writeAndFlush(msg);
  }
  
  public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
    return this.pipeline.writeAndFlush(msg, promise);
  }
  
  public ChannelPromise newPromise() {
    return this.pipeline.newPromise();
  }
  
  public ChannelProgressivePromise newProgressivePromise() {
    return this.pipeline.newProgressivePromise();
  }
  
  public ChannelFuture newSucceededFuture() {
    return this.pipeline.newSucceededFuture();
  }
  
  public ChannelFuture newFailedFuture(Throwable cause) {
    return this.pipeline.newFailedFuture(cause);
  }
  
  public ChannelFuture closeFuture() {
    return this.closeFuture;
  }
  
  public Channel.Unsafe unsafe() {
    return this.unsafe;
  }
  
  public final int hashCode() {
    return this.id.hashCode();
  }
  
  public final boolean equals(Object o) {
    return (this == o);
  }
  
  public final int compareTo(Channel o) {
    if (this == o)
      return 0; 
    return id().compareTo(o.id());
  }
  
  public String toString() {
    boolean active = isActive();
    if (this.strValActive == active && this.strVal != null)
      return this.strVal; 
    SocketAddress remoteAddr = remoteAddress();
    SocketAddress localAddr = localAddress();
    if (remoteAddr != null) {
      StringBuilder buf = (new StringBuilder(96)).append("[id: 0x").append(this.id.asShortText()).append(", L:").append(localAddr).append(active ? " - " : " ! ").append("R:").append(remoteAddr).append(']');
      this.strVal = buf.toString();
    } else if (localAddr != null) {
      StringBuilder buf = (new StringBuilder(64)).append("[id: 0x").append(this.id.asShortText()).append(", L:").append(localAddr).append(']');
      this.strVal = buf.toString();
    } else {
      StringBuilder buf = (new StringBuilder(16)).append("[id: 0x").append(this.id.asShortText()).append(']');
      this.strVal = buf.toString();
    } 
    this.strValActive = active;
    return this.strVal;
  }
  
  public final ChannelPromise voidPromise() {
    return this.pipeline.voidPromise();
  }
  
  protected abstract class AbstractUnsafe implements Channel.Unsafe {
    private volatile ChannelOutboundBuffer outboundBuffer = new ChannelOutboundBuffer(AbstractChannel.this);
    
    private RecvByteBufAllocator.Handle recvHandle;
    
    private boolean inFlush0;
    
    private boolean neverRegistered = true;
    
    private void assertEventLoop() {
      assert !AbstractChannel.this.registered || AbstractChannel.this.eventLoop.inEventLoop();
    }
    
    public RecvByteBufAllocator.Handle recvBufAllocHandle() {
      if (this.recvHandle == null)
        this.recvHandle = AbstractChannel.this.config().<RecvByteBufAllocator>getRecvByteBufAllocator().newHandle(); 
      return this.recvHandle;
    }
    
    public final ChannelOutboundBuffer outboundBuffer() {
      return this.outboundBuffer;
    }
    
    public final SocketAddress localAddress() {
      return AbstractChannel.this.localAddress0();
    }
    
    public final SocketAddress remoteAddress() {
      return AbstractChannel.this.remoteAddress0();
    }
    
    public final void register(EventLoop eventLoop, final ChannelPromise promise) {
      if (eventLoop == null)
        throw new NullPointerException("eventLoop"); 
      if (AbstractChannel.this.isRegistered()) {
        promise.setFailure(new IllegalStateException("registered to an event loop already"));
        return;
      } 
      if (!AbstractChannel.this.isCompatible(eventLoop)) {
        promise.setFailure(new IllegalStateException("incompatible event loop type: " + eventLoop
              .getClass().getName()));
        return;
      } 
      AbstractChannel.this.eventLoop = eventLoop;
      if (eventLoop.inEventLoop()) {
        register0(promise);
      } else {
        try {
          eventLoop.execute(new Runnable() {
                public void run() {
                  AbstractChannel.AbstractUnsafe.this.register0(promise);
                }
              });
        } catch (Throwable t) {
          AbstractChannel.logger.warn("Force-closing a channel whose registration task was not accepted by an event loop: {}", AbstractChannel.this, t);
          closeForcibly();
          AbstractChannel.this.closeFuture.setClosed();
          safeSetFailure(promise, t);
        } 
      } 
    }
    
    private void register0(ChannelPromise promise) {
      try {
        if (!promise.setUncancellable() || !ensureOpen(promise))
          return; 
        boolean firstRegistration = this.neverRegistered;
        AbstractChannel.this.doRegister();
        this.neverRegistered = false;
        AbstractChannel.this.registered = true;
        AbstractChannel.this.pipeline.invokeHandlerAddedIfNeeded();
        safeSetSuccess(promise);
        AbstractChannel.this.pipeline.fireChannelRegistered();
        if (AbstractChannel.this.isActive())
          if (firstRegistration) {
            AbstractChannel.this.pipeline.fireChannelActive();
          } else if (AbstractChannel.this.config().isAutoRead()) {
            beginRead();
          }  
      } catch (Throwable t) {
        closeForcibly();
        AbstractChannel.this.closeFuture.setClosed();
        safeSetFailure(promise, t);
      } 
    }
    
    public final void bind(SocketAddress localAddress, ChannelPromise promise) {
      assertEventLoop();
      if (!promise.setUncancellable() || !ensureOpen(promise))
        return; 
      if (Boolean.TRUE.equals(AbstractChannel.this.config().getOption(ChannelOption.SO_BROADCAST)) && localAddress instanceof InetSocketAddress && 
        
        !((InetSocketAddress)localAddress).getAddress().isAnyLocalAddress() && 
        !PlatformDependent.isWindows() && !PlatformDependent.maybeSuperUser())
        AbstractChannel.logger.warn("A non-root user can't receive a broadcast packet if the socket is not bound to a wildcard address; binding to a non-wildcard address (" + localAddress + ") anyway as requested."); 
      boolean wasActive = AbstractChannel.this.isActive();
      try {
        AbstractChannel.this.doBind(localAddress);
      } catch (Throwable t) {
        safeSetFailure(promise, t);
        closeIfClosed();
        return;
      } 
      if (!wasActive && AbstractChannel.this.isActive())
        invokeLater(new Runnable() {
              public void run() {
                AbstractChannel.this.pipeline.fireChannelActive();
              }
            }); 
      safeSetSuccess(promise);
    }
    
    public final void disconnect(ChannelPromise promise) {
      assertEventLoop();
      if (!promise.setUncancellable())
        return; 
      boolean wasActive = AbstractChannel.this.isActive();
      try {
        AbstractChannel.this.doDisconnect();
      } catch (Throwable t) {
        safeSetFailure(promise, t);
        closeIfClosed();
        return;
      } 
      if (wasActive && !AbstractChannel.this.isActive())
        invokeLater(new Runnable() {
              public void run() {
                AbstractChannel.this.pipeline.fireChannelInactive();
              }
            }); 
      safeSetSuccess(promise);
      closeIfClosed();
    }
    
    public final void close(ChannelPromise promise) {
      assertEventLoop();
      close(promise, AbstractChannel.CLOSE_CLOSED_CHANNEL_EXCEPTION, AbstractChannel.CLOSE_CLOSED_CHANNEL_EXCEPTION, false);
    }
    
    public final void shutdownOutput(ChannelPromise promise) {
      assertEventLoop();
      shutdownOutput(promise, null);
    }
    
    private void shutdownOutput(final ChannelPromise promise, Throwable cause) {
      if (!promise.setUncancellable())
        return; 
      final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
      if (outboundBuffer == null) {
        promise.setFailure(AbstractChannel.CLOSE_CLOSED_CHANNEL_EXCEPTION);
        return;
      } 
      this.outboundBuffer = null;
      final ChannelOutputShutdownException shutdownCause = (cause == null) ? new ChannelOutputShutdownException("Channel output shutdown") : new ChannelOutputShutdownException("Channel output shutdown", cause);
      Executor closeExecutor = prepareToClose();
      if (closeExecutor != null) {
        closeExecutor.execute(new Runnable() {
              public void run() {
                try {
                  AbstractChannel.this.doShutdownOutput();
                  promise.setSuccess();
                } catch (Throwable err) {
                  promise.setFailure(err);
                } finally {
                  AbstractChannel.this.eventLoop().execute(new Runnable() {
                        public void run() {
                          AbstractChannel.AbstractUnsafe.this.closeOutboundBufferForShutdown(AbstractChannel.this.pipeline, outboundBuffer, shutdownCause);
                        }
                      });
                } 
              }
            });
      } else {
        try {
          AbstractChannel.this.doShutdownOutput();
          promise.setSuccess();
        } catch (Throwable err) {
          promise.setFailure(err);
        } finally {
          closeOutboundBufferForShutdown(AbstractChannel.this.pipeline, outboundBuffer, (Throwable)channelOutputShutdownException);
        } 
      } 
    }
    
    private void closeOutboundBufferForShutdown(ChannelPipeline pipeline, ChannelOutboundBuffer buffer, Throwable cause) {
      buffer.failFlushed(cause, false);
      buffer.close(cause, true);
      pipeline.fireUserEventTriggered(ChannelOutputShutdownEvent.INSTANCE);
    }
    
    private void close(final ChannelPromise promise, final Throwable cause, final ClosedChannelException closeCause, final boolean notify) {
      if (!promise.setUncancellable())
        return; 
      if (AbstractChannel.this.closeInitiated) {
        if (AbstractChannel.this.closeFuture.isDone()) {
          safeSetSuccess(promise);
        } else if (!(promise instanceof VoidChannelPromise)) {
          AbstractChannel.this.closeFuture.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                  promise.setSuccess();
                }
              });
        } 
        return;
      } 
      AbstractChannel.this.closeInitiated = true;
      final boolean wasActive = AbstractChannel.this.isActive();
      final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
      this.outboundBuffer = null;
      Executor closeExecutor = prepareToClose();
      if (closeExecutor != null) {
        closeExecutor.execute(new Runnable() {
              public void run() {
                try {
                  AbstractChannel.AbstractUnsafe.this.doClose0(promise);
                } finally {
                  AbstractChannel.AbstractUnsafe.this.invokeLater(new Runnable() {
                        public void run() {
                          if (outboundBuffer != null) {
                            outboundBuffer.failFlushed(cause, notify);
                            outboundBuffer.close(closeCause);
                          } 
                          AbstractChannel.AbstractUnsafe.this.fireChannelInactiveAndDeregister(wasActive);
                        }
                      });
                } 
              }
            });
      } else {
        try {
          doClose0(promise);
        } finally {
          if (outboundBuffer != null) {
            outboundBuffer.failFlushed(cause, notify);
            outboundBuffer.close(closeCause);
          } 
        } 
        if (this.inFlush0) {
          invokeLater(new Runnable() {
                public void run() {
                  AbstractChannel.AbstractUnsafe.this.fireChannelInactiveAndDeregister(wasActive);
                }
              });
        } else {
          fireChannelInactiveAndDeregister(wasActive);
        } 
      } 
    }
    
    private void doClose0(ChannelPromise promise) {
      try {
        AbstractChannel.this.doClose();
        AbstractChannel.this.closeFuture.setClosed();
        safeSetSuccess(promise);
      } catch (Throwable t) {
        AbstractChannel.this.closeFuture.setClosed();
        safeSetFailure(promise, t);
      } 
    }
    
    private void fireChannelInactiveAndDeregister(boolean wasActive) {
      deregister(voidPromise(), (wasActive && !AbstractChannel.this.isActive()));
    }
    
    public final void closeForcibly() {
      assertEventLoop();
      try {
        AbstractChannel.this.doClose();
      } catch (Exception e) {
        AbstractChannel.logger.warn("Failed to close a channel.", e);
      } 
    }
    
    public final void deregister(ChannelPromise promise) {
      assertEventLoop();
      deregister(promise, false);
    }
    
    private void deregister(final ChannelPromise promise, final boolean fireChannelInactive) {
      if (!promise.setUncancellable())
        return; 
      if (!AbstractChannel.this.registered) {
        safeSetSuccess(promise);
        return;
      } 
      invokeLater(new Runnable() {
            public void run() {
              try {
                AbstractChannel.this.doDeregister();
              } catch (Throwable t) {
                AbstractChannel.logger.warn("Unexpected exception occurred while deregistering a channel.", t);
              } finally {
                if (fireChannelInactive)
                  AbstractChannel.this.pipeline.fireChannelInactive(); 
                if (AbstractChannel.this.registered) {
                  AbstractChannel.this.registered = false;
                  AbstractChannel.this.pipeline.fireChannelUnregistered();
                } 
                AbstractChannel.AbstractUnsafe.this.safeSetSuccess(promise);
              } 
            }
          });
    }
    
    public final void beginRead() {
      assertEventLoop();
      if (!AbstractChannel.this.isActive())
        return; 
      try {
        AbstractChannel.this.doBeginRead();
      } catch (Exception e) {
        invokeLater(new Runnable() {
              public void run() {
                AbstractChannel.this.pipeline.fireExceptionCaught(e);
              }
            });
        close(voidPromise());
      } 
    }
    
    public final void write(Object msg, ChannelPromise promise) {
      int size;
      assertEventLoop();
      ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
      if (outboundBuffer == null) {
        safeSetFailure(promise, AbstractChannel.WRITE_CLOSED_CHANNEL_EXCEPTION);
        ReferenceCountUtil.release(msg);
        return;
      } 
      try {
        msg = AbstractChannel.this.filterOutboundMessage(msg);
        size = AbstractChannel.this.pipeline.estimatorHandle().size(msg);
        if (size < 0)
          size = 0; 
      } catch (Throwable t) {
        safeSetFailure(promise, t);
        ReferenceCountUtil.release(msg);
        return;
      } 
      outboundBuffer.addMessage(msg, size, promise);
    }
    
    public final void flush() {
      assertEventLoop();
      ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
      if (outboundBuffer == null)
        return; 
      outboundBuffer.addFlush();
      flush0();
    }
    
    protected void flush0() {
      if (this.inFlush0)
        return; 
      ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
      if (outboundBuffer == null || outboundBuffer.isEmpty())
        return; 
      this.inFlush0 = true;
      if (!AbstractChannel.this.isActive()) {
        try {
          if (AbstractChannel.this.isOpen()) {
            outboundBuffer.failFlushed(AbstractChannel.FLUSH0_NOT_YET_CONNECTED_EXCEPTION, true);
          } else {
            outboundBuffer.failFlushed(AbstractChannel.FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
          } 
        } finally {
          this.inFlush0 = false;
        } 
        return;
      } 
      try {
        AbstractChannel.this.doWrite(outboundBuffer);
      } catch (Throwable t) {
        if (t instanceof java.io.IOException && AbstractChannel.this.config().isAutoClose()) {
          close(voidPromise(), t, AbstractChannel.FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
        } else {
          try {
            shutdownOutput(voidPromise(), t);
          } catch (Throwable t2) {
            close(voidPromise(), t2, AbstractChannel.FLUSH0_CLOSED_CHANNEL_EXCEPTION, false);
          } 
        } 
      } finally {
        this.inFlush0 = false;
      } 
    }
    
    public final ChannelPromise voidPromise() {
      assertEventLoop();
      return AbstractChannel.this.unsafeVoidPromise;
    }
    
    protected final boolean ensureOpen(ChannelPromise promise) {
      if (AbstractChannel.this.isOpen())
        return true; 
      safeSetFailure(promise, AbstractChannel.ENSURE_OPEN_CLOSED_CHANNEL_EXCEPTION);
      return false;
    }
    
    protected final void safeSetSuccess(ChannelPromise promise) {
      if (!(promise instanceof VoidChannelPromise) && !promise.trySuccess())
        AbstractChannel.logger.warn("Failed to mark a promise as success because it is done already: {}", promise); 
    }
    
    protected final void safeSetFailure(ChannelPromise promise, Throwable cause) {
      if (!(promise instanceof VoidChannelPromise) && !promise.tryFailure(cause))
        AbstractChannel.logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, cause); 
    }
    
    protected final void closeIfClosed() {
      if (AbstractChannel.this.isOpen())
        return; 
      close(voidPromise());
    }
    
    private void invokeLater(Runnable task) {
      try {
        AbstractChannel.this.eventLoop().execute(task);
      } catch (RejectedExecutionException e) {
        AbstractChannel.logger.warn("Can't invoke task later as EventLoop rejected it", e);
      } 
    }
    
    protected final Throwable annotateConnectException(Throwable cause, SocketAddress remoteAddress) {
      if (cause instanceof ConnectException)
        return new AbstractChannel.AnnotatedConnectException((ConnectException)cause, remoteAddress); 
      if (cause instanceof NoRouteToHostException)
        return new AbstractChannel.AnnotatedNoRouteToHostException((NoRouteToHostException)cause, remoteAddress); 
      if (cause instanceof SocketException)
        return new AbstractChannel.AnnotatedSocketException((SocketException)cause, remoteAddress); 
      return cause;
    }
    
    protected Executor prepareToClose() {
      return null;
    }
  }
  
  protected void doRegister() throws Exception {}
  
  protected void doShutdownOutput() throws Exception {
    doClose();
  }
  
  protected void doDeregister() throws Exception {}
  
  protected Object filterOutboundMessage(Object msg) throws Exception {
    return msg;
  }
  
  protected abstract AbstractUnsafe newUnsafe();
  
  protected abstract boolean isCompatible(EventLoop paramEventLoop);
  
  protected abstract SocketAddress localAddress0();
  
  protected abstract SocketAddress remoteAddress0();
  
  protected abstract void doBind(SocketAddress paramSocketAddress) throws Exception;
  
  protected abstract void doDisconnect() throws Exception;
  
  protected abstract void doClose() throws Exception;
  
  protected abstract void doBeginRead() throws Exception;
  
  protected abstract void doWrite(ChannelOutboundBuffer paramChannelOutboundBuffer) throws Exception;
  
  static final class CloseFuture extends DefaultChannelPromise {
    CloseFuture(AbstractChannel ch) {
      super(ch);
    }
    
    public ChannelPromise setSuccess() {
      throw new IllegalStateException();
    }
    
    public ChannelPromise setFailure(Throwable cause) {
      throw new IllegalStateException();
    }
    
    public boolean trySuccess() {
      throw new IllegalStateException();
    }
    
    public boolean tryFailure(Throwable cause) {
      throw new IllegalStateException();
    }
    
    boolean setClosed() {
      return super.trySuccess();
    }
  }
  
  private static final class AnnotatedConnectException extends ConnectException {
    private static final long serialVersionUID = 3901958112696433556L;
    
    AnnotatedConnectException(ConnectException exception, SocketAddress remoteAddress) {
      super(exception.getMessage() + ": " + remoteAddress);
      initCause(exception);
      setStackTrace(exception.getStackTrace());
    }
    
    public Throwable fillInStackTrace() {
      return this;
    }
  }
  
  private static final class AnnotatedNoRouteToHostException extends NoRouteToHostException {
    private static final long serialVersionUID = -6801433937592080623L;
    
    AnnotatedNoRouteToHostException(NoRouteToHostException exception, SocketAddress remoteAddress) {
      super(exception.getMessage() + ": " + remoteAddress);
      initCause(exception);
      setStackTrace(exception.getStackTrace());
    }
    
    public Throwable fillInStackTrace() {
      return this;
    }
  }
  
  private static final class AnnotatedSocketException extends SocketException {
    private static final long serialVersionUID = 3896743275010454039L;
    
    AnnotatedSocketException(SocketException exception, SocketAddress remoteAddress) {
      super(exception.getMessage() + ": " + remoteAddress);
      initCause(exception);
      setStackTrace(exception.getStackTrace());
    }
    
    public Throwable fillInStackTrace() {
      return this;
    }
  }
}

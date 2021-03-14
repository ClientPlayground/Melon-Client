package com.github.steveice10.netty.channel.local;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.DefaultChannelConfig;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.PreferHeapByteBufAllocator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.SingleThreadEventExecutor;
import com.github.steveice10.netty.util.internal.InternalThreadLocalMap;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.ConnectException;
import java.net.SocketAddress;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class LocalChannel extends AbstractChannel {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(LocalChannel.class);
  
  private static final AtomicReferenceFieldUpdater<LocalChannel, Future> FINISH_READ_FUTURE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(LocalChannel.class, Future.class, "finishReadFuture");
  
  private static final ChannelMetadata METADATA = new ChannelMetadata(false);
  
  private static final int MAX_READER_STACK_DEPTH = 8;
  
  private static final ClosedChannelException DO_WRITE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), LocalChannel.class, "doWrite(...)");
  
  private static final ClosedChannelException DO_CLOSE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), LocalChannel.class, "doClose()");
  
  private enum State {
    OPEN, BOUND, CONNECTED, CLOSED;
  }
  
  private final ChannelConfig config = (ChannelConfig)new DefaultChannelConfig((Channel)this);
  
  final Queue<Object> inboundBuffer = PlatformDependent.newSpscQueue();
  
  private final Runnable readTask = new Runnable() {
      public void run() {
        if (!LocalChannel.this.inboundBuffer.isEmpty())
          LocalChannel.this.readInbound(); 
      }
    };
  
  private final Runnable shutdownHook = new Runnable() {
      public void run() {
        LocalChannel.this.unsafe().close(LocalChannel.this.unsafe().voidPromise());
      }
    };
  
  private volatile State state;
  
  private volatile LocalChannel peer;
  
  private volatile LocalAddress localAddress;
  
  private volatile LocalAddress remoteAddress;
  
  private volatile ChannelPromise connectPromise;
  
  private volatile boolean readInProgress;
  
  private volatile boolean writeInProgress;
  
  private volatile Future<?> finishReadFuture;
  
  public LocalChannel() {
    super(null);
    config().setAllocator((ByteBufAllocator)new PreferHeapByteBufAllocator(this.config.getAllocator()));
  }
  
  protected LocalChannel(LocalServerChannel parent, LocalChannel peer) {
    super((Channel)parent);
    config().setAllocator((ByteBufAllocator)new PreferHeapByteBufAllocator(this.config.getAllocator()));
    this.peer = peer;
    this.localAddress = parent.localAddress();
    this.remoteAddress = peer.localAddress();
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  public ChannelConfig config() {
    return this.config;
  }
  
  public LocalServerChannel parent() {
    return (LocalServerChannel)super.parent();
  }
  
  public LocalAddress localAddress() {
    return (LocalAddress)super.localAddress();
  }
  
  public LocalAddress remoteAddress() {
    return (LocalAddress)super.remoteAddress();
  }
  
  public boolean isOpen() {
    return (this.state != State.CLOSED);
  }
  
  public boolean isActive() {
    return (this.state == State.CONNECTED);
  }
  
  protected AbstractChannel.AbstractUnsafe newUnsafe() {
    return new LocalUnsafe();
  }
  
  protected boolean isCompatible(EventLoop loop) {
    return loop instanceof com.github.steveice10.netty.channel.SingleThreadEventLoop;
  }
  
  protected SocketAddress localAddress0() {
    return this.localAddress;
  }
  
  protected SocketAddress remoteAddress0() {
    return this.remoteAddress;
  }
  
  protected void doRegister() throws Exception {
    if (this.peer != null && parent() != null) {
      final LocalChannel peer = this.peer;
      this.state = State.CONNECTED;
      peer.remoteAddress = (parent() == null) ? null : parent().localAddress();
      peer.state = State.CONNECTED;
      peer.eventLoop().execute(new Runnable() {
            public void run() {
              ChannelPromise promise = peer.connectPromise;
              if (promise != null && promise.trySuccess())
                peer.pipeline().fireChannelActive(); 
            }
          });
    } 
    ((SingleThreadEventExecutor)eventLoop()).addShutdownHook(this.shutdownHook);
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    this
      .localAddress = LocalChannelRegistry.register((Channel)this, this.localAddress, localAddress);
    this.state = State.BOUND;
  }
  
  protected void doDisconnect() throws Exception {
    doClose();
  }
  
  protected void doClose() throws Exception {
    final LocalChannel peer = this.peer;
    State oldState = this.state;
    try {
      if (oldState != State.CLOSED) {
        if (this.localAddress != null) {
          if (parent() == null)
            LocalChannelRegistry.unregister(this.localAddress); 
          this.localAddress = null;
        } 
        this.state = State.CLOSED;
        if (this.writeInProgress && peer != null)
          finishPeerRead(peer); 
        ChannelPromise promise = this.connectPromise;
        if (promise != null) {
          promise.tryFailure(DO_CLOSE_CLOSED_CHANNEL_EXCEPTION);
          this.connectPromise = null;
        } 
      } 
      if (peer != null) {
        this.peer = null;
        EventLoop peerEventLoop = peer.eventLoop();
        final boolean peerIsActive = peer.isActive();
        try {
          peerEventLoop.execute(new Runnable() {
                public void run() {
                  peer.tryClose(peerIsActive);
                }
              });
        } catch (Throwable cause) {
          logger.warn("Releasing Inbound Queues for channels {}-{} because exception occurred!", new Object[] { this, peer, cause });
          if (peerEventLoop.inEventLoop()) {
            peer.releaseInboundBuffers();
          } else {
            peer.close();
          } 
          PlatformDependent.throwException(cause);
        } 
      } 
    } finally {
      if (oldState != null && oldState != State.CLOSED)
        releaseInboundBuffers(); 
    } 
  }
  
  private void tryClose(boolean isActive) {
    if (isActive) {
      unsafe().close(unsafe().voidPromise());
    } else {
      releaseInboundBuffers();
    } 
  }
  
  protected void doDeregister() throws Exception {
    ((SingleThreadEventExecutor)eventLoop()).removeShutdownHook(this.shutdownHook);
  }
  
  private void readInbound() {
    RecvByteBufAllocator.Handle handle = unsafe().recvBufAllocHandle();
    handle.reset(config());
    ChannelPipeline pipeline = pipeline();
    do {
      Object received = this.inboundBuffer.poll();
      if (received == null)
        break; 
      pipeline.fireChannelRead(received);
    } while (handle.continueReading());
    pipeline.fireChannelReadComplete();
  }
  
  protected void doBeginRead() throws Exception {
    if (this.readInProgress)
      return; 
    Queue<Object> inboundBuffer = this.inboundBuffer;
    if (inboundBuffer.isEmpty()) {
      this.readInProgress = true;
      return;
    } 
    InternalThreadLocalMap threadLocals = InternalThreadLocalMap.get();
    Integer stackDepth = Integer.valueOf(threadLocals.localChannelReaderStackDepth());
    if (stackDepth.intValue() < 8) {
      threadLocals.setLocalChannelReaderStackDepth(stackDepth.intValue() + 1);
      try {
        readInbound();
      } finally {
        threadLocals.setLocalChannelReaderStackDepth(stackDepth.intValue());
      } 
    } else {
      try {
        eventLoop().execute(this.readTask);
      } catch (Throwable cause) {
        logger.warn("Closing Local channels {}-{} because exception occurred!", new Object[] { this, this.peer, cause });
        close();
        this.peer.close();
        PlatformDependent.throwException(cause);
      } 
    } 
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    switch (this.state) {
      case OPEN:
      case BOUND:
        throw new NotYetConnectedException();
      case CLOSED:
        throw DO_WRITE_CLOSED_CHANNEL_EXCEPTION;
    } 
    LocalChannel peer = this.peer;
    this.writeInProgress = true;
    try {
      while (true) {
        Object msg = in.current();
        if (msg == null)
          break; 
        try {
          if (peer.state == State.CONNECTED) {
            peer.inboundBuffer.add(ReferenceCountUtil.retain(msg));
            in.remove();
            continue;
          } 
          in.remove(DO_WRITE_CLOSED_CHANNEL_EXCEPTION);
        } catch (Throwable cause) {
          in.remove(cause);
        } 
      } 
    } finally {
      this.writeInProgress = false;
    } 
    finishPeerRead(peer);
  }
  
  private void finishPeerRead(LocalChannel peer) {
    if (peer.eventLoop() == eventLoop() && !peer.writeInProgress) {
      finishPeerRead0(peer);
    } else {
      runFinishPeerReadTask(peer);
    } 
  }
  
  private void runFinishPeerReadTask(final LocalChannel peer) {
    Runnable finishPeerReadTask = new Runnable() {
        public void run() {
          LocalChannel.this.finishPeerRead0(peer);
        }
      };
    try {
      if (peer.writeInProgress) {
        peer.finishReadFuture = peer.eventLoop().submit(finishPeerReadTask);
      } else {
        peer.eventLoop().execute(finishPeerReadTask);
      } 
    } catch (Throwable cause) {
      logger.warn("Closing Local channels {}-{} because exception occurred!", new Object[] { this, peer, cause });
      close();
      peer.close();
      PlatformDependent.throwException(cause);
    } 
  }
  
  private void releaseInboundBuffers() {
    assert eventLoop() == null || eventLoop().inEventLoop();
    this.readInProgress = false;
    Queue<Object> inboundBuffer = this.inboundBuffer;
    Object msg;
    while ((msg = inboundBuffer.poll()) != null)
      ReferenceCountUtil.release(msg); 
  }
  
  private void finishPeerRead0(LocalChannel peer) {
    Future<?> peerFinishReadFuture = peer.finishReadFuture;
    if (peerFinishReadFuture != null) {
      if (!peerFinishReadFuture.isDone()) {
        runFinishPeerReadTask(peer);
        return;
      } 
      FINISH_READ_FUTURE_UPDATER.compareAndSet(peer, peerFinishReadFuture, null);
    } 
    if (peer.readInProgress && !peer.inboundBuffer.isEmpty()) {
      peer.readInProgress = false;
      peer.readInbound();
    } 
  }
  
  private class LocalUnsafe extends AbstractChannel.AbstractUnsafe {
    private LocalUnsafe() {
      super(LocalChannel.this);
    }
    
    public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
      if (!promise.setUncancellable() || !ensureOpen(promise))
        return; 
      if (LocalChannel.this.state == LocalChannel.State.CONNECTED) {
        Exception cause = new AlreadyConnectedException();
        safeSetFailure(promise, cause);
        LocalChannel.this.pipeline().fireExceptionCaught(cause);
        return;
      } 
      if (LocalChannel.this.connectPromise != null)
        throw new ConnectionPendingException(); 
      LocalChannel.this.connectPromise = promise;
      if (LocalChannel.this.state != LocalChannel.State.BOUND)
        if (localAddress == null)
          localAddress = new LocalAddress((Channel)LocalChannel.this);  
      if (localAddress != null)
        try {
          LocalChannel.this.doBind(localAddress);
        } catch (Throwable t) {
          safeSetFailure(promise, t);
          close(voidPromise());
          return;
        }  
      Channel boundChannel = LocalChannelRegistry.get(remoteAddress);
      if (!(boundChannel instanceof LocalServerChannel)) {
        Exception cause = new ConnectException("connection refused: " + remoteAddress);
        safeSetFailure(promise, cause);
        close(voidPromise());
        return;
      } 
      LocalServerChannel serverChannel = (LocalServerChannel)boundChannel;
      LocalChannel.this.peer = serverChannel.serve(LocalChannel.this);
    }
  }
}

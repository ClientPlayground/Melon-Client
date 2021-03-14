package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.ConnectTimeoutException;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.socket.ChannelInputShutdownEvent;
import com.github.steveice10.netty.channel.socket.ChannelInputShutdownReadComplete;
import com.github.steveice10.netty.channel.socket.SocketChannelConfig;
import com.github.steveice10.netty.channel.unix.FileDescriptor;
import com.github.steveice10.netty.channel.unix.UnixChannel;
import com.github.steveice10.netty.channel.unix.UnixChannelUtil;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

abstract class AbstractKQueueChannel extends AbstractChannel implements UnixChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false);
  
  private ChannelPromise connectPromise;
  
  private ScheduledFuture<?> connectTimeoutFuture;
  
  private SocketAddress requestedRemoteAddress;
  
  final BsdSocket socket;
  
  private boolean readFilterEnabled = true;
  
  private boolean writeFilterEnabled;
  
  boolean readReadyRunnablePending;
  
  boolean inputClosedSeenErrorOnRead;
  
  long jniSelfPtr;
  
  protected volatile boolean active;
  
  private volatile SocketAddress local;
  
  private volatile SocketAddress remote;
  
  AbstractKQueueChannel(Channel parent, BsdSocket fd, boolean active) {
    super(parent);
    this.socket = (BsdSocket)ObjectUtil.checkNotNull(fd, "fd");
    this.active = active;
    if (active) {
      this.local = fd.localAddress();
      this.remote = fd.remoteAddress();
    } 
  }
  
  AbstractKQueueChannel(Channel parent, BsdSocket fd, SocketAddress remote) {
    super(parent);
    this.socket = (BsdSocket)ObjectUtil.checkNotNull(fd, "fd");
    this.active = true;
    this.remote = remote;
    this.local = fd.localAddress();
  }
  
  static boolean isSoErrorZero(BsdSocket fd) {
    try {
      return (fd.getSoError() == 0);
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public final FileDescriptor fd() {
    return (FileDescriptor)this.socket;
  }
  
  public boolean isActive() {
    return this.active;
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  protected void doClose() throws Exception {
    this.active = false;
    this.inputClosedSeenErrorOnRead = true;
    try {
      if (isRegistered()) {
        EventLoop loop = eventLoop();
        if (loop.inEventLoop()) {
          doDeregister();
        } else {
          loop.execute(new Runnable() {
                public void run() {
                  try {
                    AbstractKQueueChannel.this.doDeregister();
                  } catch (Throwable cause) {
                    AbstractKQueueChannel.this.pipeline().fireExceptionCaught(cause);
                  } 
                }
              });
        } 
      } 
    } finally {
      this.socket.close();
    } 
  }
  
  protected void doDisconnect() throws Exception {
    doClose();
  }
  
  protected boolean isCompatible(EventLoop loop) {
    return loop instanceof KQueueEventLoop;
  }
  
  public boolean isOpen() {
    return this.socket.isOpen();
  }
  
  protected void doDeregister() throws Exception {
    readFilter(false);
    writeFilter(false);
    evSet0(Native.EVFILT_SOCK, Native.EV_DELETE, 0);
    ((KQueueEventLoop)eventLoop()).remove(this);
    this.readFilterEnabled = true;
  }
  
  protected final void doBeginRead() throws Exception {
    AbstractKQueueUnsafe unsafe = (AbstractKQueueUnsafe)unsafe();
    unsafe.readPending = true;
    readFilter(true);
    if (unsafe.maybeMoreDataToRead)
      unsafe.executeReadReadyRunnable((ChannelConfig)config()); 
  }
  
  protected void doRegister() throws Exception {
    this.readReadyRunnablePending = false;
    if (this.writeFilterEnabled)
      evSet0(Native.EVFILT_WRITE, Native.EV_ADD_CLEAR_ENABLE); 
    if (this.readFilterEnabled)
      evSet0(Native.EVFILT_READ, Native.EV_ADD_CLEAR_ENABLE); 
    evSet0(Native.EVFILT_SOCK, Native.EV_ADD, Native.NOTE_RDHUP);
  }
  
  protected final ByteBuf newDirectBuffer(ByteBuf buf) {
    return newDirectBuffer(buf, buf);
  }
  
  protected final ByteBuf newDirectBuffer(Object holder, ByteBuf buf) {
    int readableBytes = buf.readableBytes();
    if (readableBytes == 0) {
      ReferenceCountUtil.release(holder);
      return Unpooled.EMPTY_BUFFER;
    } 
    ByteBufAllocator alloc = alloc();
    if (alloc.isDirectBufferPooled())
      return newDirectBuffer0(holder, buf, alloc, readableBytes); 
    ByteBuf directBuf = ByteBufUtil.threadLocalDirectBuffer();
    if (directBuf == null)
      return newDirectBuffer0(holder, buf, alloc, readableBytes); 
    directBuf.writeBytes(buf, buf.readerIndex(), readableBytes);
    ReferenceCountUtil.safeRelease(holder);
    return directBuf;
  }
  
  private static ByteBuf newDirectBuffer0(Object holder, ByteBuf buf, ByteBufAllocator alloc, int capacity) {
    ByteBuf directBuf = alloc.directBuffer(capacity);
    directBuf.writeBytes(buf, buf.readerIndex(), capacity);
    ReferenceCountUtil.safeRelease(holder);
    return directBuf;
  }
  
  protected static void checkResolvable(InetSocketAddress addr) {
    if (addr.isUnresolved())
      throw new UnresolvedAddressException(); 
  }
  
  protected final int doReadBytes(ByteBuf byteBuf) throws Exception {
    int localReadAmount, writerIndex = byteBuf.writerIndex();
    unsafe().recvBufAllocHandle().attemptedBytesRead(byteBuf.writableBytes());
    if (byteBuf.hasMemoryAddress()) {
      localReadAmount = this.socket.readAddress(byteBuf.memoryAddress(), writerIndex, byteBuf.capacity());
    } else {
      ByteBuffer buf = byteBuf.internalNioBuffer(writerIndex, byteBuf.writableBytes());
      localReadAmount = this.socket.read(buf, buf.position(), buf.limit());
    } 
    if (localReadAmount > 0)
      byteBuf.writerIndex(writerIndex + localReadAmount); 
    return localReadAmount;
  }
  
  protected final int doWriteBytes(ChannelOutboundBuffer in, ByteBuf buf) throws Exception {
    if (buf.hasMemoryAddress()) {
      int localFlushedAmount = this.socket.writeAddress(buf.memoryAddress(), buf.readerIndex(), buf.writerIndex());
      if (localFlushedAmount > 0) {
        in.removeBytes(localFlushedAmount);
        return 1;
      } 
    } else {
      ByteBuffer nioBuf = (buf.nioBufferCount() == 1) ? buf.internalNioBuffer(buf.readerIndex(), buf.readableBytes()) : buf.nioBuffer();
      int localFlushedAmount = this.socket.write(nioBuf, nioBuf.position(), nioBuf.limit());
      if (localFlushedAmount > 0) {
        nioBuf.position(nioBuf.position() + localFlushedAmount);
        in.removeBytes(localFlushedAmount);
        return 1;
      } 
    } 
    return Integer.MAX_VALUE;
  }
  
  final boolean shouldBreakReadReady(ChannelConfig config) {
    return (this.socket.isInputShutdown() && (this.inputClosedSeenErrorOnRead || !isAllowHalfClosure(config)));
  }
  
  private static boolean isAllowHalfClosure(ChannelConfig config) {
    return (config instanceof SocketChannelConfig && ((SocketChannelConfig)config)
      .isAllowHalfClosure());
  }
  
  final void clearReadFilter() {
    if (isRegistered()) {
      EventLoop loop = eventLoop();
      final AbstractKQueueUnsafe unsafe = (AbstractKQueueUnsafe)unsafe();
      if (loop.inEventLoop()) {
        unsafe.clearReadFilter0();
      } else {
        loop.execute(new Runnable() {
              public void run() {
                if (!unsafe.readPending && !AbstractKQueueChannel.this.config().isAutoRead())
                  unsafe.clearReadFilter0(); 
              }
            });
      } 
    } else {
      this.readFilterEnabled = false;
    } 
  }
  
  void readFilter(boolean readFilterEnabled) throws IOException {
    if (this.readFilterEnabled != readFilterEnabled) {
      this.readFilterEnabled = readFilterEnabled;
      evSet(Native.EVFILT_READ, readFilterEnabled ? Native.EV_ADD_CLEAR_ENABLE : Native.EV_DELETE_DISABLE);
    } 
  }
  
  void writeFilter(boolean writeFilterEnabled) throws IOException {
    if (this.writeFilterEnabled != writeFilterEnabled) {
      this.writeFilterEnabled = writeFilterEnabled;
      evSet(Native.EVFILT_WRITE, writeFilterEnabled ? Native.EV_ADD_CLEAR_ENABLE : Native.EV_DELETE_DISABLE);
    } 
  }
  
  private void evSet(short filter, short flags) {
    if (isOpen() && isRegistered())
      evSet0(filter, flags); 
  }
  
  private void evSet0(short filter, short flags) {
    evSet0(filter, flags, 0);
  }
  
  private void evSet0(short filter, short flags, int fflags) {
    ((KQueueEventLoop)eventLoop()).evSet(this, filter, flags, fflags);
  }
  
  abstract class AbstractKQueueUnsafe extends AbstractChannel.AbstractUnsafe {
    boolean readPending;
    
    boolean maybeMoreDataToRead;
    
    private KQueueRecvByteAllocatorHandle allocHandle;
    
    private final Runnable readReadyRunnable;
    
    AbstractKQueueUnsafe() {
      super(AbstractKQueueChannel.this);
      this.readReadyRunnable = new Runnable() {
          public void run() {
            AbstractKQueueChannel.this.readReadyRunnablePending = false;
            AbstractKQueueChannel.AbstractKQueueUnsafe.this.readReady(AbstractKQueueChannel.AbstractKQueueUnsafe.this.recvBufAllocHandle());
          }
        };
    }
    
    final void readReady(long numberBytesPending) {
      KQueueRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
      allocHandle.numberBytesPending(numberBytesPending);
      readReady(allocHandle);
    }
    
    final void readReadyBefore() {
      this.maybeMoreDataToRead = false;
    }
    
    final void readReadyFinally(ChannelConfig config) {
      this.maybeMoreDataToRead = this.allocHandle.maybeMoreDataToRead();
      if (!this.readPending && !config.isAutoRead()) {
        clearReadFilter0();
      } else if (this.readPending && this.maybeMoreDataToRead) {
        executeReadReadyRunnable(config);
      } 
    }
    
    final boolean failConnectPromise(Throwable cause) {
      if (AbstractKQueueChannel.this.connectPromise != null) {
        ChannelPromise connectPromise = AbstractKQueueChannel.this.connectPromise;
        AbstractKQueueChannel.this.connectPromise = null;
        if (connectPromise.tryFailure((cause instanceof ConnectException) ? cause : (new ConnectException("failed to connect"))
            .initCause(cause))) {
          closeIfClosed();
          return true;
        } 
      } 
      return false;
    }
    
    final void writeReady() {
      if (AbstractKQueueChannel.this.connectPromise != null) {
        finishConnect();
      } else if (!AbstractKQueueChannel.this.socket.isOutputShutdown()) {
        super.flush0();
      } 
    }
    
    void shutdownInput(boolean readEOF) {
      if (readEOF && AbstractKQueueChannel.this.connectPromise != null)
        finishConnect(); 
      if (!AbstractKQueueChannel.this.socket.isInputShutdown()) {
        if (AbstractKQueueChannel.isAllowHalfClosure((ChannelConfig)AbstractKQueueChannel.this.config())) {
          try {
            AbstractKQueueChannel.this.socket.shutdown(true, false);
          } catch (IOException ignored) {
            fireEventAndClose(ChannelInputShutdownEvent.INSTANCE);
            return;
          } catch (NotYetConnectedException notYetConnectedException) {}
          AbstractKQueueChannel.this.pipeline().fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
        } else {
          close(voidPromise());
        } 
      } else if (!readEOF) {
        AbstractKQueueChannel.this.inputClosedSeenErrorOnRead = true;
        AbstractKQueueChannel.this.pipeline().fireUserEventTriggered(ChannelInputShutdownReadComplete.INSTANCE);
      } 
    }
    
    final void readEOF() {
      KQueueRecvByteAllocatorHandle allocHandle = recvBufAllocHandle();
      allocHandle.readEOF();
      if (AbstractKQueueChannel.this.isActive()) {
        readReady(allocHandle);
      } else {
        shutdownInput(true);
      } 
    }
    
    public KQueueRecvByteAllocatorHandle recvBufAllocHandle() {
      if (this.allocHandle == null)
        this
          .allocHandle = new KQueueRecvByteAllocatorHandle((RecvByteBufAllocator.ExtendedHandle)super.recvBufAllocHandle()); 
      return this.allocHandle;
    }
    
    protected final void flush0() {
      if (!AbstractKQueueChannel.this.writeFilterEnabled)
        super.flush0(); 
    }
    
    final void executeReadReadyRunnable(ChannelConfig config) {
      if (AbstractKQueueChannel.this.readReadyRunnablePending || !AbstractKQueueChannel.this.isActive() || AbstractKQueueChannel.this.shouldBreakReadReady(config))
        return; 
      AbstractKQueueChannel.this.readReadyRunnablePending = true;
      AbstractKQueueChannel.this.eventLoop().execute(this.readReadyRunnable);
    }
    
    protected final void clearReadFilter0() {
      assert AbstractKQueueChannel.this.eventLoop().inEventLoop();
      try {
        this.readPending = false;
        AbstractKQueueChannel.this.readFilter(false);
      } catch (IOException e) {
        AbstractKQueueChannel.this.pipeline().fireExceptionCaught(e);
        AbstractKQueueChannel.this.unsafe().close(AbstractKQueueChannel.this.unsafe().voidPromise());
      } 
    }
    
    private void fireEventAndClose(Object evt) {
      AbstractKQueueChannel.this.pipeline().fireUserEventTriggered(evt);
      close(voidPromise());
    }
    
    public void connect(final SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
      if (!promise.setUncancellable() || !ensureOpen(promise))
        return; 
      try {
        if (AbstractKQueueChannel.this.connectPromise != null)
          throw new ConnectionPendingException(); 
        boolean wasActive = AbstractKQueueChannel.this.isActive();
        if (AbstractKQueueChannel.this.doConnect(remoteAddress, localAddress)) {
          fulfillConnectPromise(promise, wasActive);
        } else {
          AbstractKQueueChannel.this.connectPromise = promise;
          AbstractKQueueChannel.this.requestedRemoteAddress = remoteAddress;
          int connectTimeoutMillis = AbstractKQueueChannel.this.config().getConnectTimeoutMillis();
          if (connectTimeoutMillis > 0)
            AbstractKQueueChannel.this.connectTimeoutFuture = (ScheduledFuture<?>)AbstractKQueueChannel.this.eventLoop().schedule(new Runnable() {
                  public void run() {
                    ChannelPromise connectPromise = AbstractKQueueChannel.this.connectPromise;
                    ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
                    if (connectPromise != null && connectPromise.tryFailure((Throwable)cause))
                      AbstractKQueueChannel.AbstractKQueueUnsafe.this.close(AbstractKQueueChannel.AbstractKQueueUnsafe.this.voidPromise()); 
                  }
                },  connectTimeoutMillis, TimeUnit.MILLISECONDS); 
          promise.addListener((GenericFutureListener)new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                  if (future.isCancelled()) {
                    if (AbstractKQueueChannel.this.connectTimeoutFuture != null)
                      AbstractKQueueChannel.this.connectTimeoutFuture.cancel(false); 
                    AbstractKQueueChannel.this.connectPromise = null;
                    AbstractKQueueChannel.AbstractKQueueUnsafe.this.close(AbstractKQueueChannel.AbstractKQueueUnsafe.this.voidPromise());
                  } 
                }
              });
        } 
      } catch (Throwable t) {
        closeIfClosed();
        promise.tryFailure(annotateConnectException(t, remoteAddress));
      } 
    }
    
    private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive) {
      if (promise == null)
        return; 
      AbstractKQueueChannel.this.active = true;
      boolean active = AbstractKQueueChannel.this.isActive();
      boolean promiseSet = promise.trySuccess();
      if (!wasActive && active)
        AbstractKQueueChannel.this.pipeline().fireChannelActive(); 
      if (!promiseSet)
        close(voidPromise()); 
    }
    
    private void fulfillConnectPromise(ChannelPromise promise, Throwable cause) {
      if (promise == null)
        return; 
      promise.tryFailure(cause);
      closeIfClosed();
    }
    
    private void finishConnect() {
      assert AbstractKQueueChannel.this.eventLoop().inEventLoop();
      boolean connectStillInProgress = false;
      try {
        boolean wasActive = AbstractKQueueChannel.this.isActive();
        if (!doFinishConnect()) {
          connectStillInProgress = true;
          return;
        } 
        fulfillConnectPromise(AbstractKQueueChannel.this.connectPromise, wasActive);
      } catch (Throwable t) {
        fulfillConnectPromise(AbstractKQueueChannel.this.connectPromise, annotateConnectException(t, AbstractKQueueChannel.this.requestedRemoteAddress));
      } finally {
        if (!connectStillInProgress) {
          if (AbstractKQueueChannel.this.connectTimeoutFuture != null)
            AbstractKQueueChannel.this.connectTimeoutFuture.cancel(false); 
          AbstractKQueueChannel.this.connectPromise = null;
        } 
      } 
    }
    
    private boolean doFinishConnect() throws Exception {
      if (AbstractKQueueChannel.this.socket.finishConnect()) {
        AbstractKQueueChannel.this.writeFilter(false);
        if (AbstractKQueueChannel.this.requestedRemoteAddress instanceof InetSocketAddress)
          AbstractKQueueChannel.this.remote = UnixChannelUtil.computeRemoteAddr((InetSocketAddress)AbstractKQueueChannel.this.requestedRemoteAddress, AbstractKQueueChannel.this.socket.remoteAddress()); 
        AbstractKQueueChannel.this.requestedRemoteAddress = null;
        return true;
      } 
      AbstractKQueueChannel.this.writeFilter(true);
      return false;
    }
    
    abstract void readReady(KQueueRecvByteAllocatorHandle param1KQueueRecvByteAllocatorHandle);
  }
  
  protected void doBind(SocketAddress local) throws Exception {
    if (local instanceof InetSocketAddress)
      checkResolvable((InetSocketAddress)local); 
    this.socket.bind(local);
    this.local = this.socket.localAddress();
  }
  
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    if (localAddress instanceof InetSocketAddress)
      checkResolvable((InetSocketAddress)localAddress); 
    InetSocketAddress remoteSocketAddr = (remoteAddress instanceof InetSocketAddress) ? (InetSocketAddress)remoteAddress : null;
    if (remoteSocketAddr != null)
      checkResolvable(remoteSocketAddr); 
    if (this.remote != null)
      throw new AlreadyConnectedException(); 
    if (localAddress != null)
      this.socket.bind(localAddress); 
    boolean connected = doConnect0(remoteAddress);
    if (connected)
      this
        .remote = (remoteSocketAddr == null) ? remoteAddress : UnixChannelUtil.computeRemoteAddr(remoteSocketAddr, this.socket.remoteAddress()); 
    this.local = this.socket.localAddress();
    return connected;
  }
  
  private boolean doConnect0(SocketAddress remote) throws Exception {
    boolean success = false;
    try {
      boolean connected = this.socket.connect(remote);
      if (!connected)
        writeFilter(true); 
      success = true;
      return connected;
    } finally {
      if (!success)
        doClose(); 
    } 
  }
  
  protected SocketAddress localAddress0() {
    return this.local;
  }
  
  protected SocketAddress remoteAddress0() {
    return this.remote;
  }
  
  protected abstract AbstractKQueueUnsafe newUnsafe();
  
  public abstract KQueueChannelConfig config();
}

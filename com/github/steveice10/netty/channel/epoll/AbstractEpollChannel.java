package com.github.steveice10.netty.channel.epoll;

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
import com.github.steveice10.netty.channel.unix.Socket;
import com.github.steveice10.netty.channel.unix.UnixChannel;
import com.github.steveice10.netty.channel.unix.UnixChannelUtil;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

abstract class AbstractEpollChannel extends AbstractChannel implements UnixChannel {
  private static final ClosedChannelException DO_CLOSE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), AbstractEpollChannel.class, "doClose()");
  
  private static final ChannelMetadata METADATA = new ChannelMetadata(false);
  
  private final int readFlag;
  
  final LinuxSocket socket;
  
  private ChannelPromise connectPromise;
  
  private ScheduledFuture<?> connectTimeoutFuture;
  
  private SocketAddress requestedRemoteAddress;
  
  private volatile SocketAddress local;
  
  private volatile SocketAddress remote;
  
  protected int flags = Native.EPOLLET;
  
  boolean inputClosedSeenErrorOnRead;
  
  boolean epollInReadyRunnablePending;
  
  protected volatile boolean active;
  
  AbstractEpollChannel(LinuxSocket fd, int flag) {
    this((Channel)null, fd, flag, false);
  }
  
  AbstractEpollChannel(Channel parent, LinuxSocket fd, int flag, boolean active) {
    super(parent);
    this.socket = (LinuxSocket)ObjectUtil.checkNotNull(fd, "fd");
    this.readFlag = flag;
    this.flags |= flag;
    this.active = active;
    if (active) {
      this.local = fd.localAddress();
      this.remote = fd.remoteAddress();
    } 
  }
  
  AbstractEpollChannel(Channel parent, LinuxSocket fd, int flag, SocketAddress remote) {
    super(parent);
    this.socket = (LinuxSocket)ObjectUtil.checkNotNull(fd, "fd");
    this.readFlag = flag;
    this.flags |= flag;
    this.active = true;
    this.remote = remote;
    this.local = fd.localAddress();
  }
  
  static boolean isSoErrorZero(Socket fd) {
    try {
      return (fd.getSoError() == 0);
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  void setFlag(int flag) throws IOException {
    if (!isFlagSet(flag)) {
      this.flags |= flag;
      modifyEvents();
    } 
  }
  
  void clearFlag(int flag) throws IOException {
    if (isFlagSet(flag)) {
      this.flags &= flag ^ 0xFFFFFFFF;
      modifyEvents();
    } 
  }
  
  boolean isFlagSet(int flag) {
    return ((this.flags & flag) != 0);
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
      ChannelPromise promise = this.connectPromise;
      if (promise != null) {
        promise.tryFailure(DO_CLOSE_CLOSED_CHANNEL_EXCEPTION);
        this.connectPromise = null;
      } 
      ScheduledFuture<?> future = this.connectTimeoutFuture;
      if (future != null) {
        future.cancel(false);
        this.connectTimeoutFuture = null;
      } 
      if (isRegistered()) {
        EventLoop loop = eventLoop();
        if (loop.inEventLoop()) {
          doDeregister();
        } else {
          loop.execute(new Runnable() {
                public void run() {
                  try {
                    AbstractEpollChannel.this.doDeregister();
                  } catch (Throwable cause) {
                    AbstractEpollChannel.this.pipeline().fireExceptionCaught(cause);
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
    return loop instanceof EpollEventLoop;
  }
  
  public boolean isOpen() {
    return this.socket.isOpen();
  }
  
  protected void doDeregister() throws Exception {
    ((EpollEventLoop)eventLoop()).remove(this);
  }
  
  protected final void doBeginRead() throws Exception {
    AbstractEpollUnsafe unsafe = (AbstractEpollUnsafe)unsafe();
    unsafe.readPending = true;
    setFlag(this.readFlag);
    if (unsafe.maybeMoreDataToRead)
      unsafe.executeEpollInReadyRunnable((ChannelConfig)config()); 
  }
  
  final boolean shouldBreakEpollInReady(ChannelConfig config) {
    return (this.socket.isInputShutdown() && (this.inputClosedSeenErrorOnRead || !isAllowHalfClosure(config)));
  }
  
  private static boolean isAllowHalfClosure(ChannelConfig config) {
    return (config instanceof SocketChannelConfig && ((SocketChannelConfig)config)
      .isAllowHalfClosure());
  }
  
  final void clearEpollIn() {
    if (isRegistered()) {
      EventLoop loop = eventLoop();
      final AbstractEpollUnsafe unsafe = (AbstractEpollUnsafe)unsafe();
      if (loop.inEventLoop()) {
        unsafe.clearEpollIn0();
      } else {
        loop.execute(new Runnable() {
              public void run() {
                if (!unsafe.readPending && !AbstractEpollChannel.this.config().isAutoRead())
                  unsafe.clearEpollIn0(); 
              }
            });
      } 
    } else {
      this.flags &= this.readFlag ^ 0xFFFFFFFF;
    } 
  }
  
  private void modifyEvents() throws IOException {
    if (isOpen() && isRegistered())
      ((EpollEventLoop)eventLoop()).modify(this); 
  }
  
  protected void doRegister() throws Exception {
    this.epollInReadyRunnablePending = false;
    ((EpollEventLoop)eventLoop()).add(this);
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
  
  protected abstract class AbstractEpollUnsafe extends AbstractChannel.AbstractUnsafe {
    boolean readPending;
    
    boolean maybeMoreDataToRead;
    
    private EpollRecvByteAllocatorHandle allocHandle;
    
    private final Runnable epollInReadyRunnable;
    
    protected AbstractEpollUnsafe() {
      super(AbstractEpollChannel.this);
      this.epollInReadyRunnable = new Runnable() {
          public void run() {
            AbstractEpollChannel.this.epollInReadyRunnablePending = false;
            AbstractEpollChannel.AbstractEpollUnsafe.this.epollInReady();
          }
        };
    }
    
    final void epollInBefore() {
      this.maybeMoreDataToRead = false;
    }
    
    final void epollInFinally(ChannelConfig config) {
      this.maybeMoreDataToRead = (this.allocHandle.isEdgeTriggered() && this.allocHandle.maybeMoreDataToRead());
      if (!this.readPending && !config.isAutoRead()) {
        AbstractEpollChannel.this.clearEpollIn();
      } else if (this.readPending && this.maybeMoreDataToRead) {
        executeEpollInReadyRunnable(config);
      } 
    }
    
    final void executeEpollInReadyRunnable(ChannelConfig config) {
      if (AbstractEpollChannel.this.epollInReadyRunnablePending || !AbstractEpollChannel.this.isActive() || AbstractEpollChannel.this.shouldBreakEpollInReady(config))
        return; 
      AbstractEpollChannel.this.epollInReadyRunnablePending = true;
      AbstractEpollChannel.this.eventLoop().execute(this.epollInReadyRunnable);
    }
    
    final void epollRdHupReady() {
      recvBufAllocHandle().receivedRdHup();
      if (AbstractEpollChannel.this.isActive()) {
        epollInReady();
      } else {
        shutdownInput(true);
      } 
      clearEpollRdHup();
    }
    
    private void clearEpollRdHup() {
      try {
        AbstractEpollChannel.this.clearFlag(Native.EPOLLRDHUP);
      } catch (IOException e) {
        AbstractEpollChannel.this.pipeline().fireExceptionCaught(e);
        close(voidPromise());
      } 
    }
    
    void shutdownInput(boolean rdHup) {
      if (!AbstractEpollChannel.this.socket.isInputShutdown()) {
        if (AbstractEpollChannel.isAllowHalfClosure((ChannelConfig)AbstractEpollChannel.this.config())) {
          try {
            AbstractEpollChannel.this.socket.shutdown(true, false);
          } catch (IOException ignored) {
            fireEventAndClose(ChannelInputShutdownEvent.INSTANCE);
            return;
          } catch (NotYetConnectedException notYetConnectedException) {}
          AbstractEpollChannel.this.clearEpollIn();
          AbstractEpollChannel.this.pipeline().fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
        } else {
          close(voidPromise());
        } 
      } else if (!rdHup) {
        AbstractEpollChannel.this.inputClosedSeenErrorOnRead = true;
        AbstractEpollChannel.this.pipeline().fireUserEventTriggered(ChannelInputShutdownReadComplete.INSTANCE);
      } 
    }
    
    private void fireEventAndClose(Object evt) {
      AbstractEpollChannel.this.pipeline().fireUserEventTriggered(evt);
      close(voidPromise());
    }
    
    public EpollRecvByteAllocatorHandle recvBufAllocHandle() {
      if (this.allocHandle == null)
        this.allocHandle = newEpollHandle((RecvByteBufAllocator.ExtendedHandle)super.recvBufAllocHandle()); 
      return this.allocHandle;
    }
    
    EpollRecvByteAllocatorHandle newEpollHandle(RecvByteBufAllocator.ExtendedHandle handle) {
      return new EpollRecvByteAllocatorHandle(handle);
    }
    
    protected final void flush0() {
      if (!AbstractEpollChannel.this.isFlagSet(Native.EPOLLOUT))
        super.flush0(); 
    }
    
    final void epollOutReady() {
      if (AbstractEpollChannel.this.connectPromise != null) {
        finishConnect();
      } else if (!AbstractEpollChannel.this.socket.isOutputShutdown()) {
        super.flush0();
      } 
    }
    
    protected final void clearEpollIn0() {
      assert AbstractEpollChannel.this.eventLoop().inEventLoop();
      try {
        this.readPending = false;
        AbstractEpollChannel.this.clearFlag(AbstractEpollChannel.this.readFlag);
      } catch (IOException e) {
        AbstractEpollChannel.this.pipeline().fireExceptionCaught(e);
        AbstractEpollChannel.this.unsafe().close(AbstractEpollChannel.this.unsafe().voidPromise());
      } 
    }
    
    public void connect(final SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
      if (!promise.setUncancellable() || !ensureOpen(promise))
        return; 
      try {
        if (AbstractEpollChannel.this.connectPromise != null)
          throw new ConnectionPendingException(); 
        boolean wasActive = AbstractEpollChannel.this.isActive();
        if (AbstractEpollChannel.this.doConnect(remoteAddress, localAddress)) {
          fulfillConnectPromise(promise, wasActive);
        } else {
          AbstractEpollChannel.this.connectPromise = promise;
          AbstractEpollChannel.this.requestedRemoteAddress = remoteAddress;
          int connectTimeoutMillis = AbstractEpollChannel.this.config().getConnectTimeoutMillis();
          if (connectTimeoutMillis > 0)
            AbstractEpollChannel.this.connectTimeoutFuture = (ScheduledFuture<?>)AbstractEpollChannel.this.eventLoop().schedule(new Runnable() {
                  public void run() {
                    ChannelPromise connectPromise = AbstractEpollChannel.this.connectPromise;
                    ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
                    if (connectPromise != null && connectPromise.tryFailure((Throwable)cause))
                      AbstractEpollChannel.AbstractEpollUnsafe.this.close(AbstractEpollChannel.AbstractEpollUnsafe.this.voidPromise()); 
                  }
                },  connectTimeoutMillis, TimeUnit.MILLISECONDS); 
          promise.addListener((GenericFutureListener)new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                  if (future.isCancelled()) {
                    if (AbstractEpollChannel.this.connectTimeoutFuture != null)
                      AbstractEpollChannel.this.connectTimeoutFuture.cancel(false); 
                    AbstractEpollChannel.this.connectPromise = null;
                    AbstractEpollChannel.AbstractEpollUnsafe.this.close(AbstractEpollChannel.AbstractEpollUnsafe.this.voidPromise());
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
      AbstractEpollChannel.this.active = true;
      boolean active = AbstractEpollChannel.this.isActive();
      boolean promiseSet = promise.trySuccess();
      if (!wasActive && active)
        AbstractEpollChannel.this.pipeline().fireChannelActive(); 
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
      assert AbstractEpollChannel.this.eventLoop().inEventLoop();
      boolean connectStillInProgress = false;
      try {
        boolean wasActive = AbstractEpollChannel.this.isActive();
        if (!doFinishConnect()) {
          connectStillInProgress = true;
          return;
        } 
        fulfillConnectPromise(AbstractEpollChannel.this.connectPromise, wasActive);
      } catch (Throwable t) {
        fulfillConnectPromise(AbstractEpollChannel.this.connectPromise, annotateConnectException(t, AbstractEpollChannel.this.requestedRemoteAddress));
      } finally {
        if (!connectStillInProgress) {
          if (AbstractEpollChannel.this.connectTimeoutFuture != null)
            AbstractEpollChannel.this.connectTimeoutFuture.cancel(false); 
          AbstractEpollChannel.this.connectPromise = null;
        } 
      } 
    }
    
    private boolean doFinishConnect() throws Exception {
      if (AbstractEpollChannel.this.socket.finishConnect()) {
        AbstractEpollChannel.this.clearFlag(Native.EPOLLOUT);
        if (AbstractEpollChannel.this.requestedRemoteAddress instanceof InetSocketAddress)
          AbstractEpollChannel.this.remote = UnixChannelUtil.computeRemoteAddr((InetSocketAddress)AbstractEpollChannel.this.requestedRemoteAddress, AbstractEpollChannel.this.socket.remoteAddress()); 
        AbstractEpollChannel.this.requestedRemoteAddress = null;
        return true;
      } 
      AbstractEpollChannel.this.setFlag(Native.EPOLLOUT);
      return false;
    }
    
    abstract void epollInReady();
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
        setFlag(Native.EPOLLOUT); 
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
  
  public abstract EpollChannelConfig config();
  
  protected abstract AbstractEpollUnsafe newUnsafe();
}

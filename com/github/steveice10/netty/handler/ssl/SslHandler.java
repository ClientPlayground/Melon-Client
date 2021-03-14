package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.buffer.CompositeByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.AbstractCoalescingBufferQueue;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelOutboundHandler;
import com.github.steveice10.netty.channel.ChannelOutboundInvoker;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.ChannelPromiseNotifier;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import com.github.steveice10.netty.handler.codec.UnsupportedMessageTypeException;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.concurrent.DefaultPromise;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.FutureListener;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.ImmediateExecutor;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.concurrent.ScheduledFuture;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

public class SslHandler extends ByteToMessageDecoder implements ChannelOutboundHandler {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(SslHandler.class);
  
  private static final Pattern IGNORABLE_CLASS_IN_STACK = Pattern.compile("^.*(?:Socket|Datagram|Sctp|Udt)Channel.*$");
  
  private static final Pattern IGNORABLE_ERROR_MESSAGE = Pattern.compile("^.*(?:connection.*(?:reset|closed|abort|broken)|broken.*pipe).*$", 2);
  
  private static final SSLException SSLENGINE_CLOSED = (SSLException)ThrowableUtil.unknownStackTrace(new SSLException("SSLEngine closed already"), SslHandler.class, "wrap(...)");
  
  private static final SSLException HANDSHAKE_TIMED_OUT = (SSLException)ThrowableUtil.unknownStackTrace(new SSLException("handshake timed out"), SslHandler.class, "handshake(...)");
  
  private static final ClosedChannelException CHANNEL_CLOSED = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), SslHandler.class, "channelInactive(...)");
  
  private static final int MAX_PLAINTEXT_LENGTH = 16384;
  
  private volatile ChannelHandlerContext ctx;
  
  private final SSLEngine engine;
  
  private final SslEngineType engineType;
  
  private final Executor delegatedTaskExecutor;
  
  private final boolean jdkCompatibilityMode;
  
  private enum SslEngineType {
    TCNATIVE(true, ByteToMessageDecoder.COMPOSITE_CUMULATOR) {
      SSLEngineResult unwrap(SslHandler handler, ByteBuf in, int readerIndex, int len, ByteBuf out) throws SSLException {
        SSLEngineResult result;
        int nioBufferCount = in.nioBufferCount();
        int writerIndex = out.writerIndex();
        if (nioBufferCount > 1) {
          ReferenceCountedOpenSslEngine opensslEngine = (ReferenceCountedOpenSslEngine)handler.engine;
          try {
            handler.singleBuffer[0] = SslHandler.toByteBuffer(out, writerIndex, out
                .writableBytes());
            result = opensslEngine.unwrap(in.nioBuffers(readerIndex, len), handler.singleBuffer);
          } finally {
            handler.singleBuffer[0] = null;
          } 
        } else {
          result = handler.engine.unwrap(SslHandler.toByteBuffer(in, readerIndex, len), SslHandler
              .toByteBuffer(out, writerIndex, out.writableBytes()));
        } 
        out.writerIndex(writerIndex + result.bytesProduced());
        return result;
      }
      
      int getPacketBufferSize(SslHandler handler) {
        return ((ReferenceCountedOpenSslEngine)handler.engine).maxEncryptedPacketLength0();
      }
      
      int calculateWrapBufferCapacity(SslHandler handler, int pendingBytes, int numComponents) {
        return ((ReferenceCountedOpenSslEngine)handler.engine).calculateMaxLengthForWrap(pendingBytes, numComponents);
      }
      
      int calculatePendingData(SslHandler handler, int guess) {
        int sslPending = ((ReferenceCountedOpenSslEngine)handler.engine).sslPending();
        return (sslPending > 0) ? sslPending : guess;
      }
      
      boolean jdkCompatibilityMode(SSLEngine engine) {
        return ((ReferenceCountedOpenSslEngine)engine).jdkCompatibilityMode;
      }
    },
    CONSCRYPT(true, ByteToMessageDecoder.COMPOSITE_CUMULATOR) {
      SSLEngineResult unwrap(SslHandler handler, ByteBuf in, int readerIndex, int len, ByteBuf out) throws SSLException {
        SSLEngineResult result;
        int nioBufferCount = in.nioBufferCount();
        int writerIndex = out.writerIndex();
        if (nioBufferCount > 1) {
          try {
            handler.singleBuffer[0] = SslHandler.toByteBuffer(out, writerIndex, out.writableBytes());
            result = ((ConscryptAlpnSslEngine)handler.engine).unwrap(in
                .nioBuffers(readerIndex, len), handler
                .singleBuffer);
          } finally {
            handler.singleBuffer[0] = null;
          } 
        } else {
          result = handler.engine.unwrap(SslHandler.toByteBuffer(in, readerIndex, len), SslHandler
              .toByteBuffer(out, writerIndex, out.writableBytes()));
        } 
        out.writerIndex(writerIndex + result.bytesProduced());
        return result;
      }
      
      int calculateWrapBufferCapacity(SslHandler handler, int pendingBytes, int numComponents) {
        return ((ConscryptAlpnSslEngine)handler.engine).calculateOutNetBufSize(pendingBytes, numComponents);
      }
      
      int calculatePendingData(SslHandler handler, int guess) {
        return guess;
      }
      
      boolean jdkCompatibilityMode(SSLEngine engine) {
        return true;
      }
    },
    JDK(false, ByteToMessageDecoder.MERGE_CUMULATOR) {
      SSLEngineResult unwrap(SslHandler handler, ByteBuf in, int readerIndex, int len, ByteBuf out) throws SSLException {
        int writerIndex = out.writerIndex();
        ByteBuffer inNioBuffer = SslHandler.toByteBuffer(in, readerIndex, len);
        int position = inNioBuffer.position();
        SSLEngineResult result = handler.engine.unwrap(inNioBuffer, SslHandler
            .toByteBuffer(out, writerIndex, out.writableBytes()));
        out.writerIndex(writerIndex + result.bytesProduced());
        if (result.bytesConsumed() == 0) {
          int consumed = inNioBuffer.position() - position;
          if (consumed != result.bytesConsumed())
            return new SSLEngineResult(result
                .getStatus(), result.getHandshakeStatus(), consumed, result.bytesProduced()); 
        } 
        return result;
      }
      
      int calculateWrapBufferCapacity(SslHandler handler, int pendingBytes, int numComponents) {
        return handler.engine.getSession().getPacketBufferSize();
      }
      
      int calculatePendingData(SslHandler handler, int guess) {
        return guess;
      }
      
      boolean jdkCompatibilityMode(SSLEngine engine) {
        return true;
      }
    };
    
    final boolean wantsDirectBuffer;
    
    final ByteToMessageDecoder.Cumulator cumulator;
    
    static SslEngineType forEngine(SSLEngine engine) {
      return (engine instanceof ReferenceCountedOpenSslEngine) ? TCNATIVE : ((engine instanceof ConscryptAlpnSslEngine) ? CONSCRYPT : JDK);
    }
    
    SslEngineType(boolean wantsDirectBuffer, ByteToMessageDecoder.Cumulator cumulator) {
      this.wantsDirectBuffer = wantsDirectBuffer;
      this.cumulator = cumulator;
    }
    
    int getPacketBufferSize(SslHandler handler) {
      return handler.engine.getSession().getPacketBufferSize();
    }
    
    abstract SSLEngineResult unwrap(SslHandler param1SslHandler, ByteBuf param1ByteBuf1, int param1Int1, int param1Int2, ByteBuf param1ByteBuf2) throws SSLException;
    
    abstract int calculateWrapBufferCapacity(SslHandler param1SslHandler, int param1Int1, int param1Int2);
    
    abstract int calculatePendingData(SslHandler param1SslHandler, int param1Int);
    
    abstract boolean jdkCompatibilityMode(SSLEngine param1SSLEngine);
  }
  
  private final ByteBuffer[] singleBuffer = new ByteBuffer[1];
  
  private final boolean startTls;
  
  private boolean sentFirstMessage;
  
  private boolean flushedBeforeHandshake;
  
  private boolean readDuringHandshake;
  
  private boolean handshakeStarted;
  
  private SslHandlerCoalescingBufferQueue pendingUnencryptedWrites;
  
  private Promise<Channel> handshakePromise = (Promise<Channel>)new LazyChannelPromise();
  
  private final LazyChannelPromise sslClosePromise = new LazyChannelPromise();
  
  private boolean needsFlush;
  
  private boolean outboundClosed;
  
  private boolean closeNotify;
  
  private int packetLength;
  
  private boolean firedChannelRead;
  
  private volatile long handshakeTimeoutMillis = 10000L;
  
  private volatile long closeNotifyFlushTimeoutMillis = 3000L;
  
  private volatile long closeNotifyReadTimeoutMillis;
  
  volatile int wrapDataSize = 16384;
  
  public SslHandler(SSLEngine engine) {
    this(engine, false);
  }
  
  public SslHandler(SSLEngine engine, boolean startTls) {
    this(engine, startTls, (Executor)ImmediateExecutor.INSTANCE);
  }
  
  @Deprecated
  public SslHandler(SSLEngine engine, Executor delegatedTaskExecutor) {
    this(engine, false, delegatedTaskExecutor);
  }
  
  @Deprecated
  public SslHandler(SSLEngine engine, boolean startTls, Executor delegatedTaskExecutor) {
    if (engine == null)
      throw new NullPointerException("engine"); 
    if (delegatedTaskExecutor == null)
      throw new NullPointerException("delegatedTaskExecutor"); 
    this.engine = engine;
    this.engineType = SslEngineType.forEngine(engine);
    this.delegatedTaskExecutor = delegatedTaskExecutor;
    this.startTls = startTls;
    this.jdkCompatibilityMode = this.engineType.jdkCompatibilityMode(engine);
    setCumulator(this.engineType.cumulator);
  }
  
  public long getHandshakeTimeoutMillis() {
    return this.handshakeTimeoutMillis;
  }
  
  public void setHandshakeTimeout(long handshakeTimeout, TimeUnit unit) {
    if (unit == null)
      throw new NullPointerException("unit"); 
    setHandshakeTimeoutMillis(unit.toMillis(handshakeTimeout));
  }
  
  public void setHandshakeTimeoutMillis(long handshakeTimeoutMillis) {
    if (handshakeTimeoutMillis < 0L)
      throw new IllegalArgumentException("handshakeTimeoutMillis: " + handshakeTimeoutMillis + " (expected: >= 0)"); 
    this.handshakeTimeoutMillis = handshakeTimeoutMillis;
  }
  
  public final void setWrapDataSize(int wrapDataSize) {
    this.wrapDataSize = wrapDataSize;
  }
  
  @Deprecated
  public long getCloseNotifyTimeoutMillis() {
    return getCloseNotifyFlushTimeoutMillis();
  }
  
  @Deprecated
  public void setCloseNotifyTimeout(long closeNotifyTimeout, TimeUnit unit) {
    setCloseNotifyFlushTimeout(closeNotifyTimeout, unit);
  }
  
  @Deprecated
  public void setCloseNotifyTimeoutMillis(long closeNotifyFlushTimeoutMillis) {
    setCloseNotifyFlushTimeoutMillis(closeNotifyFlushTimeoutMillis);
  }
  
  public final long getCloseNotifyFlushTimeoutMillis() {
    return this.closeNotifyFlushTimeoutMillis;
  }
  
  public final void setCloseNotifyFlushTimeout(long closeNotifyFlushTimeout, TimeUnit unit) {
    setCloseNotifyFlushTimeoutMillis(unit.toMillis(closeNotifyFlushTimeout));
  }
  
  public final void setCloseNotifyFlushTimeoutMillis(long closeNotifyFlushTimeoutMillis) {
    if (closeNotifyFlushTimeoutMillis < 0L)
      throw new IllegalArgumentException("closeNotifyFlushTimeoutMillis: " + closeNotifyFlushTimeoutMillis + " (expected: >= 0)"); 
    this.closeNotifyFlushTimeoutMillis = closeNotifyFlushTimeoutMillis;
  }
  
  public final long getCloseNotifyReadTimeoutMillis() {
    return this.closeNotifyReadTimeoutMillis;
  }
  
  public final void setCloseNotifyReadTimeout(long closeNotifyReadTimeout, TimeUnit unit) {
    setCloseNotifyReadTimeoutMillis(unit.toMillis(closeNotifyReadTimeout));
  }
  
  public final void setCloseNotifyReadTimeoutMillis(long closeNotifyReadTimeoutMillis) {
    if (closeNotifyReadTimeoutMillis < 0L)
      throw new IllegalArgumentException("closeNotifyReadTimeoutMillis: " + closeNotifyReadTimeoutMillis + " (expected: >= 0)"); 
    this.closeNotifyReadTimeoutMillis = closeNotifyReadTimeoutMillis;
  }
  
  public SSLEngine engine() {
    return this.engine;
  }
  
  public String applicationProtocol() {
    SSLEngine engine = engine();
    if (!(engine instanceof ApplicationProtocolAccessor))
      return null; 
    return ((ApplicationProtocolAccessor)engine).getNegotiatedApplicationProtocol();
  }
  
  public Future<Channel> handshakeFuture() {
    return (Future<Channel>)this.handshakePromise;
  }
  
  @Deprecated
  public ChannelFuture close() {
    return close(this.ctx.newPromise());
  }
  
  @Deprecated
  public ChannelFuture close(final ChannelPromise promise) {
    final ChannelHandlerContext ctx = this.ctx;
    ctx.executor().execute(new Runnable() {
          public void run() {
            SslHandler.this.outboundClosed = true;
            SslHandler.this.engine.closeOutbound();
            try {
              SslHandler.this.flush(ctx, promise);
            } catch (Exception e) {
              if (!promise.tryFailure(e))
                SslHandler.logger.warn("{} flush() raised a masked exception.", ctx.channel(), e); 
            } 
          }
        });
    return (ChannelFuture)promise;
  }
  
  public Future<Channel> sslCloseFuture() {
    return (Future<Channel>)this.sslClosePromise;
  }
  
  public void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
    if (!this.pendingUnencryptedWrites.isEmpty())
      this.pendingUnencryptedWrites.releaseAndFailAll((ChannelOutboundInvoker)ctx, (Throwable)new ChannelException("Pending write on removal of SslHandler")); 
    this.pendingUnencryptedWrites = null;
    if (this.engine instanceof ReferenceCounted)
      ((ReferenceCounted)this.engine).release(); 
  }
  
  public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    ctx.bind(localAddress, promise);
  }
  
  public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    ctx.connect(remoteAddress, localAddress, promise);
  }
  
  public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    ctx.deregister(promise);
  }
  
  public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    closeOutboundAndChannel(ctx, promise, true);
  }
  
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    closeOutboundAndChannel(ctx, promise, false);
  }
  
  public void read(ChannelHandlerContext ctx) throws Exception {
    if (!this.handshakePromise.isDone())
      this.readDuringHandshake = true; 
    ctx.read();
  }
  
  private static IllegalStateException newPendingWritesNullException() {
    return new IllegalStateException("pendingUnencryptedWrites is null, handlerRemoved0 called?");
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (!(msg instanceof ByteBuf)) {
      UnsupportedMessageTypeException exception = new UnsupportedMessageTypeException(msg, new Class[] { ByteBuf.class });
      ReferenceCountUtil.safeRelease(msg);
      promise.setFailure((Throwable)exception);
    } else if (this.pendingUnencryptedWrites == null) {
      ReferenceCountUtil.safeRelease(msg);
      promise.setFailure(newPendingWritesNullException());
    } else {
      this.pendingUnencryptedWrites.add((ByteBuf)msg, promise);
    } 
  }
  
  public void flush(ChannelHandlerContext ctx) throws Exception {
    if (this.startTls && !this.sentFirstMessage) {
      this.sentFirstMessage = true;
      this.pendingUnencryptedWrites.writeAndRemoveAll(ctx);
      forceFlush(ctx);
      return;
    } 
    try {
      wrapAndFlush(ctx);
    } catch (Throwable cause) {
      setHandshakeFailure(ctx, cause);
      PlatformDependent.throwException(cause);
    } 
  }
  
  private void wrapAndFlush(ChannelHandlerContext ctx) throws SSLException {
    if (this.pendingUnencryptedWrites.isEmpty())
      this.pendingUnencryptedWrites.add(Unpooled.EMPTY_BUFFER, ctx.newPromise()); 
    if (!this.handshakePromise.isDone())
      this.flushedBeforeHandshake = true; 
    try {
      wrap(ctx, false);
    } finally {
      forceFlush(ctx);
    } 
  }
  
  private void wrap(ChannelHandlerContext ctx, boolean inUnwrap) throws SSLException {
    ByteBuf out = null;
    ChannelPromise promise = null;
    ByteBufAllocator alloc = ctx.alloc();
    boolean needUnwrap = false;
    ByteBuf buf = null;
    try {
      int wrapDataSize = this.wrapDataSize;
      while (!ctx.isRemoved()) {
        promise = ctx.newPromise();
        buf = (wrapDataSize > 0) ? this.pendingUnencryptedWrites.remove(alloc, wrapDataSize, promise) : this.pendingUnencryptedWrites.removeFirst(promise);
        if (buf == null)
          break; 
        if (out == null)
          out = allocateOutNetBuf(ctx, buf.readableBytes(), buf.nioBufferCount()); 
        SSLEngineResult result = wrap(alloc, this.engine, buf, out);
        if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
          buf.release();
          buf = null;
          promise.tryFailure(SSLENGINE_CLOSED);
          promise = null;
          this.pendingUnencryptedWrites.releaseAndFailAll((ChannelOutboundInvoker)ctx, SSLENGINE_CLOSED);
          return;
        } 
        if (buf.isReadable()) {
          this.pendingUnencryptedWrites.addFirst(buf, promise);
          promise = null;
        } else {
          buf.release();
        } 
        buf = null;
        switch (result.getHandshakeStatus()) {
          case BUFFER_OVERFLOW:
            runDelegatedTasks();
            continue;
          case CLOSED:
            setHandshakeSuccess();
          case null:
            setHandshakeSuccessIfStillHandshaking();
          case null:
            finishWrap(ctx, out, promise, inUnwrap, false);
            promise = null;
            out = null;
            continue;
          case null:
            needUnwrap = true;
            return;
        } 
        throw new IllegalStateException("Unknown handshake status: " + result
            .getHandshakeStatus());
      } 
    } finally {
      if (buf != null)
        buf.release(); 
      finishWrap(ctx, out, promise, inUnwrap, needUnwrap);
    } 
  }
  
  private void finishWrap(ChannelHandlerContext ctx, ByteBuf out, ChannelPromise promise, boolean inUnwrap, boolean needUnwrap) {
    if (out == null) {
      out = Unpooled.EMPTY_BUFFER;
    } else if (!out.isReadable()) {
      out.release();
      out = Unpooled.EMPTY_BUFFER;
    } 
    if (promise != null) {
      ctx.write(out, promise);
    } else {
      ctx.write(out);
    } 
    if (inUnwrap)
      this.needsFlush = true; 
    if (needUnwrap)
      readIfNeeded(ctx); 
  }
  
  private boolean wrapNonAppData(ChannelHandlerContext ctx, boolean inUnwrap) throws SSLException {
    ByteBuf out = null;
    ByteBufAllocator alloc = ctx.alloc();
    try {
      while (!ctx.isRemoved()) {
        boolean bool;
        if (out == null)
          out = allocateOutNetBuf(ctx, 2048, 1); 
        SSLEngineResult result = wrap(alloc, this.engine, Unpooled.EMPTY_BUFFER, out);
        if (result.bytesProduced() > 0) {
          ctx.write(out);
          if (inUnwrap)
            this.needsFlush = true; 
          out = null;
        } 
        switch (result.getHandshakeStatus()) {
          case CLOSED:
            setHandshakeSuccess();
            bool = false;
            return bool;
          case BUFFER_OVERFLOW:
            runDelegatedTasks();
            break;
          case null:
            if (inUnwrap) {
              bool = false;
              return bool;
            } 
            unwrapNonAppData(ctx);
            break;
          case null:
            break;
          case null:
            setHandshakeSuccessIfStillHandshaking();
            if (!inUnwrap)
              unwrapNonAppData(ctx); 
            bool = true;
            return bool;
          default:
            throw new IllegalStateException("Unknown handshake status: " + result.getHandshakeStatus());
        } 
        if (result.bytesProduced() == 0)
          break; 
        if (result.bytesConsumed() == 0 && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)
          break; 
      } 
    } finally {
      if (out != null)
        out.release(); 
    } 
    return false;
  }
  
  private SSLEngineResult wrap(ByteBufAllocator alloc, SSLEngine engine, ByteBuf in, ByteBuf out) throws SSLException {
    ByteBuf newDirectIn = null;
    try {
      ByteBuffer[] in0;
      SSLEngineResult result;
      int readerIndex = in.readerIndex();
      int readableBytes = in.readableBytes();
      if (in.isDirect() || !this.engineType.wantsDirectBuffer) {
        if (!(in instanceof CompositeByteBuf) && in.nioBufferCount() == 1) {
          in0 = this.singleBuffer;
          in0[0] = in.internalNioBuffer(readerIndex, readableBytes);
        } else {
          in0 = in.nioBuffers();
        } 
      } else {
        newDirectIn = alloc.directBuffer(readableBytes);
        newDirectIn.writeBytes(in, readerIndex, readableBytes);
        in0 = this.singleBuffer;
        in0[0] = newDirectIn.internalNioBuffer(newDirectIn.readerIndex(), readableBytes);
      } 
      while (true) {
        ByteBuffer out0 = out.nioBuffer(out.writerIndex(), out.writableBytes());
        result = engine.wrap(in0, out0);
        in.skipBytes(result.bytesConsumed());
        out.writerIndex(out.writerIndex() + result.bytesProduced());
        switch (result.getStatus()) {
          case BUFFER_OVERFLOW:
            out.ensureWritable(engine.getSession().getPacketBufferSize());
            continue;
        } 
        break;
      } 
      return result;
    } finally {
      this.singleBuffer[0] = null;
      if (newDirectIn != null)
        newDirectIn.release(); 
    } 
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    setHandshakeFailure(ctx, CHANNEL_CLOSED, !this.outboundClosed, this.handshakeStarted, false);
    notifyClosePromise(CHANNEL_CLOSED);
    super.channelInactive(ctx);
  }
  
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (ignoreException(cause)) {
      if (logger.isDebugEnabled())
        logger.debug("{} Swallowing a harmless 'connection reset by peer / broken pipe' error that occurred while writing close_notify in response to the peer's close_notify", ctx
            
            .channel(), cause); 
      if (ctx.channel().isActive())
        ctx.close(); 
    } else {
      ctx.fireExceptionCaught(cause);
    } 
  }
  
  private boolean ignoreException(Throwable t) {
    if (!(t instanceof SSLException) && t instanceof java.io.IOException && this.sslClosePromise.isDone()) {
      String message = t.getMessage();
      if (message != null && IGNORABLE_ERROR_MESSAGE.matcher(message).matches())
        return true; 
      StackTraceElement[] elements = t.getStackTrace();
      for (StackTraceElement element : elements) {
        String classname = element.getClassName();
        String methodname = element.getMethodName();
        if (!classname.startsWith("com.github.steveice10.netty."))
          if ("read".equals(methodname)) {
            if (IGNORABLE_CLASS_IN_STACK.matcher(classname).matches())
              return true; 
            try {
              Class<?> clazz = PlatformDependent.getClassLoader(getClass()).loadClass(classname);
              if (SocketChannel.class.isAssignableFrom(clazz) || DatagramChannel.class
                .isAssignableFrom(clazz))
                return true; 
              if (PlatformDependent.javaVersion() >= 7 && "com.sun.nio.sctp.SctpChannel"
                .equals(clazz.getSuperclass().getName()))
                return true; 
            } catch (Throwable cause) {
              logger.debug("Unexpected exception while loading class {} classname {}", new Object[] { getClass(), classname, cause });
            } 
          }  
      } 
    } 
    return false;
  }
  
  public static boolean isEncrypted(ByteBuf buffer) {
    if (buffer.readableBytes() < 5)
      throw new IllegalArgumentException("buffer must have at least 5 readable bytes"); 
    return (SslUtils.getEncryptedPacketLength(buffer, buffer.readerIndex()) != -2);
  }
  
  private void decodeJdkCompatible(ChannelHandlerContext ctx, ByteBuf in) throws NotSslRecordException {
    int packetLength = this.packetLength;
    if (packetLength > 0) {
      if (in.readableBytes() < packetLength)
        return; 
    } else {
      int readableBytes = in.readableBytes();
      if (readableBytes < 5)
        return; 
      packetLength = SslUtils.getEncryptedPacketLength(in, in.readerIndex());
      if (packetLength == -2) {
        NotSslRecordException e = new NotSslRecordException("not an SSL/TLS record: " + ByteBufUtil.hexDump(in));
        in.skipBytes(in.readableBytes());
        setHandshakeFailure(ctx, e);
        throw e;
      } 
      assert packetLength > 0;
      if (packetLength > readableBytes) {
        this.packetLength = packetLength;
        return;
      } 
    } 
    this.packetLength = 0;
    try {
      int bytesConsumed = unwrap(ctx, in, in.readerIndex(), packetLength);
      assert bytesConsumed == packetLength || this.engine.isInboundDone() : "we feed the SSLEngine a packets worth of data: " + packetLength + " but it only consumed: " + bytesConsumed;
      in.skipBytes(bytesConsumed);
    } catch (Throwable cause) {
      handleUnwrapThrowable(ctx, cause);
    } 
  }
  
  private void decodeNonJdkCompatible(ChannelHandlerContext ctx, ByteBuf in) {
    try {
      in.skipBytes(unwrap(ctx, in, in.readerIndex(), in.readableBytes()));
    } catch (Throwable cause) {
      handleUnwrapThrowable(ctx, cause);
    } 
  }
  
  private void handleUnwrapThrowable(ChannelHandlerContext ctx, Throwable cause) {
    try {
      if (this.handshakePromise.tryFailure(cause))
        ctx.fireUserEventTriggered(new SslHandshakeCompletionEvent(cause)); 
      wrapAndFlush(ctx);
    } catch (SSLException ex) {
      logger.debug("SSLException during trying to call SSLEngine.wrap(...) because of an previous SSLException, ignoring...", ex);
    } finally {
      setHandshakeFailure(ctx, cause, true, false, true);
    } 
    PlatformDependent.throwException(cause);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws SSLException {
    if (this.jdkCompatibilityMode) {
      decodeJdkCompatible(ctx, in);
    } else {
      decodeNonJdkCompatible(ctx, in);
    } 
  }
  
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    discardSomeReadBytes();
    flushIfNeeded(ctx);
    readIfNeeded(ctx);
    this.firedChannelRead = false;
    ctx.fireChannelReadComplete();
  }
  
  private void readIfNeeded(ChannelHandlerContext ctx) {
    if (!ctx.channel().config().isAutoRead() && (!this.firedChannelRead || !this.handshakePromise.isDone()))
      ctx.read(); 
  }
  
  private void flushIfNeeded(ChannelHandlerContext ctx) {
    if (this.needsFlush)
      forceFlush(ctx); 
  }
  
  private void unwrapNonAppData(ChannelHandlerContext ctx) throws SSLException {
    unwrap(ctx, Unpooled.EMPTY_BUFFER, 0, 0);
  }
  
  private int unwrap(ChannelHandlerContext ctx, ByteBuf packet, int offset, int length) throws SSLException {
    int originalLength = length;
    boolean wrapLater = false;
    boolean notifyClosure = false;
    int overflowReadableBytes = -1;
    ByteBuf decodeOut = allocate(ctx, length);
    try {
      while (!ctx.isRemoved()) {
        int readableBytes, previousOverflowReadableBytes, bufferSize;
        SSLEngineResult result = this.engineType.unwrap(this, packet, offset, length, decodeOut);
        SSLEngineResult.Status status = result.getStatus();
        SSLEngineResult.HandshakeStatus handshakeStatus = result.getHandshakeStatus();
        int produced = result.bytesProduced();
        int consumed = result.bytesConsumed();
        offset += consumed;
        length -= consumed;
        switch (status) {
          case BUFFER_OVERFLOW:
            readableBytes = decodeOut.readableBytes();
            previousOverflowReadableBytes = overflowReadableBytes;
            overflowReadableBytes = readableBytes;
            bufferSize = this.engine.getSession().getApplicationBufferSize() - readableBytes;
            if (readableBytes > 0) {
              this.firedChannelRead = true;
              ctx.fireChannelRead(decodeOut);
              decodeOut = null;
              if (bufferSize <= 0)
                bufferSize = this.engine.getSession().getApplicationBufferSize(); 
            } else {
              decodeOut.release();
              decodeOut = null;
            } 
            if (readableBytes == 0 && previousOverflowReadableBytes == 0)
              throw new IllegalStateException("Two consecutive overflows but no content was consumed. " + SSLSession.class
                  .getSimpleName() + " getApplicationBufferSize: " + this.engine
                  .getSession().getApplicationBufferSize() + " maybe too small."); 
            decodeOut = allocate(ctx, this.engineType.calculatePendingData(this, bufferSize));
            continue;
          case CLOSED:
            notifyClosure = true;
            overflowReadableBytes = -1;
            break;
          default:
            overflowReadableBytes = -1;
            break;
        } 
        switch (handshakeStatus) {
          case null:
            break;
          case null:
            if (wrapNonAppData(ctx, true) && length == 0)
              break; 
            break;
          case BUFFER_OVERFLOW:
            runDelegatedTasks();
            break;
          case CLOSED:
            setHandshakeSuccess();
            wrapLater = true;
            break;
          case null:
            if (setHandshakeSuccessIfStillHandshaking()) {
              wrapLater = true;
              continue;
            } 
            if (this.flushedBeforeHandshake) {
              this.flushedBeforeHandshake = false;
              wrapLater = true;
            } 
            if (length == 0)
              break; 
            break;
          default:
            throw new IllegalStateException("unknown handshake status: " + handshakeStatus);
        } 
        if (status == SSLEngineResult.Status.BUFFER_UNDERFLOW || (consumed == 0 && produced == 0)) {
          if (handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP)
            readIfNeeded(ctx); 
          break;
        } 
      } 
      if (wrapLater)
        wrap(ctx, true); 
      if (notifyClosure)
        notifyClosePromise((Throwable)null); 
    } finally {
      if (decodeOut != null)
        if (decodeOut.isReadable()) {
          this.firedChannelRead = true;
          ctx.fireChannelRead(decodeOut);
        } else {
          decodeOut.release();
        }  
    } 
    return originalLength - length;
  }
  
  private static ByteBuffer toByteBuffer(ByteBuf out, int index, int len) {
    return (out.nioBufferCount() == 1) ? out.internalNioBuffer(index, len) : out
      .nioBuffer(index, len);
  }
  
  private void runDelegatedTasks() {
    if (this.delegatedTaskExecutor == ImmediateExecutor.INSTANCE) {
      while (true) {
        Runnable task = this.engine.getDelegatedTask();
        if (task == null)
          break; 
        task.run();
      } 
    } else {
      final List<Runnable> tasks = new ArrayList<Runnable>(2);
      while (true) {
        Runnable task = this.engine.getDelegatedTask();
        if (task == null)
          break; 
        tasks.add(task);
      } 
      if (tasks.isEmpty())
        return; 
      final CountDownLatch latch = new CountDownLatch(1);
      this.delegatedTaskExecutor.execute(new Runnable() {
            public void run() {
              try {
                for (Runnable task : tasks)
                  task.run(); 
              } catch (Exception e) {
                SslHandler.this.ctx.fireExceptionCaught(e);
              } finally {
                latch.countDown();
              } 
            }
          });
      boolean interrupted = false;
      while (latch.getCount() != 0L) {
        try {
          latch.await();
        } catch (InterruptedException e) {
          interrupted = true;
        } 
      } 
      if (interrupted)
        Thread.currentThread().interrupt(); 
    } 
  }
  
  private boolean setHandshakeSuccessIfStillHandshaking() {
    if (!this.handshakePromise.isDone()) {
      setHandshakeSuccess();
      return true;
    } 
    return false;
  }
  
  private void setHandshakeSuccess() {
    this.handshakePromise.trySuccess(this.ctx.channel());
    if (logger.isDebugEnabled())
      logger.debug("{} HANDSHAKEN: {}", this.ctx.channel(), this.engine.getSession().getCipherSuite()); 
    this.ctx.fireUserEventTriggered(SslHandshakeCompletionEvent.SUCCESS);
    if (this.readDuringHandshake && !this.ctx.channel().config().isAutoRead()) {
      this.readDuringHandshake = false;
      this.ctx.read();
    } 
  }
  
  private void setHandshakeFailure(ChannelHandlerContext ctx, Throwable cause) {
    setHandshakeFailure(ctx, cause, true, true, false);
  }
  
  private void setHandshakeFailure(ChannelHandlerContext ctx, Throwable cause, boolean closeInbound, boolean notify, boolean alwaysFlushAndClose) {
    try {
      this.outboundClosed = true;
      this.engine.closeOutbound();
      if (closeInbound)
        try {
          this.engine.closeInbound();
        } catch (SSLException e) {
          if (logger.isDebugEnabled()) {
            String msg = e.getMessage();
            if (msg == null || !msg.contains("possible truncation attack"))
              logger.debug("{} SSLEngine.closeInbound() raised an exception.", ctx.channel(), e); 
          } 
        }  
      if (this.handshakePromise.tryFailure(cause) || alwaysFlushAndClose)
        SslUtils.handleHandshakeFailure(ctx, cause, notify); 
    } finally {
      releaseAndFailAll(cause);
    } 
  }
  
  private void releaseAndFailAll(Throwable cause) {
    if (this.pendingUnencryptedWrites != null)
      this.pendingUnencryptedWrites.releaseAndFailAll((ChannelOutboundInvoker)this.ctx, cause); 
  }
  
  private void notifyClosePromise(Throwable cause) {
    if (cause == null) {
      if (this.sslClosePromise.trySuccess(this.ctx.channel()))
        this.ctx.fireUserEventTriggered(SslCloseCompletionEvent.SUCCESS); 
    } else if (this.sslClosePromise.tryFailure(cause)) {
      this.ctx.fireUserEventTriggered(new SslCloseCompletionEvent(cause));
    } 
  }
  
  private void closeOutboundAndChannel(ChannelHandlerContext ctx, final ChannelPromise promise, boolean disconnect) throws Exception {
    this.outboundClosed = true;
    this.engine.closeOutbound();
    if (!ctx.channel().isActive()) {
      if (disconnect) {
        ctx.disconnect(promise);
      } else {
        ctx.close(promise);
      } 
      return;
    } 
    ChannelPromise closeNotifyPromise = ctx.newPromise();
    try {
      flush(ctx, closeNotifyPromise);
    } finally {
      if (!this.closeNotify) {
        this.closeNotify = true;
        safeClose(ctx, (ChannelFuture)closeNotifyPromise, ctx.newPromise().addListener((GenericFutureListener)new ChannelPromiseNotifier(false, new ChannelPromise[] { promise })));
      } else {
        this.sslClosePromise.addListener((GenericFutureListener)new FutureListener<Channel>() {
              public void operationComplete(Future<Channel> future) {
                promise.setSuccess();
              }
            });
      } 
    } 
  }
  
  private void flush(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    if (this.pendingUnencryptedWrites != null) {
      this.pendingUnencryptedWrites.add(Unpooled.EMPTY_BUFFER, promise);
    } else {
      promise.setFailure(newPendingWritesNullException());
    } 
    flush(ctx);
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
    this.pendingUnencryptedWrites = new SslHandlerCoalescingBufferQueue(ctx.channel(), 16);
    if (ctx.channel().isActive())
      startHandshakeProcessing(); 
  }
  
  private void startHandshakeProcessing() {
    this.handshakeStarted = true;
    if (this.engine.getUseClientMode()) {
      handshake((Promise<Channel>)null);
    } else {
      applyHandshakeTimeout((Promise<Channel>)null);
    } 
  }
  
  public Future<Channel> renegotiate() {
    ChannelHandlerContext ctx = this.ctx;
    if (ctx == null)
      throw new IllegalStateException(); 
    return renegotiate(ctx.executor().newPromise());
  }
  
  public Future<Channel> renegotiate(final Promise<Channel> promise) {
    if (promise == null)
      throw new NullPointerException("promise"); 
    ChannelHandlerContext ctx = this.ctx;
    if (ctx == null)
      throw new IllegalStateException(); 
    EventExecutor executor = ctx.executor();
    if (!executor.inEventLoop()) {
      executor.execute(new Runnable() {
            public void run() {
              SslHandler.this.handshake(promise);
            }
          });
      return (Future<Channel>)promise;
    } 
    handshake(promise);
    return (Future<Channel>)promise;
  }
  
  private void handshake(final Promise<Channel> newHandshakePromise) {
    Promise<Channel> p;
    if (newHandshakePromise != null) {
      Promise<Channel> oldHandshakePromise = this.handshakePromise;
      if (!oldHandshakePromise.isDone()) {
        oldHandshakePromise.addListener((GenericFutureListener)new FutureListener<Channel>() {
              public void operationComplete(Future<Channel> future) throws Exception {
                if (future.isSuccess()) {
                  newHandshakePromise.setSuccess(future.getNow());
                } else {
                  newHandshakePromise.setFailure(future.cause());
                } 
              }
            });
        return;
      } 
      this.handshakePromise = p = newHandshakePromise;
    } else {
      if (this.engine.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)
        return; 
      p = this.handshakePromise;
      assert !p.isDone();
    } 
    ChannelHandlerContext ctx = this.ctx;
    try {
      this.engine.beginHandshake();
      wrapNonAppData(ctx, false);
    } catch (Throwable e) {
      setHandshakeFailure(ctx, e);
    } finally {
      forceFlush(ctx);
    } 
    applyHandshakeTimeout(p);
  }
  
  private void applyHandshakeTimeout(Promise<Channel> p) {
    final Promise<Channel> promise = (p == null) ? this.handshakePromise : p;
    long handshakeTimeoutMillis = this.handshakeTimeoutMillis;
    if (handshakeTimeoutMillis <= 0L || promise.isDone())
      return; 
    final ScheduledFuture timeoutFuture = this.ctx.executor().schedule(new Runnable() {
          public void run() {
            if (promise.isDone())
              return; 
            try {
              if (SslHandler.this.handshakePromise.tryFailure(SslHandler.HANDSHAKE_TIMED_OUT))
                SslUtils.handleHandshakeFailure(SslHandler.this.ctx, SslHandler.HANDSHAKE_TIMED_OUT, true); 
            } finally {
              SslHandler.this.releaseAndFailAll(SslHandler.HANDSHAKE_TIMED_OUT);
            } 
          }
        }handshakeTimeoutMillis, TimeUnit.MILLISECONDS);
    promise.addListener((GenericFutureListener)new FutureListener<Channel>() {
          public void operationComplete(Future<Channel> f) throws Exception {
            timeoutFuture.cancel(false);
          }
        });
  }
  
  private void forceFlush(ChannelHandlerContext ctx) {
    this.needsFlush = false;
    ctx.flush();
  }
  
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    if (!this.startTls)
      startHandshakeProcessing(); 
    ctx.fireChannelActive();
  }
  
  private void safeClose(final ChannelHandlerContext ctx, final ChannelFuture flushFuture, final ChannelPromise promise) {
    final ScheduledFuture<?> timeoutFuture;
    if (!ctx.channel().isActive()) {
      ctx.close(promise);
      return;
    } 
    if (!flushFuture.isDone()) {
      long closeNotifyTimeout = this.closeNotifyFlushTimeoutMillis;
      if (closeNotifyTimeout > 0L) {
        ScheduledFuture scheduledFuture = ctx.executor().schedule(new Runnable() {
              public void run() {
                if (!flushFuture.isDone()) {
                  SslHandler.logger.warn("{} Last write attempt timed out; force-closing the connection.", ctx
                      .channel());
                  SslHandler.addCloseListener(ctx.close(ctx.newPromise()), promise);
                } 
              }
            }closeNotifyTimeout, TimeUnit.MILLISECONDS);
      } else {
        timeoutFuture = null;
      } 
    } else {
      timeoutFuture = null;
    } 
    flushFuture.addListener((GenericFutureListener)new ChannelFutureListener() {
          public void operationComplete(ChannelFuture f) throws Exception {
            if (timeoutFuture != null)
              timeoutFuture.cancel(false); 
            final long closeNotifyReadTimeout = SslHandler.this.closeNotifyReadTimeoutMillis;
            if (closeNotifyReadTimeout <= 0L) {
              SslHandler.addCloseListener(ctx.close(ctx.newPromise()), promise);
            } else {
              final ScheduledFuture<?> closeNotifyReadTimeoutFuture;
              if (!SslHandler.this.sslClosePromise.isDone()) {
                ScheduledFuture scheduledFuture = ctx.executor().schedule(new Runnable() {
                      public void run() {
                        if (!SslHandler.this.sslClosePromise.isDone()) {
                          SslHandler.logger.debug("{} did not receive close_notify in {}ms; force-closing the connection.", ctx
                              
                              .channel(), Long.valueOf(closeNotifyReadTimeout));
                          SslHandler.addCloseListener(ctx.close(ctx.newPromise()), promise);
                        } 
                      }
                    }closeNotifyReadTimeout, TimeUnit.MILLISECONDS);
              } else {
                closeNotifyReadTimeoutFuture = null;
              } 
              SslHandler.this.sslClosePromise.addListener((GenericFutureListener)new FutureListener<Channel>() {
                    public void operationComplete(Future<Channel> future) throws Exception {
                      if (closeNotifyReadTimeoutFuture != null)
                        closeNotifyReadTimeoutFuture.cancel(false); 
                      SslHandler.addCloseListener(ctx.close(ctx.newPromise()), promise);
                    }
                  });
            } 
          }
        });
  }
  
  private static void addCloseListener(ChannelFuture future, ChannelPromise promise) {
    future.addListener((GenericFutureListener)new ChannelPromiseNotifier(false, new ChannelPromise[] { promise }));
  }
  
  private ByteBuf allocate(ChannelHandlerContext ctx, int capacity) {
    ByteBufAllocator alloc = ctx.alloc();
    if (this.engineType.wantsDirectBuffer)
      return alloc.directBuffer(capacity); 
    return alloc.buffer(capacity);
  }
  
  private ByteBuf allocateOutNetBuf(ChannelHandlerContext ctx, int pendingBytes, int numComponents) {
    return allocate(ctx, this.engineType.calculateWrapBufferCapacity(this, pendingBytes, numComponents));
  }
  
  private final class SslHandlerCoalescingBufferQueue extends AbstractCoalescingBufferQueue {
    SslHandlerCoalescingBufferQueue(Channel channel, int initSize) {
      super(channel, initSize);
    }
    
    protected ByteBuf compose(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf next) {
      int wrapDataSize = SslHandler.this.wrapDataSize;
      if (cumulation instanceof CompositeByteBuf) {
        CompositeByteBuf composite = (CompositeByteBuf)cumulation;
        int numComponents = composite.numComponents();
        if (numComponents == 0 || 
          !SslHandler.attemptCopyToCumulation(composite.internalComponent(numComponents - 1), next, wrapDataSize))
          composite.addComponent(true, next); 
        return (ByteBuf)composite;
      } 
      return SslHandler.attemptCopyToCumulation(cumulation, next, wrapDataSize) ? cumulation : 
        copyAndCompose(alloc, cumulation, next);
    }
    
    protected ByteBuf composeFirst(ByteBufAllocator allocator, ByteBuf first) {
      if (first instanceof CompositeByteBuf) {
        CompositeByteBuf composite = (CompositeByteBuf)first;
        first = allocator.directBuffer(composite.readableBytes());
        try {
          first.writeBytes((ByteBuf)composite);
        } catch (Throwable cause) {
          first.release();
          PlatformDependent.throwException(cause);
        } 
        composite.release();
      } 
      return first;
    }
    
    protected ByteBuf removeEmptyValue() {
      return null;
    }
  }
  
  private static boolean attemptCopyToCumulation(ByteBuf cumulation, ByteBuf next, int wrapDataSize) {
    int inReadableBytes = next.readableBytes();
    int cumulationCapacity = cumulation.capacity();
    if (wrapDataSize - cumulation.readableBytes() >= inReadableBytes && ((cumulation
      
      .isWritable(inReadableBytes) && cumulationCapacity >= wrapDataSize) || (cumulationCapacity < wrapDataSize && 
      
      ByteBufUtil.ensureWritableSuccess(cumulation.ensureWritable(inReadableBytes, false))))) {
      cumulation.writeBytes(next);
      next.release();
      return true;
    } 
    return false;
  }
  
  private final class LazyChannelPromise extends DefaultPromise<Channel> {
    private LazyChannelPromise() {}
    
    protected EventExecutor executor() {
      if (SslHandler.this.ctx == null)
        throw new IllegalStateException(); 
      return SslHandler.this.ctx.executor();
    }
    
    protected void checkDeadLock() {
      if (SslHandler.this.ctx == null)
        return; 
      super.checkDeadLock();
    }
  }
}

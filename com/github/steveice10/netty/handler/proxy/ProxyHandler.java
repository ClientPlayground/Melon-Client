package com.github.steveice10.netty.handler.proxy;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelDuplexHandler;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.PendingWriteQueue;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.DefaultPromise;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.ScheduledFuture;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.SocketAddress;
import java.nio.channels.ConnectionPendingException;
import java.util.concurrent.TimeUnit;

public abstract class ProxyHandler extends ChannelDuplexHandler {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ProxyHandler.class);
  
  private static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = 10000L;
  
  static final String AUTH_NONE = "none";
  
  private final SocketAddress proxyAddress;
  
  private volatile SocketAddress destinationAddress;
  
  private volatile long connectTimeoutMillis = 10000L;
  
  private volatile ChannelHandlerContext ctx;
  
  private PendingWriteQueue pendingWrites;
  
  private boolean finished;
  
  private boolean suppressChannelReadComplete;
  
  private boolean flushedPrematurely;
  
  private final LazyChannelPromise connectPromise = new LazyChannelPromise();
  
  private ScheduledFuture<?> connectTimeoutFuture;
  
  private final ChannelFutureListener writeListener = new ChannelFutureListener() {
      public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess())
          ProxyHandler.this.setConnectFailure(future.cause()); 
      }
    };
  
  protected ProxyHandler(SocketAddress proxyAddress) {
    if (proxyAddress == null)
      throw new NullPointerException("proxyAddress"); 
    this.proxyAddress = proxyAddress;
  }
  
  public final <T extends SocketAddress> T proxyAddress() {
    return (T)this.proxyAddress;
  }
  
  public final <T extends SocketAddress> T destinationAddress() {
    return (T)this.destinationAddress;
  }
  
  public final boolean isConnected() {
    return this.connectPromise.isSuccess();
  }
  
  public final Future<Channel> connectFuture() {
    return (Future<Channel>)this.connectPromise;
  }
  
  public final long connectTimeoutMillis() {
    return this.connectTimeoutMillis;
  }
  
  public final void setConnectTimeoutMillis(long connectTimeoutMillis) {
    if (connectTimeoutMillis <= 0L)
      connectTimeoutMillis = 0L; 
    this.connectTimeoutMillis = connectTimeoutMillis;
  }
  
  public final void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
    addCodec(ctx);
    if (ctx.channel().isActive())
      sendInitialMessage(ctx); 
  }
  
  public final void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    if (this.destinationAddress != null) {
      promise.setFailure(new ConnectionPendingException());
      return;
    } 
    this.destinationAddress = remoteAddress;
    ctx.connect(this.proxyAddress, localAddress, promise);
  }
  
  public final void channelActive(ChannelHandlerContext ctx) throws Exception {
    sendInitialMessage(ctx);
    ctx.fireChannelActive();
  }
  
  private void sendInitialMessage(ChannelHandlerContext ctx) throws Exception {
    long connectTimeoutMillis = this.connectTimeoutMillis;
    if (connectTimeoutMillis > 0L)
      this.connectTimeoutFuture = ctx.executor().schedule(new Runnable() {
            public void run() {
              if (!ProxyHandler.this.connectPromise.isDone())
                ProxyHandler.this.setConnectFailure(new ProxyConnectException(ProxyHandler.this.exceptionMessage("timeout"))); 
            }
          },  connectTimeoutMillis, TimeUnit.MILLISECONDS); 
    Object initialMessage = newInitialMessage(ctx);
    if (initialMessage != null)
      sendToProxyServer(initialMessage); 
    readIfNeeded(ctx);
  }
  
  protected final void sendToProxyServer(Object msg) {
    this.ctx.writeAndFlush(msg).addListener((GenericFutureListener)this.writeListener);
  }
  
  public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (this.finished) {
      ctx.fireChannelInactive();
    } else {
      setConnectFailure(new ProxyConnectException(exceptionMessage("disconnected")));
    } 
  }
  
  public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (this.finished) {
      ctx.fireExceptionCaught(cause);
    } else {
      setConnectFailure(cause);
    } 
  }
  
  public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (this.finished) {
      this.suppressChannelReadComplete = false;
      ctx.fireChannelRead(msg);
    } else {
      this.suppressChannelReadComplete = true;
      Throwable cause = null;
      try {
        boolean done = handleResponse(ctx, msg);
        if (done)
          setConnectSuccess(); 
      } catch (Throwable t) {
        cause = t;
      } finally {
        ReferenceCountUtil.release(msg);
        if (cause != null)
          setConnectFailure(cause); 
      } 
    } 
  }
  
  private void setConnectSuccess() {
    this.finished = true;
    cancelConnectTimeoutFuture();
    if (!this.connectPromise.isDone()) {
      boolean removedCodec = true;
      removedCodec &= safeRemoveEncoder();
      this.ctx.fireUserEventTriggered(new ProxyConnectionEvent(
            protocol(), authScheme(), this.proxyAddress, this.destinationAddress));
      removedCodec &= safeRemoveDecoder();
      if (removedCodec) {
        writePendingWrites();
        if (this.flushedPrematurely)
          this.ctx.flush(); 
        this.connectPromise.trySuccess(this.ctx.channel());
      } else {
        Exception cause = new ProxyConnectException("failed to remove all codec handlers added by the proxy handler; bug?");
        failPendingWritesAndClose(cause);
      } 
    } 
  }
  
  private boolean safeRemoveDecoder() {
    try {
      removeDecoder(this.ctx);
      return true;
    } catch (Exception e) {
      logger.warn("Failed to remove proxy decoders:", e);
      return false;
    } 
  }
  
  private boolean safeRemoveEncoder() {
    try {
      removeEncoder(this.ctx);
      return true;
    } catch (Exception e) {
      logger.warn("Failed to remove proxy encoders:", e);
      return false;
    } 
  }
  
  private void setConnectFailure(Throwable cause) {
    this.finished = true;
    cancelConnectTimeoutFuture();
    if (!this.connectPromise.isDone()) {
      if (!(cause instanceof ProxyConnectException))
        cause = new ProxyConnectException(exceptionMessage(cause.toString()), cause); 
      safeRemoveDecoder();
      safeRemoveEncoder();
      failPendingWritesAndClose(cause);
    } 
  }
  
  private void failPendingWritesAndClose(Throwable cause) {
    failPendingWrites(cause);
    this.connectPromise.tryFailure(cause);
    this.ctx.fireExceptionCaught(cause);
    this.ctx.close();
  }
  
  private void cancelConnectTimeoutFuture() {
    if (this.connectTimeoutFuture != null) {
      this.connectTimeoutFuture.cancel(false);
      this.connectTimeoutFuture = null;
    } 
  }
  
  protected final String exceptionMessage(String msg) {
    if (msg == null)
      msg = ""; 
    StringBuilder buf = (new StringBuilder(128 + msg.length())).append(protocol()).append(", ").append(authScheme()).append(", ").append(this.proxyAddress).append(" => ").append(this.destinationAddress);
    if (!msg.isEmpty())
      buf.append(", ").append(msg); 
    return buf.toString();
  }
  
  public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    if (this.suppressChannelReadComplete) {
      this.suppressChannelReadComplete = false;
      readIfNeeded(ctx);
    } else {
      ctx.fireChannelReadComplete();
    } 
  }
  
  public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (this.finished) {
      writePendingWrites();
      ctx.write(msg, promise);
    } else {
      addPendingWrite(ctx, msg, promise);
    } 
  }
  
  public final void flush(ChannelHandlerContext ctx) throws Exception {
    if (this.finished) {
      writePendingWrites();
      ctx.flush();
    } else {
      this.flushedPrematurely = true;
    } 
  }
  
  private static void readIfNeeded(ChannelHandlerContext ctx) {
    if (!ctx.channel().config().isAutoRead())
      ctx.read(); 
  }
  
  private void writePendingWrites() {
    if (this.pendingWrites != null) {
      this.pendingWrites.removeAndWriteAll();
      this.pendingWrites = null;
    } 
  }
  
  private void failPendingWrites(Throwable cause) {
    if (this.pendingWrites != null) {
      this.pendingWrites.removeAndFailAll(cause);
      this.pendingWrites = null;
    } 
  }
  
  private void addPendingWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
    PendingWriteQueue pendingWrites = this.pendingWrites;
    if (pendingWrites == null)
      this.pendingWrites = pendingWrites = new PendingWriteQueue(ctx); 
    pendingWrites.add(msg, promise);
  }
  
  public abstract String protocol();
  
  public abstract String authScheme();
  
  protected abstract void addCodec(ChannelHandlerContext paramChannelHandlerContext) throws Exception;
  
  protected abstract void removeEncoder(ChannelHandlerContext paramChannelHandlerContext) throws Exception;
  
  protected abstract void removeDecoder(ChannelHandlerContext paramChannelHandlerContext) throws Exception;
  
  protected abstract Object newInitialMessage(ChannelHandlerContext paramChannelHandlerContext) throws Exception;
  
  protected abstract boolean handleResponse(ChannelHandlerContext paramChannelHandlerContext, Object paramObject) throws Exception;
  
  private final class LazyChannelPromise extends DefaultPromise<Channel> {
    private LazyChannelPromise() {}
    
    protected EventExecutor executor() {
      if (ProxyHandler.this.ctx == null)
        throw new IllegalStateException(); 
      return ProxyHandler.this.ctx.executor();
    }
  }
}

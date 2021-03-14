package com.github.steveice10.netty.handler.flush;

import com.github.steveice10.netty.channel.ChannelDuplexHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import java.util.concurrent.Future;

public class FlushConsolidationHandler extends ChannelDuplexHandler {
  private final int explicitFlushAfterFlushes;
  
  private final boolean consolidateWhenNoReadInProgress;
  
  private final Runnable flushTask;
  
  private int flushPendingCount;
  
  private boolean readInProgress;
  
  private ChannelHandlerContext ctx;
  
  private Future<?> nextScheduledFlush;
  
  public FlushConsolidationHandler() {
    this(256, false);
  }
  
  public FlushConsolidationHandler(int explicitFlushAfterFlushes) {
    this(explicitFlushAfterFlushes, false);
  }
  
  public FlushConsolidationHandler(int explicitFlushAfterFlushes, boolean consolidateWhenNoReadInProgress) {
    if (explicitFlushAfterFlushes <= 0)
      throw new IllegalArgumentException("explicitFlushAfterFlushes: " + explicitFlushAfterFlushes + " (expected: > 0)"); 
    this.explicitFlushAfterFlushes = explicitFlushAfterFlushes;
    this.consolidateWhenNoReadInProgress = consolidateWhenNoReadInProgress;
    this.flushTask = consolidateWhenNoReadInProgress ? new Runnable() {
        public void run() {
          if (FlushConsolidationHandler.this.flushPendingCount > 0 && !FlushConsolidationHandler.this.readInProgress) {
            FlushConsolidationHandler.this.flushPendingCount = 0;
            FlushConsolidationHandler.this.ctx.flush();
            FlushConsolidationHandler.this.nextScheduledFlush = null;
          } 
        }
      } : null;
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
  }
  
  public void flush(ChannelHandlerContext ctx) throws Exception {
    if (this.readInProgress) {
      if (++this.flushPendingCount == this.explicitFlushAfterFlushes)
        flushNow(ctx); 
    } else if (this.consolidateWhenNoReadInProgress) {
      if (++this.flushPendingCount == this.explicitFlushAfterFlushes) {
        flushNow(ctx);
      } else {
        scheduleFlush(ctx);
      } 
    } else {
      flushNow(ctx);
    } 
  }
  
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    resetReadAndFlushIfNeeded(ctx);
    ctx.fireChannelReadComplete();
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    this.readInProgress = true;
    ctx.fireChannelRead(msg);
  }
  
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    resetReadAndFlushIfNeeded(ctx);
    ctx.fireExceptionCaught(cause);
  }
  
  public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    resetReadAndFlushIfNeeded(ctx);
    ctx.disconnect(promise);
  }
  
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    resetReadAndFlushIfNeeded(ctx);
    ctx.close(promise);
  }
  
  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    if (!ctx.channel().isWritable())
      flushIfNeeded(ctx); 
    ctx.fireChannelWritabilityChanged();
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    flushIfNeeded(ctx);
  }
  
  private void resetReadAndFlushIfNeeded(ChannelHandlerContext ctx) {
    this.readInProgress = false;
    flushIfNeeded(ctx);
  }
  
  private void flushIfNeeded(ChannelHandlerContext ctx) {
    if (this.flushPendingCount > 0)
      flushNow(ctx); 
  }
  
  private void flushNow(ChannelHandlerContext ctx) {
    cancelScheduledFlush();
    this.flushPendingCount = 0;
    ctx.flush();
  }
  
  private void scheduleFlush(ChannelHandlerContext ctx) {
    if (this.nextScheduledFlush == null)
      this.nextScheduledFlush = (Future<?>)ctx.channel().eventLoop().submit(this.flushTask); 
  }
  
  private void cancelScheduledFlush() {
    if (this.nextScheduledFlush != null) {
      this.nextScheduledFlush.cancel(false);
      this.nextScheduledFlush = null;
    } 
  }
}

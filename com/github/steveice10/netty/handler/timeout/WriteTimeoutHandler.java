package com.github.steveice10.netty.handler.timeout;

import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelOutboundHandlerAdapter;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WriteTimeoutHandler extends ChannelOutboundHandlerAdapter {
  private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
  
  private final long timeoutNanos;
  
  private WriteTimeoutTask lastTask;
  
  private boolean closed;
  
  public WriteTimeoutHandler(int timeoutSeconds) {
    this(timeoutSeconds, TimeUnit.SECONDS);
  }
  
  public WriteTimeoutHandler(long timeout, TimeUnit unit) {
    if (unit == null)
      throw new NullPointerException("unit"); 
    if (timeout <= 0L) {
      this.timeoutNanos = 0L;
    } else {
      this.timeoutNanos = Math.max(unit.toNanos(timeout), MIN_TIMEOUT_NANOS);
    } 
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (this.timeoutNanos > 0L) {
      promise = promise.unvoid();
      scheduleTimeout(ctx, promise);
    } 
    ctx.write(msg, promise);
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    WriteTimeoutTask task = this.lastTask;
    this.lastTask = null;
    while (task != null) {
      task.scheduledFuture.cancel(false);
      WriteTimeoutTask prev = task.prev;
      task.prev = null;
      task.next = null;
      task = prev;
    } 
  }
  
  private void scheduleTimeout(ChannelHandlerContext ctx, ChannelPromise promise) {
    WriteTimeoutTask task = new WriteTimeoutTask(ctx, promise);
    task.scheduledFuture = (ScheduledFuture<?>)ctx.executor().schedule(task, this.timeoutNanos, TimeUnit.NANOSECONDS);
    if (!task.scheduledFuture.isDone()) {
      addWriteTimeoutTask(task);
      promise.addListener((GenericFutureListener)task);
    } 
  }
  
  private void addWriteTimeoutTask(WriteTimeoutTask task) {
    if (this.lastTask != null) {
      this.lastTask.next = task;
      task.prev = this.lastTask;
    } 
    this.lastTask = task;
  }
  
  private void removeWriteTimeoutTask(WriteTimeoutTask task) {
    if (task == this.lastTask) {
      assert task.next == null;
      this.lastTask = this.lastTask.prev;
      if (this.lastTask != null)
        this.lastTask.next = null; 
    } else {
      if (task.prev == null && task.next == null)
        return; 
      if (task.prev == null) {
        task.next.prev = null;
      } else {
        task.prev.next = task.next;
        task.next.prev = task.prev;
      } 
    } 
    task.prev = null;
    task.next = null;
  }
  
  protected void writeTimedOut(ChannelHandlerContext ctx) throws Exception {
    if (!this.closed) {
      ctx.fireExceptionCaught((Throwable)WriteTimeoutException.INSTANCE);
      ctx.close();
      this.closed = true;
    } 
  }
  
  private final class WriteTimeoutTask implements Runnable, ChannelFutureListener {
    private final ChannelHandlerContext ctx;
    
    private final ChannelPromise promise;
    
    WriteTimeoutTask prev;
    
    WriteTimeoutTask next;
    
    ScheduledFuture<?> scheduledFuture;
    
    WriteTimeoutTask(ChannelHandlerContext ctx, ChannelPromise promise) {
      this.ctx = ctx;
      this.promise = promise;
    }
    
    public void run() {
      if (!this.promise.isDone())
        try {
          WriteTimeoutHandler.this.writeTimedOut(this.ctx);
        } catch (Throwable t) {
          this.ctx.fireExceptionCaught(t);
        }  
      WriteTimeoutHandler.this.removeWriteTimeoutTask(this);
    }
    
    public void operationComplete(ChannelFuture future) throws Exception {
      this.scheduledFuture.cancel(false);
      WriteTimeoutHandler.this.removeWriteTimeoutTask(this);
    }
  }
}

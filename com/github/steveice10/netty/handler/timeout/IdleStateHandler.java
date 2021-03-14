package com.github.steveice10.netty.handler.timeout;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelDuplexHandler;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class IdleStateHandler extends ChannelDuplexHandler {
  private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
  
  private final ChannelFutureListener writeListener = new ChannelFutureListener() {
      public void operationComplete(ChannelFuture future) throws Exception {
        IdleStateHandler.this.lastWriteTime = IdleStateHandler.this.ticksInNanos();
        IdleStateHandler.this.firstWriterIdleEvent = IdleStateHandler.this.firstAllIdleEvent = true;
      }
    };
  
  private final boolean observeOutput;
  
  private final long readerIdleTimeNanos;
  
  private final long writerIdleTimeNanos;
  
  private final long allIdleTimeNanos;
  
  private ScheduledFuture<?> readerIdleTimeout;
  
  private long lastReadTime;
  
  private boolean firstReaderIdleEvent = true;
  
  private ScheduledFuture<?> writerIdleTimeout;
  
  private long lastWriteTime;
  
  private boolean firstWriterIdleEvent = true;
  
  private ScheduledFuture<?> allIdleTimeout;
  
  private boolean firstAllIdleEvent = true;
  
  private byte state;
  
  private boolean reading;
  
  private long lastChangeCheckTimeStamp;
  
  private int lastMessageHashCode;
  
  private long lastPendingWriteBytes;
  
  public IdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
    this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
  }
  
  public IdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
    this(false, readerIdleTime, writerIdleTime, allIdleTime, unit);
  }
  
  public IdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
    if (unit == null)
      throw new NullPointerException("unit"); 
    this.observeOutput = observeOutput;
    if (readerIdleTime <= 0L) {
      this.readerIdleTimeNanos = 0L;
    } else {
      this.readerIdleTimeNanos = Math.max(unit.toNanos(readerIdleTime), MIN_TIMEOUT_NANOS);
    } 
    if (writerIdleTime <= 0L) {
      this.writerIdleTimeNanos = 0L;
    } else {
      this.writerIdleTimeNanos = Math.max(unit.toNanos(writerIdleTime), MIN_TIMEOUT_NANOS);
    } 
    if (allIdleTime <= 0L) {
      this.allIdleTimeNanos = 0L;
    } else {
      this.allIdleTimeNanos = Math.max(unit.toNanos(allIdleTime), MIN_TIMEOUT_NANOS);
    } 
  }
  
  public long getReaderIdleTimeInMillis() {
    return TimeUnit.NANOSECONDS.toMillis(this.readerIdleTimeNanos);
  }
  
  public long getWriterIdleTimeInMillis() {
    return TimeUnit.NANOSECONDS.toMillis(this.writerIdleTimeNanos);
  }
  
  public long getAllIdleTimeInMillis() {
    return TimeUnit.NANOSECONDS.toMillis(this.allIdleTimeNanos);
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    if (ctx.channel().isActive() && ctx.channel().isRegistered())
      initialize(ctx); 
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    destroy();
  }
  
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    if (ctx.channel().isActive())
      initialize(ctx); 
    super.channelRegistered(ctx);
  }
  
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    initialize(ctx);
    super.channelActive(ctx);
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    destroy();
    super.channelInactive(ctx);
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (this.readerIdleTimeNanos > 0L || this.allIdleTimeNanos > 0L) {
      this.reading = true;
      this.firstReaderIdleEvent = this.firstAllIdleEvent = true;
    } 
    ctx.fireChannelRead(msg);
  }
  
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    if ((this.readerIdleTimeNanos > 0L || this.allIdleTimeNanos > 0L) && this.reading) {
      this.lastReadTime = ticksInNanos();
      this.reading = false;
    } 
    ctx.fireChannelReadComplete();
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (this.writerIdleTimeNanos > 0L || this.allIdleTimeNanos > 0L) {
      ctx.write(msg, promise.unvoid()).addListener((GenericFutureListener)this.writeListener);
    } else {
      ctx.write(msg, promise);
    } 
  }
  
  private void initialize(ChannelHandlerContext ctx) {
    switch (this.state) {
      case 1:
      case 2:
        return;
    } 
    this.state = 1;
    initOutputChanged(ctx);
    this.lastReadTime = this.lastWriteTime = ticksInNanos();
    if (this.readerIdleTimeNanos > 0L)
      this.readerIdleTimeout = schedule(ctx, new ReaderIdleTimeoutTask(ctx), this.readerIdleTimeNanos, TimeUnit.NANOSECONDS); 
    if (this.writerIdleTimeNanos > 0L)
      this.writerIdleTimeout = schedule(ctx, new WriterIdleTimeoutTask(ctx), this.writerIdleTimeNanos, TimeUnit.NANOSECONDS); 
    if (this.allIdleTimeNanos > 0L)
      this.allIdleTimeout = schedule(ctx, new AllIdleTimeoutTask(ctx), this.allIdleTimeNanos, TimeUnit.NANOSECONDS); 
  }
  
  long ticksInNanos() {
    return System.nanoTime();
  }
  
  ScheduledFuture<?> schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit) {
    return (ScheduledFuture<?>)ctx.executor().schedule(task, delay, unit);
  }
  
  private void destroy() {
    this.state = 2;
    if (this.readerIdleTimeout != null) {
      this.readerIdleTimeout.cancel(false);
      this.readerIdleTimeout = null;
    } 
    if (this.writerIdleTimeout != null) {
      this.writerIdleTimeout.cancel(false);
      this.writerIdleTimeout = null;
    } 
    if (this.allIdleTimeout != null) {
      this.allIdleTimeout.cancel(false);
      this.allIdleTimeout = null;
    } 
  }
  
  protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
    ctx.fireUserEventTriggered(evt);
  }
  
  protected IdleStateEvent newIdleStateEvent(IdleState state, boolean first) {
    switch (state) {
      case ALL_IDLE:
        return first ? IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT : IdleStateEvent.ALL_IDLE_STATE_EVENT;
      case READER_IDLE:
        return first ? IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT : IdleStateEvent.READER_IDLE_STATE_EVENT;
      case WRITER_IDLE:
        return first ? IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT : IdleStateEvent.WRITER_IDLE_STATE_EVENT;
    } 
    throw new IllegalArgumentException("Unhandled: state=" + state + ", first=" + first);
  }
  
  private void initOutputChanged(ChannelHandlerContext ctx) {
    if (this.observeOutput) {
      Channel channel = ctx.channel();
      Channel.Unsafe unsafe = channel.unsafe();
      ChannelOutboundBuffer buf = unsafe.outboundBuffer();
      if (buf != null) {
        this.lastMessageHashCode = System.identityHashCode(buf.current());
        this.lastPendingWriteBytes = buf.totalPendingWriteBytes();
      } 
    } 
  }
  
  private boolean hasOutputChanged(ChannelHandlerContext ctx, boolean first) {
    if (this.observeOutput) {
      if (this.lastChangeCheckTimeStamp != this.lastWriteTime) {
        this.lastChangeCheckTimeStamp = this.lastWriteTime;
        if (!first)
          return true; 
      } 
      Channel channel = ctx.channel();
      Channel.Unsafe unsafe = channel.unsafe();
      ChannelOutboundBuffer buf = unsafe.outboundBuffer();
      if (buf != null) {
        int messageHashCode = System.identityHashCode(buf.current());
        long pendingWriteBytes = buf.totalPendingWriteBytes();
        if (messageHashCode != this.lastMessageHashCode || pendingWriteBytes != this.lastPendingWriteBytes) {
          this.lastMessageHashCode = messageHashCode;
          this.lastPendingWriteBytes = pendingWriteBytes;
          if (!first)
            return true; 
        } 
      } 
    } 
    return false;
  }
  
  private static abstract class AbstractIdleTask implements Runnable {
    private final ChannelHandlerContext ctx;
    
    AbstractIdleTask(ChannelHandlerContext ctx) {
      this.ctx = ctx;
    }
    
    public void run() {
      if (!this.ctx.channel().isOpen())
        return; 
      run(this.ctx);
    }
    
    protected abstract void run(ChannelHandlerContext param1ChannelHandlerContext);
  }
  
  private final class ReaderIdleTimeoutTask extends AbstractIdleTask {
    ReaderIdleTimeoutTask(ChannelHandlerContext ctx) {
      super(ctx);
    }
    
    protected void run(ChannelHandlerContext ctx) {
      long nextDelay = IdleStateHandler.this.readerIdleTimeNanos;
      if (!IdleStateHandler.this.reading)
        nextDelay -= IdleStateHandler.this.ticksInNanos() - IdleStateHandler.this.lastReadTime; 
      if (nextDelay <= 0L) {
        IdleStateHandler.this.readerIdleTimeout = IdleStateHandler.this.schedule(ctx, this, IdleStateHandler.this.readerIdleTimeNanos, TimeUnit.NANOSECONDS);
        boolean first = IdleStateHandler.this.firstReaderIdleEvent;
        IdleStateHandler.this.firstReaderIdleEvent = false;
        try {
          IdleStateEvent event = IdleStateHandler.this.newIdleStateEvent(IdleState.READER_IDLE, first);
          IdleStateHandler.this.channelIdle(ctx, event);
        } catch (Throwable t) {
          ctx.fireExceptionCaught(t);
        } 
      } else {
        IdleStateHandler.this.readerIdleTimeout = IdleStateHandler.this.schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
      } 
    }
  }
  
  private final class WriterIdleTimeoutTask extends AbstractIdleTask {
    WriterIdleTimeoutTask(ChannelHandlerContext ctx) {
      super(ctx);
    }
    
    protected void run(ChannelHandlerContext ctx) {
      long lastWriteTime = IdleStateHandler.this.lastWriteTime;
      long nextDelay = IdleStateHandler.this.writerIdleTimeNanos - IdleStateHandler.this.ticksInNanos() - lastWriteTime;
      if (nextDelay <= 0L) {
        IdleStateHandler.this.writerIdleTimeout = IdleStateHandler.this.schedule(ctx, this, IdleStateHandler.this.writerIdleTimeNanos, TimeUnit.NANOSECONDS);
        boolean first = IdleStateHandler.this.firstWriterIdleEvent;
        IdleStateHandler.this.firstWriterIdleEvent = false;
        try {
          if (IdleStateHandler.this.hasOutputChanged(ctx, first))
            return; 
          IdleStateEvent event = IdleStateHandler.this.newIdleStateEvent(IdleState.WRITER_IDLE, first);
          IdleStateHandler.this.channelIdle(ctx, event);
        } catch (Throwable t) {
          ctx.fireExceptionCaught(t);
        } 
      } else {
        IdleStateHandler.this.writerIdleTimeout = IdleStateHandler.this.schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
      } 
    }
  }
  
  private final class AllIdleTimeoutTask extends AbstractIdleTask {
    AllIdleTimeoutTask(ChannelHandlerContext ctx) {
      super(ctx);
    }
    
    protected void run(ChannelHandlerContext ctx) {
      long nextDelay = IdleStateHandler.this.allIdleTimeNanos;
      if (!IdleStateHandler.this.reading)
        nextDelay -= IdleStateHandler.this.ticksInNanos() - Math.max(IdleStateHandler.this.lastReadTime, IdleStateHandler.this.lastWriteTime); 
      if (nextDelay <= 0L) {
        IdleStateHandler.this.allIdleTimeout = IdleStateHandler.this.schedule(ctx, this, IdleStateHandler.this.allIdleTimeNanos, TimeUnit.NANOSECONDS);
        boolean first = IdleStateHandler.this.firstAllIdleEvent;
        IdleStateHandler.this.firstAllIdleEvent = false;
        try {
          if (IdleStateHandler.this.hasOutputChanged(ctx, first))
            return; 
          IdleStateEvent event = IdleStateHandler.this.newIdleStateEvent(IdleState.ALL_IDLE, first);
          IdleStateHandler.this.channelIdle(ctx, event);
        } catch (Throwable t) {
          ctx.fireExceptionCaught(t);
        } 
      } else {
        IdleStateHandler.this.allIdleTimeout = IdleStateHandler.this.schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS);
      } 
    }
  }
}

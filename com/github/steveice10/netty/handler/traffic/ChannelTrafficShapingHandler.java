package com.github.steveice10.netty.handler.traffic;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import java.util.ArrayDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChannelTrafficShapingHandler extends AbstractTrafficShapingHandler {
  private final ArrayDeque<ToSend> messagesQueue = new ArrayDeque<ToSend>();
  
  private long queueSize;
  
  public ChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval, long maxTime) {
    super(writeLimit, readLimit, checkInterval, maxTime);
  }
  
  public ChannelTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval) {
    super(writeLimit, readLimit, checkInterval);
  }
  
  public ChannelTrafficShapingHandler(long writeLimit, long readLimit) {
    super(writeLimit, readLimit);
  }
  
  public ChannelTrafficShapingHandler(long checkInterval) {
    super(checkInterval);
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    TrafficCounter trafficCounter = new TrafficCounter(this, (ScheduledExecutorService)ctx.executor(), "ChannelTC" + ctx.channel().hashCode(), this.checkInterval);
    setTrafficCounter(trafficCounter);
    trafficCounter.start();
    super.handlerAdded(ctx);
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    this.trafficCounter.stop();
    synchronized (this) {
      if (ctx.channel().isActive()) {
        for (ToSend toSend : this.messagesQueue) {
          long size = calculateSize(toSend.toSend);
          this.trafficCounter.bytesRealWriteFlowControl(size);
          this.queueSize -= size;
          ctx.write(toSend.toSend, toSend.promise);
        } 
      } else {
        for (ToSend toSend : this.messagesQueue) {
          if (toSend.toSend instanceof ByteBuf)
            ((ByteBuf)toSend.toSend).release(); 
        } 
      } 
      this.messagesQueue.clear();
    } 
    releaseWriteSuspended(ctx);
    releaseReadSuspended(ctx);
    super.handlerRemoved(ctx);
  }
  
  private static final class ToSend {
    final long relativeTimeAction;
    
    final Object toSend;
    
    final ChannelPromise promise;
    
    private ToSend(long delay, Object toSend, ChannelPromise promise) {
      this.relativeTimeAction = delay;
      this.toSend = toSend;
      this.promise = promise;
    }
  }
  
  void submitWrite(final ChannelHandlerContext ctx, Object msg, long size, long delay, long now, ChannelPromise promise) {
    ToSend newToSend;
    synchronized (this) {
      if (delay == 0L && this.messagesQueue.isEmpty()) {
        this.trafficCounter.bytesRealWriteFlowControl(size);
        ctx.write(msg, promise);
        return;
      } 
      newToSend = new ToSend(delay + now, msg, promise);
      this.messagesQueue.addLast(newToSend);
      this.queueSize += size;
      checkWriteSuspend(ctx, delay, this.queueSize);
    } 
    final long futureNow = newToSend.relativeTimeAction;
    ctx.executor().schedule(new Runnable() {
          public void run() {
            ChannelTrafficShapingHandler.this.sendAllValid(ctx, futureNow);
          }
        }delay, TimeUnit.MILLISECONDS);
  }
  
  private void sendAllValid(ChannelHandlerContext ctx, long now) {
    synchronized (this) {
      ToSend newToSend = this.messagesQueue.pollFirst();
      for (; newToSend != null; newToSend = this.messagesQueue.pollFirst()) {
        if (newToSend.relativeTimeAction <= now) {
          long size = calculateSize(newToSend.toSend);
          this.trafficCounter.bytesRealWriteFlowControl(size);
          this.queueSize -= size;
          ctx.write(newToSend.toSend, newToSend.promise);
        } else {
          this.messagesQueue.addFirst(newToSend);
          break;
        } 
      } 
      if (this.messagesQueue.isEmpty())
        releaseWriteSuspended(ctx); 
    } 
    ctx.flush();
  }
  
  public long queueSize() {
    return this.queueSize;
  }
}

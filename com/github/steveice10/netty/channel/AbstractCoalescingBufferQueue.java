package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.CompositeByteBuf;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.util.ArrayDeque;

public abstract class AbstractCoalescingBufferQueue {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractCoalescingBufferQueue.class);
  
  private final ArrayDeque<Object> bufAndListenerPairs;
  
  private final PendingBytesTracker tracker;
  
  private int readableBytes;
  
  protected AbstractCoalescingBufferQueue(Channel channel, int initSize) {
    this.bufAndListenerPairs = new ArrayDeque(initSize);
    this.tracker = (channel == null) ? null : PendingBytesTracker.newTracker(channel);
  }
  
  public final void addFirst(ByteBuf buf, ChannelPromise promise) {
    addFirst(buf, toChannelFutureListener(promise));
  }
  
  private void addFirst(ByteBuf buf, ChannelFutureListener listener) {
    if (listener != null)
      this.bufAndListenerPairs.addFirst(listener); 
    this.bufAndListenerPairs.addFirst(buf);
    incrementReadableBytes(buf.readableBytes());
  }
  
  public final void add(ByteBuf buf) {
    add(buf, (ChannelFutureListener)null);
  }
  
  public final void add(ByteBuf buf, ChannelPromise promise) {
    add(buf, toChannelFutureListener(promise));
  }
  
  public final void add(ByteBuf buf, ChannelFutureListener listener) {
    this.bufAndListenerPairs.add(buf);
    if (listener != null)
      this.bufAndListenerPairs.add(listener); 
    incrementReadableBytes(buf.readableBytes());
  }
  
  public final ByteBuf removeFirst(ChannelPromise aggregatePromise) {
    Object entry = this.bufAndListenerPairs.poll();
    if (entry == null)
      return null; 
    assert entry instanceof ByteBuf;
    ByteBuf result = (ByteBuf)entry;
    decrementReadableBytes(result.readableBytes());
    entry = this.bufAndListenerPairs.peek();
    if (entry instanceof ChannelFutureListener) {
      aggregatePromise.addListener((ChannelFutureListener)entry);
      this.bufAndListenerPairs.poll();
    } 
    return result;
  }
  
  public final ByteBuf remove(ByteBufAllocator alloc, int bytes, ChannelPromise aggregatePromise) {
    ObjectUtil.checkPositiveOrZero(bytes, "bytes");
    ObjectUtil.checkNotNull(aggregatePromise, "aggregatePromise");
    if (this.bufAndListenerPairs.isEmpty())
      return removeEmptyValue(); 
    bytes = Math.min(bytes, this.readableBytes);
    ByteBuf toReturn = null;
    ByteBuf entryBuffer = null;
    int originalBytes = bytes;
    try {
      while (true) {
        Object entry = this.bufAndListenerPairs.poll();
        if (entry == null)
          break; 
        if (entry instanceof ChannelFutureListener) {
          aggregatePromise.addListener((ChannelFutureListener)entry);
          continue;
        } 
        entryBuffer = (ByteBuf)entry;
        if (entryBuffer.readableBytes() > bytes) {
          this.bufAndListenerPairs.addFirst(entryBuffer);
          if (bytes > 0) {
            entryBuffer = entryBuffer.readRetainedSlice(bytes);
            toReturn = (toReturn == null) ? composeFirst(alloc, entryBuffer) : compose(alloc, toReturn, entryBuffer);
            bytes = 0;
          } 
          break;
        } 
        bytes -= entryBuffer.readableBytes();
        toReturn = (toReturn == null) ? composeFirst(alloc, entryBuffer) : compose(alloc, toReturn, entryBuffer);
        entryBuffer = null;
      } 
    } catch (Throwable cause) {
      ReferenceCountUtil.safeRelease(entryBuffer);
      ReferenceCountUtil.safeRelease(toReturn);
      aggregatePromise.setFailure(cause);
      PlatformDependent.throwException(cause);
    } 
    decrementReadableBytes(originalBytes - bytes);
    return toReturn;
  }
  
  public final int readableBytes() {
    return this.readableBytes;
  }
  
  public final boolean isEmpty() {
    return this.bufAndListenerPairs.isEmpty();
  }
  
  public final void releaseAndFailAll(ChannelOutboundInvoker invoker, Throwable cause) {
    releaseAndCompleteAll(invoker.newFailedFuture(cause));
  }
  
  public final void copyTo(AbstractCoalescingBufferQueue dest) {
    dest.bufAndListenerPairs.addAll(this.bufAndListenerPairs);
    dest.incrementReadableBytes(this.readableBytes);
  }
  
  public final void writeAndRemoveAll(ChannelHandlerContext ctx) {
    decrementReadableBytes(this.readableBytes);
    Throwable pending = null;
    ByteBuf previousBuf = null;
    while (true) {
      Object entry = this.bufAndListenerPairs.poll();
      try {
        if (entry == null) {
          if (previousBuf != null)
            ctx.write(previousBuf, ctx.voidPromise()); 
          break;
        } 
        if (entry instanceof ByteBuf) {
          if (previousBuf != null)
            ctx.write(previousBuf, ctx.voidPromise()); 
          previousBuf = (ByteBuf)entry;
          continue;
        } 
        if (entry instanceof ChannelPromise) {
          ctx.write(previousBuf, (ChannelPromise)entry);
          previousBuf = null;
          continue;
        } 
        ctx.write(previousBuf).addListener((ChannelFutureListener)entry);
        previousBuf = null;
      } catch (Throwable t) {
        if (pending == null) {
          pending = t;
          continue;
        } 
        logger.info("Throwable being suppressed because Throwable {} is already pending", pending, t);
      } 
    } 
    if (pending != null)
      throw new IllegalStateException(pending); 
  }
  
  protected abstract ByteBuf compose(ByteBufAllocator paramByteBufAllocator, ByteBuf paramByteBuf1, ByteBuf paramByteBuf2);
  
  protected final ByteBuf composeIntoComposite(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf next) {
    CompositeByteBuf composite = alloc.compositeBuffer(size() + 2);
    try {
      composite.addComponent(true, cumulation);
      composite.addComponent(true, next);
    } catch (Throwable cause) {
      composite.release();
      ReferenceCountUtil.safeRelease(next);
      PlatformDependent.throwException(cause);
    } 
    return (ByteBuf)composite;
  }
  
  protected final ByteBuf copyAndCompose(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf next) {
    ByteBuf newCumulation = alloc.ioBuffer(cumulation.readableBytes() + next.readableBytes());
    try {
      newCumulation.writeBytes(cumulation).writeBytes(next);
    } catch (Throwable cause) {
      newCumulation.release();
      ReferenceCountUtil.safeRelease(next);
      PlatformDependent.throwException(cause);
    } 
    cumulation.release();
    next.release();
    return newCumulation;
  }
  
  protected ByteBuf composeFirst(ByteBufAllocator allocator, ByteBuf first) {
    return first;
  }
  
  protected abstract ByteBuf removeEmptyValue();
  
  protected final int size() {
    return this.bufAndListenerPairs.size();
  }
  
  private void releaseAndCompleteAll(ChannelFuture future) {
    decrementReadableBytes(this.readableBytes);
    Throwable pending = null;
    while (true) {
      Object entry = this.bufAndListenerPairs.poll();
      if (entry == null)
        break; 
      try {
        if (entry instanceof ByteBuf) {
          ReferenceCountUtil.safeRelease(entry);
          continue;
        } 
        ((ChannelFutureListener)entry).operationComplete(future);
      } catch (Throwable t) {
        if (pending == null) {
          pending = t;
          continue;
        } 
        logger.info("Throwable being suppressed because Throwable {} is already pending", pending, t);
      } 
    } 
    if (pending != null)
      throw new IllegalStateException(pending); 
  }
  
  private void incrementReadableBytes(int increment) {
    int nextReadableBytes = this.readableBytes + increment;
    if (nextReadableBytes < this.readableBytes)
      throw new IllegalStateException("buffer queue length overflow: " + this.readableBytes + " + " + increment); 
    this.readableBytes = nextReadableBytes;
    if (this.tracker != null)
      this.tracker.incrementPendingOutboundBytes(increment); 
  }
  
  private void decrementReadableBytes(int decrement) {
    this.readableBytes -= decrement;
    assert this.readableBytes >= 0;
    if (this.tracker != null)
      this.tracker.decrementPendingOutboundBytes(decrement); 
  }
  
  private static ChannelFutureListener toChannelFutureListener(ChannelPromise promise) {
    return promise.isVoid() ? null : new DelegatingChannelPromiseNotifier(promise);
  }
}

package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.Recycler;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.PromiseCombiner;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;

public final class PendingWriteQueue {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(PendingWriteQueue.class);
  
  private static final int PENDING_WRITE_OVERHEAD = SystemPropertyUtil.getInt("com.github.steveice10.netty.transport.pendingWriteSizeOverhead", 64);
  
  private final ChannelHandlerContext ctx;
  
  private final PendingBytesTracker tracker;
  
  private PendingWrite head;
  
  private PendingWrite tail;
  
  private int size;
  
  private long bytes;
  
  public PendingWriteQueue(ChannelHandlerContext ctx) {
    this.tracker = PendingBytesTracker.newTracker(ctx.channel());
    this.ctx = ctx;
  }
  
  public boolean isEmpty() {
    assert this.ctx.executor().inEventLoop();
    return (this.head == null);
  }
  
  public int size() {
    assert this.ctx.executor().inEventLoop();
    return this.size;
  }
  
  public long bytes() {
    assert this.ctx.executor().inEventLoop();
    return this.bytes;
  }
  
  private int size(Object msg) {
    int messageSize = this.tracker.size(msg);
    if (messageSize < 0)
      messageSize = 0; 
    return messageSize + PENDING_WRITE_OVERHEAD;
  }
  
  public void add(Object msg, ChannelPromise promise) {
    assert this.ctx.executor().inEventLoop();
    if (msg == null)
      throw new NullPointerException("msg"); 
    if (promise == null)
      throw new NullPointerException("promise"); 
    int messageSize = size(msg);
    PendingWrite write = PendingWrite.newInstance(msg, messageSize, promise);
    PendingWrite currentTail = this.tail;
    if (currentTail == null) {
      this.tail = this.head = write;
    } else {
      currentTail.next = write;
      this.tail = write;
    } 
    this.size++;
    this.bytes += messageSize;
    this.tracker.incrementPendingOutboundBytes(write.size);
  }
  
  public ChannelFuture removeAndWriteAll() {
    assert this.ctx.executor().inEventLoop();
    if (isEmpty())
      return null; 
    ChannelPromise p = this.ctx.newPromise();
    PromiseCombiner combiner = new PromiseCombiner();
    try {
      for (PendingWrite write = this.head; write != null; write = this.head) {
        this.head = this.tail = null;
        this.size = 0;
        this.bytes = 0L;
        while (write != null) {
          PendingWrite next = write.next;
          Object msg = write.msg;
          ChannelPromise promise = write.promise;
          recycle(write, false);
          if (!(promise instanceof VoidChannelPromise))
            combiner.add(promise); 
          this.ctx.write(msg, promise);
          write = next;
        } 
      } 
      combiner.finish(p);
    } catch (Throwable cause) {
      p.setFailure(cause);
    } 
    assertEmpty();
    return p;
  }
  
  public void removeAndFailAll(Throwable cause) {
    assert this.ctx.executor().inEventLoop();
    if (cause == null)
      throw new NullPointerException("cause"); 
    for (PendingWrite write = this.head; write != null; write = this.head) {
      this.head = this.tail = null;
      this.size = 0;
      this.bytes = 0L;
      while (write != null) {
        PendingWrite next = write.next;
        ReferenceCountUtil.safeRelease(write.msg);
        ChannelPromise promise = write.promise;
        recycle(write, false);
        safeFail(promise, cause);
        write = next;
      } 
    } 
    assertEmpty();
  }
  
  public void removeAndFail(Throwable cause) {
    assert this.ctx.executor().inEventLoop();
    if (cause == null)
      throw new NullPointerException("cause"); 
    PendingWrite write = this.head;
    if (write == null)
      return; 
    ReferenceCountUtil.safeRelease(write.msg);
    ChannelPromise promise = write.promise;
    safeFail(promise, cause);
    recycle(write, true);
  }
  
  private void assertEmpty() {
    assert this.tail == null && this.head == null && this.size == 0;
  }
  
  public ChannelFuture removeAndWrite() {
    assert this.ctx.executor().inEventLoop();
    PendingWrite write = this.head;
    if (write == null)
      return null; 
    Object msg = write.msg;
    ChannelPromise promise = write.promise;
    recycle(write, true);
    return this.ctx.write(msg, promise);
  }
  
  public ChannelPromise remove() {
    assert this.ctx.executor().inEventLoop();
    PendingWrite write = this.head;
    if (write == null)
      return null; 
    ChannelPromise promise = write.promise;
    ReferenceCountUtil.safeRelease(write.msg);
    recycle(write, true);
    return promise;
  }
  
  public Object current() {
    assert this.ctx.executor().inEventLoop();
    PendingWrite write = this.head;
    if (write == null)
      return null; 
    return write.msg;
  }
  
  private void recycle(PendingWrite write, boolean update) {
    PendingWrite next = write.next;
    long writeSize = write.size;
    if (update)
      if (next == null) {
        this.head = this.tail = null;
        this.size = 0;
        this.bytes = 0L;
      } else {
        this.head = next;
        this.size--;
        this.bytes -= writeSize;
        assert this.size > 0 && this.bytes >= 0L;
      }  
    write.recycle();
    this.tracker.decrementPendingOutboundBytes(writeSize);
  }
  
  private static void safeFail(ChannelPromise promise, Throwable cause) {
    if (!(promise instanceof VoidChannelPromise) && !promise.tryFailure(cause))
      logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, cause); 
  }
  
  static final class PendingWrite {
    private static final Recycler<PendingWrite> RECYCLER = new Recycler<PendingWrite>() {
        protected PendingWriteQueue.PendingWrite newObject(Recycler.Handle<PendingWriteQueue.PendingWrite> handle) {
          return new PendingWriteQueue.PendingWrite(handle);
        }
      };
    
    private final Recycler.Handle<PendingWrite> handle;
    
    private PendingWrite next;
    
    private long size;
    
    private ChannelPromise promise;
    
    private Object msg;
    
    private PendingWrite(Recycler.Handle<PendingWrite> handle) {
      this.handle = handle;
    }
    
    static PendingWrite newInstance(Object msg, int size, ChannelPromise promise) {
      PendingWrite write = (PendingWrite)RECYCLER.get();
      write.size = size;
      write.msg = msg;
      write.promise = promise;
      return write;
    }
    
    private void recycle() {
      this.size = 0L;
      this.next = null;
      this.msg = null;
      this.promise = null;
      this.handle.recycle(this);
    }
  }
}

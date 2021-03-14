package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.Recycler;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.FastThreadLocal;
import com.github.steveice10.netty.util.internal.InternalThreadLocalMap;
import com.github.steveice10.netty.util.internal.PromiseNotificationUtil;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public final class ChannelOutboundBuffer {
  static final int CHANNEL_OUTBOUND_BUFFER_ENTRY_OVERHEAD = SystemPropertyUtil.getInt("com.github.steveice10.netty.transport.outboundBufferEntrySizeOverhead", 96);
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelOutboundBuffer.class);
  
  private static final FastThreadLocal<ByteBuffer[]> NIO_BUFFERS = new FastThreadLocal<ByteBuffer[]>() {
      protected ByteBuffer[] initialValue() throws Exception {
        return new ByteBuffer[1024];
      }
    };
  
  private final Channel channel;
  
  private Entry flushedEntry;
  
  private Entry unflushedEntry;
  
  private Entry tailEntry;
  
  private int flushed;
  
  private int nioBufferCount;
  
  private long nioBufferSize;
  
  private boolean inFail;
  
  private static final AtomicLongFieldUpdater<ChannelOutboundBuffer> TOTAL_PENDING_SIZE_UPDATER = AtomicLongFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "totalPendingSize");
  
  private volatile long totalPendingSize;
  
  private static final AtomicIntegerFieldUpdater<ChannelOutboundBuffer> UNWRITABLE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(ChannelOutboundBuffer.class, "unwritable");
  
  private volatile int unwritable;
  
  private volatile Runnable fireChannelWritabilityChangedTask;
  
  ChannelOutboundBuffer(AbstractChannel channel) {
    this.channel = channel;
  }
  
  public void addMessage(Object msg, int size, ChannelPromise promise) {
    Entry entry = Entry.newInstance(msg, size, total(msg), promise);
    if (this.tailEntry == null) {
      this.flushedEntry = null;
    } else {
      Entry tail = this.tailEntry;
      tail.next = entry;
    } 
    this.tailEntry = entry;
    if (this.unflushedEntry == null)
      this.unflushedEntry = entry; 
    incrementPendingOutboundBytes(entry.pendingSize, false);
  }
  
  public void addFlush() {
    Entry entry = this.unflushedEntry;
    if (entry != null) {
      if (this.flushedEntry == null)
        this.flushedEntry = entry; 
      do {
        this.flushed++;
        if (!entry.promise.setUncancellable()) {
          int pending = entry.cancel();
          decrementPendingOutboundBytes(pending, false, true);
        } 
        entry = entry.next;
      } while (entry != null);
      this.unflushedEntry = null;
    } 
  }
  
  void incrementPendingOutboundBytes(long size) {
    incrementPendingOutboundBytes(size, true);
  }
  
  private void incrementPendingOutboundBytes(long size, boolean invokeLater) {
    if (size == 0L)
      return; 
    long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, size);
    if (newWriteBufferSize > this.channel.config().getWriteBufferHighWaterMark())
      setUnwritable(invokeLater); 
  }
  
  void decrementPendingOutboundBytes(long size) {
    decrementPendingOutboundBytes(size, true, true);
  }
  
  private void decrementPendingOutboundBytes(long size, boolean invokeLater, boolean notifyWritability) {
    if (size == 0L)
      return; 
    long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);
    if (notifyWritability && newWriteBufferSize < this.channel.config().getWriteBufferLowWaterMark())
      setWritable(invokeLater); 
  }
  
  private static long total(Object msg) {
    if (msg instanceof ByteBuf)
      return ((ByteBuf)msg).readableBytes(); 
    if (msg instanceof FileRegion)
      return ((FileRegion)msg).count(); 
    if (msg instanceof ByteBufHolder)
      return ((ByteBufHolder)msg).content().readableBytes(); 
    return -1L;
  }
  
  public Object current() {
    Entry entry = this.flushedEntry;
    if (entry == null)
      return null; 
    return entry.msg;
  }
  
  public void progress(long amount) {
    Entry e = this.flushedEntry;
    assert e != null;
    ChannelPromise p = e.promise;
    if (p instanceof ChannelProgressivePromise) {
      long progress = e.progress + amount;
      e.progress = progress;
      ((ChannelProgressivePromise)p).tryProgress(progress, e.total);
    } 
  }
  
  public boolean remove() {
    Entry e = this.flushedEntry;
    if (e == null) {
      clearNioBuffers();
      return false;
    } 
    Object msg = e.msg;
    ChannelPromise promise = e.promise;
    int size = e.pendingSize;
    removeEntry(e);
    if (!e.cancelled) {
      ReferenceCountUtil.safeRelease(msg);
      safeSuccess(promise);
      decrementPendingOutboundBytes(size, false, true);
    } 
    e.recycle();
    return true;
  }
  
  public boolean remove(Throwable cause) {
    return remove0(cause, true);
  }
  
  private boolean remove0(Throwable cause, boolean notifyWritability) {
    Entry e = this.flushedEntry;
    if (e == null) {
      clearNioBuffers();
      return false;
    } 
    Object msg = e.msg;
    ChannelPromise promise = e.promise;
    int size = e.pendingSize;
    removeEntry(e);
    if (!e.cancelled) {
      ReferenceCountUtil.safeRelease(msg);
      safeFail(promise, cause);
      decrementPendingOutboundBytes(size, false, notifyWritability);
    } 
    e.recycle();
    return true;
  }
  
  private void removeEntry(Entry e) {
    if (--this.flushed == 0) {
      this.flushedEntry = null;
      if (e == this.tailEntry) {
        this.tailEntry = null;
        this.unflushedEntry = null;
      } 
    } else {
      this.flushedEntry = e.next;
    } 
  }
  
  public void removeBytes(long writtenBytes) {
    while (true) {
      Object msg = current();
      if (!(msg instanceof ByteBuf)) {
        assert writtenBytes == 0L;
        break;
      } 
      ByteBuf buf = (ByteBuf)msg;
      int readerIndex = buf.readerIndex();
      int readableBytes = buf.writerIndex() - readerIndex;
      if (readableBytes <= writtenBytes) {
        if (writtenBytes != 0L) {
          progress(readableBytes);
          writtenBytes -= readableBytes;
        } 
        remove();
        continue;
      } 
      if (writtenBytes != 0L) {
        buf.readerIndex(readerIndex + (int)writtenBytes);
        progress(writtenBytes);
      } 
      break;
    } 
    clearNioBuffers();
  }
  
  private void clearNioBuffers() {
    int count = this.nioBufferCount;
    if (count > 0) {
      this.nioBufferCount = 0;
      Arrays.fill((Object[])NIO_BUFFERS.get(), 0, count, (Object)null);
    } 
  }
  
  public ByteBuffer[] nioBuffers() {
    return nioBuffers(2147483647, 2147483647L);
  }
  
  public ByteBuffer[] nioBuffers(int maxCount, long maxBytes) {
    assert maxCount > 0;
    assert maxBytes > 0L;
    long nioBufferSize = 0L;
    int nioBufferCount = 0;
    InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
    ByteBuffer[] nioBuffers = (ByteBuffer[])NIO_BUFFERS.get(threadLocalMap);
    Entry entry = this.flushedEntry;
    while (isFlushedEntry(entry) && entry.msg instanceof ByteBuf) {
      if (!entry.cancelled) {
        ByteBuf buf = (ByteBuf)entry.msg;
        int readerIndex = buf.readerIndex();
        int readableBytes = buf.writerIndex() - readerIndex;
        if (readableBytes > 0) {
          if (maxBytes - readableBytes < nioBufferSize && nioBufferCount != 0)
            break; 
          nioBufferSize += readableBytes;
          int count = entry.count;
          if (count == -1)
            entry.count = count = buf.nioBufferCount(); 
          int neededSpace = Math.min(maxCount, nioBufferCount + count);
          if (neededSpace > nioBuffers.length) {
            nioBuffers = expandNioBufferArray(nioBuffers, neededSpace, nioBufferCount);
            NIO_BUFFERS.set(threadLocalMap, nioBuffers);
          } 
          if (count == 1) {
            ByteBuffer nioBuf = entry.buf;
            if (nioBuf == null)
              entry.buf = nioBuf = buf.internalNioBuffer(readerIndex, readableBytes); 
            nioBuffers[nioBufferCount++] = nioBuf;
          } else {
            ByteBuffer[] nioBufs = entry.bufs;
            if (nioBufs == null)
              entry.bufs = nioBufs = buf.nioBuffers(); 
            for (int i = 0; i < nioBufs.length && nioBufferCount < maxCount; i++) {
              ByteBuffer nioBuf = nioBufs[i];
              if (nioBuf == null)
                break; 
              if (nioBuf.hasRemaining())
                nioBuffers[nioBufferCount++] = nioBuf; 
            } 
          } 
          if (nioBufferCount == maxCount)
            break; 
        } 
      } 
      entry = entry.next;
    } 
    this.nioBufferCount = nioBufferCount;
    this.nioBufferSize = nioBufferSize;
    return nioBuffers;
  }
  
  private static ByteBuffer[] expandNioBufferArray(ByteBuffer[] array, int neededSpace, int size) {
    int newCapacity = array.length;
    do {
      newCapacity <<= 1;
      if (newCapacity < 0)
        throw new IllegalStateException(); 
    } while (neededSpace > newCapacity);
    ByteBuffer[] newArray = new ByteBuffer[newCapacity];
    System.arraycopy(array, 0, newArray, 0, size);
    return newArray;
  }
  
  public int nioBufferCount() {
    return this.nioBufferCount;
  }
  
  public long nioBufferSize() {
    return this.nioBufferSize;
  }
  
  public boolean isWritable() {
    return (this.unwritable == 0);
  }
  
  public boolean getUserDefinedWritability(int index) {
    return ((this.unwritable & writabilityMask(index)) == 0);
  }
  
  public void setUserDefinedWritability(int index, boolean writable) {
    if (writable) {
      setUserDefinedWritability(index);
    } else {
      clearUserDefinedWritability(index);
    } 
  }
  
  private void setUserDefinedWritability(int index) {
    int mask = writabilityMask(index) ^ 0xFFFFFFFF;
    while (true) {
      int oldValue = this.unwritable;
      int newValue = oldValue & mask;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue != 0 && newValue == 0)
          fireChannelWritabilityChanged(true); 
        return;
      } 
    } 
  }
  
  private void clearUserDefinedWritability(int index) {
    int mask = writabilityMask(index);
    while (true) {
      int oldValue = this.unwritable;
      int newValue = oldValue | mask;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue == 0 && newValue != 0)
          fireChannelWritabilityChanged(true); 
        return;
      } 
    } 
  }
  
  private static int writabilityMask(int index) {
    if (index < 1 || index > 31)
      throw new IllegalArgumentException("index: " + index + " (expected: 1~31)"); 
    return 1 << index;
  }
  
  private void setWritable(boolean invokeLater) {
    while (true) {
      int oldValue = this.unwritable;
      int newValue = oldValue & 0xFFFFFFFE;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue != 0 && newValue == 0)
          fireChannelWritabilityChanged(invokeLater); 
        return;
      } 
    } 
  }
  
  private void setUnwritable(boolean invokeLater) {
    while (true) {
      int oldValue = this.unwritable;
      int newValue = oldValue | 0x1;
      if (UNWRITABLE_UPDATER.compareAndSet(this, oldValue, newValue)) {
        if (oldValue == 0 && newValue != 0)
          fireChannelWritabilityChanged(invokeLater); 
        return;
      } 
    } 
  }
  
  private void fireChannelWritabilityChanged(boolean invokeLater) {
    final ChannelPipeline pipeline = this.channel.pipeline();
    if (invokeLater) {
      Runnable task = this.fireChannelWritabilityChangedTask;
      if (task == null)
        this.fireChannelWritabilityChangedTask = task = new Runnable() {
            public void run() {
              pipeline.fireChannelWritabilityChanged();
            }
          }; 
      this.channel.eventLoop().execute(task);
    } else {
      pipeline.fireChannelWritabilityChanged();
    } 
  }
  
  public int size() {
    return this.flushed;
  }
  
  public boolean isEmpty() {
    return (this.flushed == 0);
  }
  
  void failFlushed(Throwable cause, boolean notify) {
    if (this.inFail)
      return; 
    try {
      this.inFail = true;
      do {
      
      } while (remove0(cause, notify));
    } finally {
      this.inFail = false;
    } 
  }
  
  void close(final Throwable cause, final boolean allowChannelOpen) {
    if (this.inFail) {
      this.channel.eventLoop().execute(new Runnable() {
            public void run() {
              ChannelOutboundBuffer.this.close(cause, allowChannelOpen);
            }
          });
      return;
    } 
    this.inFail = true;
    if (!allowChannelOpen && this.channel.isOpen())
      throw new IllegalStateException("close() must be invoked after the channel is closed."); 
    if (!isEmpty())
      throw new IllegalStateException("close() must be invoked after all flushed writes are handled."); 
    try {
      Entry e = this.unflushedEntry;
      while (e != null) {
        int size = e.pendingSize;
        TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, -size);
        if (!e.cancelled) {
          ReferenceCountUtil.safeRelease(e.msg);
          safeFail(e.promise, cause);
        } 
        e = e.recycleAndGetNext();
      } 
    } finally {
      this.inFail = false;
    } 
    clearNioBuffers();
  }
  
  void close(ClosedChannelException cause) {
    close(cause, false);
  }
  
  private static void safeSuccess(ChannelPromise promise) {
    PromiseNotificationUtil.trySuccess(promise, null, (promise instanceof VoidChannelPromise) ? null : logger);
  }
  
  private static void safeFail(ChannelPromise promise, Throwable cause) {
    PromiseNotificationUtil.tryFailure(promise, cause, (promise instanceof VoidChannelPromise) ? null : logger);
  }
  
  @Deprecated
  public void recycle() {}
  
  public long totalPendingWriteBytes() {
    return this.totalPendingSize;
  }
  
  public long bytesBeforeUnwritable() {
    long bytes = this.channel.config().getWriteBufferHighWaterMark() - this.totalPendingSize;
    if (bytes > 0L)
      return isWritable() ? bytes : 0L; 
    return 0L;
  }
  
  public long bytesBeforeWritable() {
    long bytes = this.totalPendingSize - this.channel.config().getWriteBufferLowWaterMark();
    if (bytes > 0L)
      return isWritable() ? 0L : bytes; 
    return 0L;
  }
  
  public void forEachFlushedMessage(MessageProcessor processor) throws Exception {
    if (processor == null)
      throw new NullPointerException("processor"); 
    Entry entry = this.flushedEntry;
    if (entry == null)
      return; 
    do {
      if (!entry.cancelled && 
        !processor.processMessage(entry.msg))
        return; 
      entry = entry.next;
    } while (isFlushedEntry(entry));
  }
  
  private boolean isFlushedEntry(Entry e) {
    return (e != null && e != this.unflushedEntry);
  }
  
  public static interface MessageProcessor {
    boolean processMessage(Object param1Object) throws Exception;
  }
  
  static final class Entry {
    private static final Recycler<Entry> RECYCLER = new Recycler<Entry>() {
        protected ChannelOutboundBuffer.Entry newObject(Recycler.Handle<ChannelOutboundBuffer.Entry> handle) {
          return new ChannelOutboundBuffer.Entry(handle);
        }
      };
    
    private final Recycler.Handle<Entry> handle;
    
    Entry next;
    
    Object msg;
    
    ByteBuffer[] bufs;
    
    ByteBuffer buf;
    
    ChannelPromise promise;
    
    long progress;
    
    long total;
    
    int pendingSize;
    
    int count = -1;
    
    boolean cancelled;
    
    private Entry(Recycler.Handle<Entry> handle) {
      this.handle = handle;
    }
    
    static Entry newInstance(Object msg, int size, long total, ChannelPromise promise) {
      Entry entry = (Entry)RECYCLER.get();
      entry.msg = msg;
      entry.pendingSize = size + ChannelOutboundBuffer.CHANNEL_OUTBOUND_BUFFER_ENTRY_OVERHEAD;
      entry.total = total;
      entry.promise = promise;
      return entry;
    }
    
    int cancel() {
      if (!this.cancelled) {
        this.cancelled = true;
        int pSize = this.pendingSize;
        ReferenceCountUtil.safeRelease(this.msg);
        this.msg = Unpooled.EMPTY_BUFFER;
        this.pendingSize = 0;
        this.total = 0L;
        this.progress = 0L;
        this.bufs = null;
        this.buf = null;
        return pSize;
      } 
      return 0;
    }
    
    void recycle() {
      this.next = null;
      this.bufs = null;
      this.buf = null;
      this.msg = null;
      this.promise = null;
      this.progress = 0L;
      this.total = 0L;
      this.pendingSize = 0;
      this.count = -1;
      this.cancelled = false;
      this.handle.recycle(this);
    }
    
    Entry recycleAndGetNext() {
      Entry next = this.next;
      recycle();
      return next;
    }
  }
}

package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.internal.LongCounter;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

abstract class PoolArena<T> implements PoolArenaMetric {
  static final boolean HAS_UNSAFE = PlatformDependent.hasUnsafe();
  
  static final int numTinySubpagePools = 32;
  
  final PooledByteBufAllocator parent;
  
  private final int maxOrder;
  
  final int pageSize;
  
  final int pageShifts;
  
  final int chunkSize;
  
  final int subpageOverflowMask;
  
  final int numSmallSubpagePools;
  
  final int directMemoryCacheAlignment;
  
  final int directMemoryCacheAlignmentMask;
  
  private final PoolSubpage<T>[] tinySubpagePools;
  
  private final PoolSubpage<T>[] smallSubpagePools;
  
  private final PoolChunkList<T> q050;
  
  private final PoolChunkList<T> q025;
  
  private final PoolChunkList<T> q000;
  
  private final PoolChunkList<T> qInit;
  
  private final PoolChunkList<T> q075;
  
  private final PoolChunkList<T> q100;
  
  private final List<PoolChunkListMetric> chunkListMetrics;
  
  private long allocationsNormal;
  
  enum SizeClass {
    Tiny, Small, Normal;
  }
  
  private final LongCounter allocationsTiny = PlatformDependent.newLongCounter();
  
  private final LongCounter allocationsSmall = PlatformDependent.newLongCounter();
  
  private final LongCounter allocationsHuge = PlatformDependent.newLongCounter();
  
  private final LongCounter activeBytesHuge = PlatformDependent.newLongCounter();
  
  private long deallocationsTiny;
  
  private long deallocationsSmall;
  
  private long deallocationsNormal;
  
  private final LongCounter deallocationsHuge = PlatformDependent.newLongCounter();
  
  final AtomicInteger numThreadCaches = new AtomicInteger();
  
  protected PoolArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize, int cacheAlignment) {
    this.parent = parent;
    this.pageSize = pageSize;
    this.maxOrder = maxOrder;
    this.pageShifts = pageShifts;
    this.chunkSize = chunkSize;
    this.directMemoryCacheAlignment = cacheAlignment;
    this.directMemoryCacheAlignmentMask = cacheAlignment - 1;
    this.subpageOverflowMask = pageSize - 1 ^ 0xFFFFFFFF;
    this.tinySubpagePools = newSubpagePoolArray(32);
    int i;
    for (i = 0; i < this.tinySubpagePools.length; i++)
      this.tinySubpagePools[i] = newSubpagePoolHead(pageSize); 
    this.numSmallSubpagePools = pageShifts - 9;
    this.smallSubpagePools = newSubpagePoolArray(this.numSmallSubpagePools);
    for (i = 0; i < this.smallSubpagePools.length; i++)
      this.smallSubpagePools[i] = newSubpagePoolHead(pageSize); 
    this.q100 = new PoolChunkList<T>(this, null, 100, 2147483647, chunkSize);
    this.q075 = new PoolChunkList<T>(this, this.q100, 75, 100, chunkSize);
    this.q050 = new PoolChunkList<T>(this, this.q075, 50, 100, chunkSize);
    this.q025 = new PoolChunkList<T>(this, this.q050, 25, 75, chunkSize);
    this.q000 = new PoolChunkList<T>(this, this.q025, 1, 50, chunkSize);
    this.qInit = new PoolChunkList<T>(this, this.q000, -2147483648, 25, chunkSize);
    this.q100.prevList(this.q075);
    this.q075.prevList(this.q050);
    this.q050.prevList(this.q025);
    this.q025.prevList(this.q000);
    this.q000.prevList(null);
    this.qInit.prevList(this.qInit);
    List<PoolChunkListMetric> metrics = new ArrayList<PoolChunkListMetric>(6);
    metrics.add(this.qInit);
    metrics.add(this.q000);
    metrics.add(this.q025);
    metrics.add(this.q050);
    metrics.add(this.q075);
    metrics.add(this.q100);
    this.chunkListMetrics = Collections.unmodifiableList(metrics);
  }
  
  private PoolSubpage<T> newSubpagePoolHead(int pageSize) {
    PoolSubpage<T> head = new PoolSubpage<T>(pageSize);
    head.prev = head;
    head.next = head;
    return head;
  }
  
  private PoolSubpage<T>[] newSubpagePoolArray(int size) {
    return (PoolSubpage<T>[])new PoolSubpage[size];
  }
  
  abstract boolean isDirect();
  
  PooledByteBuf<T> allocate(PoolThreadCache cache, int reqCapacity, int maxCapacity) {
    PooledByteBuf<T> buf = newByteBuf(maxCapacity);
    allocate(cache, buf, reqCapacity);
    return buf;
  }
  
  static int tinyIdx(int normCapacity) {
    return normCapacity >>> 4;
  }
  
  static int smallIdx(int normCapacity) {
    int tableIdx = 0;
    int i = normCapacity >>> 10;
    while (i != 0) {
      i >>>= 1;
      tableIdx++;
    } 
    return tableIdx;
  }
  
  boolean isTinyOrSmall(int normCapacity) {
    return ((normCapacity & this.subpageOverflowMask) == 0);
  }
  
  static boolean isTiny(int normCapacity) {
    return ((normCapacity & 0xFFFFFE00) == 0);
  }
  
  private void allocate(PoolThreadCache cache, PooledByteBuf<T> buf, int reqCapacity) {
    int normCapacity = normalizeCapacity(reqCapacity);
    if (isTinyOrSmall(normCapacity)) {
      int tableIdx;
      PoolSubpage<T>[] table;
      boolean tiny = isTiny(normCapacity);
      if (tiny) {
        if (cache.allocateTiny(this, buf, reqCapacity, normCapacity))
          return; 
        tableIdx = tinyIdx(normCapacity);
        table = this.tinySubpagePools;
      } else {
        if (cache.allocateSmall(this, buf, reqCapacity, normCapacity))
          return; 
        tableIdx = smallIdx(normCapacity);
        table = this.smallSubpagePools;
      } 
      PoolSubpage<T> head = table[tableIdx];
      synchronized (head) {
        PoolSubpage<T> s = head.next;
        if (s != head) {
          assert s.doNotDestroy && s.elemSize == normCapacity;
          long handle = s.allocate();
          assert handle >= 0L;
          s.chunk.initBufWithSubpage(buf, handle, reqCapacity);
          incTinySmallAllocation(tiny);
          return;
        } 
      } 
      synchronized (this) {
        allocateNormal(buf, reqCapacity, normCapacity);
      } 
      incTinySmallAllocation(tiny);
      return;
    } 
    if (normCapacity <= this.chunkSize) {
      if (cache.allocateNormal(this, buf, reqCapacity, normCapacity))
        return; 
      synchronized (this) {
        allocateNormal(buf, reqCapacity, normCapacity);
        this.allocationsNormal++;
      } 
    } else {
      allocateHuge(buf, reqCapacity);
    } 
  }
  
  private void allocateNormal(PooledByteBuf<T> buf, int reqCapacity, int normCapacity) {
    if (this.q050.allocate(buf, reqCapacity, normCapacity) || this.q025.allocate(buf, reqCapacity, normCapacity) || this.q000
      .allocate(buf, reqCapacity, normCapacity) || this.qInit.allocate(buf, reqCapacity, normCapacity) || this.q075
      .allocate(buf, reqCapacity, normCapacity))
      return; 
    PoolChunk<T> c = newChunk(this.pageSize, this.maxOrder, this.pageShifts, this.chunkSize);
    long handle = c.allocate(normCapacity);
    assert handle > 0L;
    c.initBuf(buf, handle, reqCapacity);
    this.qInit.add(c);
  }
  
  private void incTinySmallAllocation(boolean tiny) {
    if (tiny) {
      this.allocationsTiny.increment();
    } else {
      this.allocationsSmall.increment();
    } 
  }
  
  private void allocateHuge(PooledByteBuf<T> buf, int reqCapacity) {
    PoolChunk<T> chunk = newUnpooledChunk(reqCapacity);
    this.activeBytesHuge.add(chunk.chunkSize());
    buf.initUnpooled(chunk, reqCapacity);
    this.allocationsHuge.increment();
  }
  
  void free(PoolChunk<T> chunk, long handle, int normCapacity, PoolThreadCache cache) {
    if (chunk.unpooled) {
      int size = chunk.chunkSize();
      destroyChunk(chunk);
      this.activeBytesHuge.add(-size);
      this.deallocationsHuge.increment();
    } else {
      SizeClass sizeClass = sizeClass(normCapacity);
      if (cache != null && cache.add(this, chunk, handle, normCapacity, sizeClass))
        return; 
      freeChunk(chunk, handle, sizeClass);
    } 
  }
  
  private SizeClass sizeClass(int normCapacity) {
    if (!isTinyOrSmall(normCapacity))
      return SizeClass.Normal; 
    return isTiny(normCapacity) ? SizeClass.Tiny : SizeClass.Small;
  }
  
  void freeChunk(PoolChunk<T> chunk, long handle, SizeClass sizeClass) {
    boolean destroyChunk;
    synchronized (this) {
      switch (sizeClass) {
        case Normal:
          this.deallocationsNormal++;
          break;
        case Small:
          this.deallocationsSmall++;
          break;
        case Tiny:
          this.deallocationsTiny++;
          break;
        default:
          throw new Error();
      } 
      destroyChunk = !chunk.parent.free(chunk, handle);
    } 
    if (destroyChunk)
      destroyChunk(chunk); 
  }
  
  PoolSubpage<T> findSubpagePoolHead(int elemSize) {
    int tableIdx;
    PoolSubpage<T>[] table;
    if (isTiny(elemSize)) {
      tableIdx = elemSize >>> 4;
      table = this.tinySubpagePools;
    } else {
      tableIdx = 0;
      elemSize >>>= 10;
      while (elemSize != 0) {
        elemSize >>>= 1;
        tableIdx++;
      } 
      table = this.smallSubpagePools;
    } 
    return table[tableIdx];
  }
  
  int normalizeCapacity(int reqCapacity) {
    if (reqCapacity < 0)
      throw new IllegalArgumentException("capacity: " + reqCapacity + " (expected: 0+)"); 
    if (reqCapacity >= this.chunkSize)
      return (this.directMemoryCacheAlignment == 0) ? reqCapacity : alignCapacity(reqCapacity); 
    if (!isTiny(reqCapacity)) {
      int normalizedCapacity = reqCapacity;
      normalizedCapacity--;
      normalizedCapacity |= normalizedCapacity >>> 1;
      normalizedCapacity |= normalizedCapacity >>> 2;
      normalizedCapacity |= normalizedCapacity >>> 4;
      normalizedCapacity |= normalizedCapacity >>> 8;
      normalizedCapacity |= normalizedCapacity >>> 16;
      normalizedCapacity++;
      if (normalizedCapacity < 0)
        normalizedCapacity >>>= 1; 
      assert this.directMemoryCacheAlignment == 0 || (normalizedCapacity & this.directMemoryCacheAlignmentMask) == 0;
      return normalizedCapacity;
    } 
    if (this.directMemoryCacheAlignment > 0)
      return alignCapacity(reqCapacity); 
    if ((reqCapacity & 0xF) == 0)
      return reqCapacity; 
    return (reqCapacity & 0xFFFFFFF0) + 16;
  }
  
  int alignCapacity(int reqCapacity) {
    int delta = reqCapacity & this.directMemoryCacheAlignmentMask;
    return (delta == 0) ? reqCapacity : (reqCapacity + this.directMemoryCacheAlignment - delta);
  }
  
  void reallocate(PooledByteBuf<T> buf, int newCapacity, boolean freeOldMemory) {
    if (newCapacity < 0 || newCapacity > buf.maxCapacity())
      throw new IllegalArgumentException("newCapacity: " + newCapacity); 
    int oldCapacity = buf.length;
    if (oldCapacity == newCapacity)
      return; 
    PoolChunk<T> oldChunk = buf.chunk;
    long oldHandle = buf.handle;
    T oldMemory = buf.memory;
    int oldOffset = buf.offset;
    int oldMaxLength = buf.maxLength;
    int readerIndex = buf.readerIndex();
    int writerIndex = buf.writerIndex();
    allocate(this.parent.threadCache(), buf, newCapacity);
    if (newCapacity > oldCapacity) {
      memoryCopy(oldMemory, oldOffset, buf.memory, buf.offset, oldCapacity);
    } else if (newCapacity < oldCapacity) {
      if (readerIndex < newCapacity) {
        if (writerIndex > newCapacity)
          writerIndex = newCapacity; 
        memoryCopy(oldMemory, oldOffset + readerIndex, buf.memory, buf.offset + readerIndex, writerIndex - readerIndex);
      } else {
        readerIndex = writerIndex = newCapacity;
      } 
    } 
    buf.setIndex(readerIndex, writerIndex);
    if (freeOldMemory)
      free(oldChunk, oldHandle, oldMaxLength, buf.cache); 
  }
  
  public int numThreadCaches() {
    return this.numThreadCaches.get();
  }
  
  public int numTinySubpages() {
    return this.tinySubpagePools.length;
  }
  
  public int numSmallSubpages() {
    return this.smallSubpagePools.length;
  }
  
  public int numChunkLists() {
    return this.chunkListMetrics.size();
  }
  
  public List<PoolSubpageMetric> tinySubpages() {
    return subPageMetricList((PoolSubpage<?>[])this.tinySubpagePools);
  }
  
  public List<PoolSubpageMetric> smallSubpages() {
    return subPageMetricList((PoolSubpage<?>[])this.smallSubpagePools);
  }
  
  public List<PoolChunkListMetric> chunkLists() {
    return this.chunkListMetrics;
  }
  
  private static List<PoolSubpageMetric> subPageMetricList(PoolSubpage<?>[] pages) {
    List<PoolSubpageMetric> metrics = new ArrayList<PoolSubpageMetric>();
    for (PoolSubpage<?> head : pages) {
      if (head.next != head) {
        PoolSubpage<?> s = head.next;
        do {
          metrics.add(s);
          s = s.next;
        } while (s != head);
      } 
    } 
    return metrics;
  }
  
  public long numAllocations() {
    long allocsNormal;
    synchronized (this) {
      allocsNormal = this.allocationsNormal;
    } 
    return this.allocationsTiny.value() + this.allocationsSmall.value() + allocsNormal + this.allocationsHuge.value();
  }
  
  public long numTinyAllocations() {
    return this.allocationsTiny.value();
  }
  
  public long numSmallAllocations() {
    return this.allocationsSmall.value();
  }
  
  public synchronized long numNormalAllocations() {
    return this.allocationsNormal;
  }
  
  public long numDeallocations() {
    long deallocs;
    synchronized (this) {
      deallocs = this.deallocationsTiny + this.deallocationsSmall + this.deallocationsNormal;
    } 
    return deallocs + this.deallocationsHuge.value();
  }
  
  public synchronized long numTinyDeallocations() {
    return this.deallocationsTiny;
  }
  
  public synchronized long numSmallDeallocations() {
    return this.deallocationsSmall;
  }
  
  public synchronized long numNormalDeallocations() {
    return this.deallocationsNormal;
  }
  
  public long numHugeAllocations() {
    return this.allocationsHuge.value();
  }
  
  public long numHugeDeallocations() {
    return this.deallocationsHuge.value();
  }
  
  public long numActiveAllocations() {
    long val = this.allocationsTiny.value() + this.allocationsSmall.value() + this.allocationsHuge.value() - this.deallocationsHuge.value();
    synchronized (this) {
      val += this.allocationsNormal - this.deallocationsTiny + this.deallocationsSmall + this.deallocationsNormal;
    } 
    return Math.max(val, 0L);
  }
  
  public long numActiveTinyAllocations() {
    return Math.max(numTinyAllocations() - numTinyDeallocations(), 0L);
  }
  
  public long numActiveSmallAllocations() {
    return Math.max(numSmallAllocations() - numSmallDeallocations(), 0L);
  }
  
  public long numActiveNormalAllocations() {
    long val;
    synchronized (this) {
      val = this.allocationsNormal - this.deallocationsNormal;
    } 
    return Math.max(val, 0L);
  }
  
  public long numActiveHugeAllocations() {
    return Math.max(numHugeAllocations() - numHugeDeallocations(), 0L);
  }
  
  public long numActiveBytes() {
    long val = this.activeBytesHuge.value();
    synchronized (this) {
      for (int i = 0; i < this.chunkListMetrics.size(); i++) {
        for (PoolChunkMetric m : this.chunkListMetrics.get(i))
          val += m.chunkSize(); 
      } 
    } 
    return Math.max(0L, val);
  }
  
  protected abstract PoolChunk<T> newChunk(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
  
  protected abstract PoolChunk<T> newUnpooledChunk(int paramInt);
  
  protected abstract PooledByteBuf<T> newByteBuf(int paramInt);
  
  protected abstract void memoryCopy(T paramT1, int paramInt1, T paramT2, int paramInt2, int paramInt3);
  
  protected abstract void destroyChunk(PoolChunk<T> paramPoolChunk);
  
  public synchronized String toString() {
    StringBuilder buf = (new StringBuilder()).append("Chunk(s) at 0~25%:").append(StringUtil.NEWLINE).append(this.qInit).append(StringUtil.NEWLINE).append("Chunk(s) at 0~50%:").append(StringUtil.NEWLINE).append(this.q000).append(StringUtil.NEWLINE).append("Chunk(s) at 25~75%:").append(StringUtil.NEWLINE).append(this.q025).append(StringUtil.NEWLINE).append("Chunk(s) at 50~100%:").append(StringUtil.NEWLINE).append(this.q050).append(StringUtil.NEWLINE).append("Chunk(s) at 75~100%:").append(StringUtil.NEWLINE).append(this.q075).append(StringUtil.NEWLINE).append("Chunk(s) at 100%:").append(StringUtil.NEWLINE).append(this.q100).append(StringUtil.NEWLINE).append("tiny subpages:");
    appendPoolSubPages(buf, (PoolSubpage<?>[])this.tinySubpagePools);
    buf.append(StringUtil.NEWLINE)
      .append("small subpages:");
    appendPoolSubPages(buf, (PoolSubpage<?>[])this.smallSubpagePools);
    buf.append(StringUtil.NEWLINE);
    return buf.toString();
  }
  
  private static void appendPoolSubPages(StringBuilder buf, PoolSubpage<?>[] subpages) {
    for (int i = 0; i < subpages.length; i++) {
      PoolSubpage<?> head = subpages[i];
      if (head.next != head) {
        buf.append(StringUtil.NEWLINE)
          .append(i)
          .append(": ");
        PoolSubpage<?> s = head.next;
        do {
          buf.append(s);
          s = s.next;
        } while (s != head);
      } 
    } 
  }
  
  protected final void finalize() throws Throwable {
    try {
      super.finalize();
    } finally {
      destroyPoolSubPages((PoolSubpage<?>[])this.smallSubpagePools);
      destroyPoolSubPages((PoolSubpage<?>[])this.tinySubpagePools);
      destroyPoolChunkLists((PoolChunkList<T>[])new PoolChunkList[] { this.qInit, this.q000, this.q025, this.q050, this.q075, this.q100 });
    } 
  }
  
  private static void destroyPoolSubPages(PoolSubpage<?>[] pages) {
    for (PoolSubpage<?> page : pages)
      page.destroy(); 
  }
  
  private void destroyPoolChunkLists(PoolChunkList<T>... chunkLists) {
    for (PoolChunkList<T> chunkList : chunkLists)
      chunkList.destroy(this); 
  }
  
  static final class HeapArena extends PoolArena<byte[]> {
    HeapArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize, int directMemoryCacheAlignment) {
      super(parent, pageSize, maxOrder, pageShifts, chunkSize, directMemoryCacheAlignment);
    }
    
    private static byte[] newByteArray(int size) {
      return PlatformDependent.allocateUninitializedArray(size);
    }
    
    boolean isDirect() {
      return false;
    }
    
    protected PoolChunk<byte[]> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize) {
      return (PoolChunk)new PoolChunk<byte>(this, newByteArray(chunkSize), pageSize, maxOrder, pageShifts, chunkSize, 0);
    }
    
    protected PoolChunk<byte[]> newUnpooledChunk(int capacity) {
      return (PoolChunk)new PoolChunk<byte>(this, newByteArray(capacity), capacity, 0);
    }
    
    protected void destroyChunk(PoolChunk<byte[]> chunk) {}
    
    protected PooledByteBuf<byte[]> newByteBuf(int maxCapacity) {
      return HAS_UNSAFE ? PooledUnsafeHeapByteBuf.newUnsafeInstance(maxCapacity) : 
        PooledHeapByteBuf.newInstance(maxCapacity);
    }
    
    protected void memoryCopy(byte[] src, int srcOffset, byte[] dst, int dstOffset, int length) {
      if (length == 0)
        return; 
      System.arraycopy(src, srcOffset, dst, dstOffset, length);
    }
  }
  
  static final class DirectArena extends PoolArena<ByteBuffer> {
    DirectArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize, int directMemoryCacheAlignment) {
      super(parent, pageSize, maxOrder, pageShifts, chunkSize, directMemoryCacheAlignment);
    }
    
    boolean isDirect() {
      return true;
    }
    
    private int offsetCacheLine(ByteBuffer memory) {
      return HAS_UNSAFE ? 
        (int)(PlatformDependent.directBufferAddress(memory) & this.directMemoryCacheAlignmentMask) : 0;
    }
    
    protected PoolChunk<ByteBuffer> newChunk(int pageSize, int maxOrder, int pageShifts, int chunkSize) {
      if (this.directMemoryCacheAlignment == 0)
        return new PoolChunk<ByteBuffer>(this, 
            allocateDirect(chunkSize), pageSize, maxOrder, pageShifts, chunkSize, 0); 
      ByteBuffer memory = allocateDirect(chunkSize + this.directMemoryCacheAlignment);
      return new PoolChunk<ByteBuffer>(this, memory, pageSize, maxOrder, pageShifts, chunkSize, 
          
          offsetCacheLine(memory));
    }
    
    protected PoolChunk<ByteBuffer> newUnpooledChunk(int capacity) {
      if (this.directMemoryCacheAlignment == 0)
        return new PoolChunk<ByteBuffer>(this, 
            allocateDirect(capacity), capacity, 0); 
      ByteBuffer memory = allocateDirect(capacity + this.directMemoryCacheAlignment);
      return new PoolChunk<ByteBuffer>(this, memory, capacity, 
          offsetCacheLine(memory));
    }
    
    private static ByteBuffer allocateDirect(int capacity) {
      return PlatformDependent.useDirectBufferNoCleaner() ? 
        PlatformDependent.allocateDirectNoCleaner(capacity) : ByteBuffer.allocateDirect(capacity);
    }
    
    protected void destroyChunk(PoolChunk<ByteBuffer> chunk) {
      if (PlatformDependent.useDirectBufferNoCleaner()) {
        PlatformDependent.freeDirectNoCleaner((ByteBuffer)chunk.memory);
      } else {
        PlatformDependent.freeDirectBuffer((ByteBuffer)chunk.memory);
      } 
    }
    
    protected PooledByteBuf<ByteBuffer> newByteBuf(int maxCapacity) {
      if (HAS_UNSAFE)
        return PooledUnsafeDirectByteBuf.newInstance(maxCapacity); 
      return PooledDirectByteBuf.newInstance(maxCapacity);
    }
    
    protected void memoryCopy(ByteBuffer src, int srcOffset, ByteBuffer dst, int dstOffset, int length) {
      if (length == 0)
        return; 
      if (HAS_UNSAFE) {
        PlatformDependent.copyMemory(
            PlatformDependent.directBufferAddress(src) + srcOffset, 
            PlatformDependent.directBufferAddress(dst) + dstOffset, length);
      } else {
        src = src.duplicate();
        dst = dst.duplicate();
        src.position(srcOffset).limit(srcOffset + length);
        dst.position(dstOffset);
        dst.put(src);
      } 
    }
  }
}

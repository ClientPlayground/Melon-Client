package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.Recycler;
import com.github.steveice10.netty.util.internal.MathUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.ByteBuffer;
import java.util.Queue;

final class PoolThreadCache {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(PoolThreadCache.class);
  
  final PoolArena<byte[]> heapArena;
  
  final PoolArena<ByteBuffer> directArena;
  
  private final MemoryRegionCache<byte[]>[] tinySubPageHeapCaches;
  
  private final MemoryRegionCache<byte[]>[] smallSubPageHeapCaches;
  
  private final MemoryRegionCache<ByteBuffer>[] tinySubPageDirectCaches;
  
  private final MemoryRegionCache<ByteBuffer>[] smallSubPageDirectCaches;
  
  private final MemoryRegionCache<byte[]>[] normalHeapCaches;
  
  private final MemoryRegionCache<ByteBuffer>[] normalDirectCaches;
  
  private final int numShiftsNormalDirect;
  
  private final int numShiftsNormalHeap;
  
  private final int freeSweepAllocationThreshold;
  
  private int allocations;
  
  PoolThreadCache(PoolArena<byte[]> heapArena, PoolArena<ByteBuffer> directArena, int tinyCacheSize, int smallCacheSize, int normalCacheSize, int maxCachedBufferCapacity, int freeSweepAllocationThreshold) {
    if (maxCachedBufferCapacity < 0)
      throw new IllegalArgumentException("maxCachedBufferCapacity: " + maxCachedBufferCapacity + " (expected: >= 0)"); 
    this.freeSweepAllocationThreshold = freeSweepAllocationThreshold;
    this.heapArena = heapArena;
    this.directArena = directArena;
    if (directArena != null) {
      this.tinySubPageDirectCaches = createSubPageCaches(tinyCacheSize, 32, PoolArena.SizeClass.Tiny);
      this.smallSubPageDirectCaches = createSubPageCaches(smallCacheSize, directArena.numSmallSubpagePools, PoolArena.SizeClass.Small);
      this.numShiftsNormalDirect = log2(directArena.pageSize);
      this.normalDirectCaches = createNormalCaches(normalCacheSize, maxCachedBufferCapacity, directArena);
      directArena.numThreadCaches.getAndIncrement();
    } else {
      this.tinySubPageDirectCaches = null;
      this.smallSubPageDirectCaches = null;
      this.normalDirectCaches = null;
      this.numShiftsNormalDirect = -1;
    } 
    if (heapArena != null) {
      this.tinySubPageHeapCaches = createSubPageCaches(tinyCacheSize, 32, PoolArena.SizeClass.Tiny);
      this.smallSubPageHeapCaches = createSubPageCaches(smallCacheSize, heapArena.numSmallSubpagePools, PoolArena.SizeClass.Small);
      this.numShiftsNormalHeap = log2(heapArena.pageSize);
      this.normalHeapCaches = createNormalCaches(normalCacheSize, maxCachedBufferCapacity, (PoolArena)heapArena);
      heapArena.numThreadCaches.getAndIncrement();
    } else {
      this.tinySubPageHeapCaches = null;
      this.smallSubPageHeapCaches = null;
      this.normalHeapCaches = null;
      this.numShiftsNormalHeap = -1;
    } 
    if ((this.tinySubPageDirectCaches != null || this.smallSubPageDirectCaches != null || this.normalDirectCaches != null || this.tinySubPageHeapCaches != null || this.smallSubPageHeapCaches != null || this.normalHeapCaches != null) && freeSweepAllocationThreshold < 1)
      throw new IllegalArgumentException("freeSweepAllocationThreshold: " + freeSweepAllocationThreshold + " (expected: > 0)"); 
  }
  
  private static <T> MemoryRegionCache<T>[] createSubPageCaches(int cacheSize, int numCaches, PoolArena.SizeClass sizeClass) {
    if (cacheSize > 0 && numCaches > 0) {
      MemoryRegionCache[] arrayOfMemoryRegionCache = new MemoryRegionCache[numCaches];
      for (int i = 0; i < arrayOfMemoryRegionCache.length; i++)
        arrayOfMemoryRegionCache[i] = new SubPageMemoryRegionCache(cacheSize, sizeClass); 
      return (MemoryRegionCache<T>[])arrayOfMemoryRegionCache;
    } 
    return null;
  }
  
  private static <T> MemoryRegionCache<T>[] createNormalCaches(int cacheSize, int maxCachedBufferCapacity, PoolArena<T> area) {
    if (cacheSize > 0 && maxCachedBufferCapacity > 0) {
      int max = Math.min(area.chunkSize, maxCachedBufferCapacity);
      int arraySize = Math.max(1, log2(max / area.pageSize) + 1);
      MemoryRegionCache[] arrayOfMemoryRegionCache = new MemoryRegionCache[arraySize];
      for (int i = 0; i < arrayOfMemoryRegionCache.length; i++)
        arrayOfMemoryRegionCache[i] = new NormalMemoryRegionCache(cacheSize); 
      return (MemoryRegionCache<T>[])arrayOfMemoryRegionCache;
    } 
    return null;
  }
  
  private static int log2(int val) {
    int res = 0;
    while (val > 1) {
      val >>= 1;
      res++;
    } 
    return res;
  }
  
  boolean allocateTiny(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int normCapacity) {
    return allocate(cacheForTiny(area, normCapacity), buf, reqCapacity);
  }
  
  boolean allocateSmall(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int normCapacity) {
    return allocate(cacheForSmall(area, normCapacity), buf, reqCapacity);
  }
  
  boolean allocateNormal(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int normCapacity) {
    return allocate(cacheForNormal(area, normCapacity), buf, reqCapacity);
  }
  
  private boolean allocate(MemoryRegionCache<?> cache, PooledByteBuf<?> buf, int reqCapacity) {
    if (cache == null)
      return false; 
    boolean allocated = cache.allocate(buf, reqCapacity);
    if (++this.allocations >= this.freeSweepAllocationThreshold) {
      this.allocations = 0;
      trim();
    } 
    return allocated;
  }
  
  boolean add(PoolArena<?> area, PoolChunk<?> chunk, long handle, int normCapacity, PoolArena.SizeClass sizeClass) {
    MemoryRegionCache<?> cache = cache(area, normCapacity, sizeClass);
    if (cache == null)
      return false; 
    return cache.add(chunk, handle);
  }
  
  private MemoryRegionCache<?> cache(PoolArena<?> area, int normCapacity, PoolArena.SizeClass sizeClass) {
    switch (sizeClass) {
      case Normal:
        return cacheForNormal(area, normCapacity);
      case Small:
        return cacheForSmall(area, normCapacity);
      case Tiny:
        return cacheForTiny(area, normCapacity);
    } 
    throw new Error();
  }
  
  void free() {
    int numFreed = free((MemoryRegionCache<?>[])this.tinySubPageDirectCaches) + free((MemoryRegionCache<?>[])this.smallSubPageDirectCaches) + free((MemoryRegionCache<?>[])this.normalDirectCaches) + free((MemoryRegionCache<?>[])this.tinySubPageHeapCaches) + free((MemoryRegionCache<?>[])this.smallSubPageHeapCaches) + free((MemoryRegionCache<?>[])this.normalHeapCaches);
    if (numFreed > 0 && logger.isDebugEnabled())
      logger.debug("Freed {} thread-local buffer(s) from thread: {}", Integer.valueOf(numFreed), Thread.currentThread().getName()); 
    if (this.directArena != null)
      this.directArena.numThreadCaches.getAndDecrement(); 
    if (this.heapArena != null)
      this.heapArena.numThreadCaches.getAndDecrement(); 
  }
  
  private static int free(MemoryRegionCache<?>[] caches) {
    if (caches == null)
      return 0; 
    int numFreed = 0;
    for (MemoryRegionCache<?> c : caches)
      numFreed += free(c); 
    return numFreed;
  }
  
  private static int free(MemoryRegionCache<?> cache) {
    if (cache == null)
      return 0; 
    return cache.free();
  }
  
  void trim() {
    trim((MemoryRegionCache<?>[])this.tinySubPageDirectCaches);
    trim((MemoryRegionCache<?>[])this.smallSubPageDirectCaches);
    trim((MemoryRegionCache<?>[])this.normalDirectCaches);
    trim((MemoryRegionCache<?>[])this.tinySubPageHeapCaches);
    trim((MemoryRegionCache<?>[])this.smallSubPageHeapCaches);
    trim((MemoryRegionCache<?>[])this.normalHeapCaches);
  }
  
  private static void trim(MemoryRegionCache<?>[] caches) {
    if (caches == null)
      return; 
    for (MemoryRegionCache<?> c : caches)
      trim(c); 
  }
  
  private static void trim(MemoryRegionCache<?> cache) {
    if (cache == null)
      return; 
    cache.trim();
  }
  
  private MemoryRegionCache<?> cacheForTiny(PoolArena<?> area, int normCapacity) {
    int idx = PoolArena.tinyIdx(normCapacity);
    if (area.isDirect())
      return cache((MemoryRegionCache<?>[])this.tinySubPageDirectCaches, idx); 
    return cache((MemoryRegionCache<?>[])this.tinySubPageHeapCaches, idx);
  }
  
  private MemoryRegionCache<?> cacheForSmall(PoolArena<?> area, int normCapacity) {
    int idx = PoolArena.smallIdx(normCapacity);
    if (area.isDirect())
      return cache((MemoryRegionCache<?>[])this.smallSubPageDirectCaches, idx); 
    return cache((MemoryRegionCache<?>[])this.smallSubPageHeapCaches, idx);
  }
  
  private MemoryRegionCache<?> cacheForNormal(PoolArena<?> area, int normCapacity) {
    if (area.isDirect()) {
      int i = log2(normCapacity >> this.numShiftsNormalDirect);
      return cache((MemoryRegionCache<?>[])this.normalDirectCaches, i);
    } 
    int idx = log2(normCapacity >> this.numShiftsNormalHeap);
    return cache((MemoryRegionCache<?>[])this.normalHeapCaches, idx);
  }
  
  private static <T> MemoryRegionCache<T> cache(MemoryRegionCache<T>[] cache, int idx) {
    if (cache == null || idx > cache.length - 1)
      return null; 
    return cache[idx];
  }
  
  private static final class SubPageMemoryRegionCache<T> extends MemoryRegionCache<T> {
    SubPageMemoryRegionCache(int size, PoolArena.SizeClass sizeClass) {
      super(size, sizeClass);
    }
    
    protected void initBuf(PoolChunk<T> chunk, long handle, PooledByteBuf<T> buf, int reqCapacity) {
      chunk.initBufWithSubpage(buf, handle, reqCapacity);
    }
  }
  
  private static final class NormalMemoryRegionCache<T> extends MemoryRegionCache<T> {
    NormalMemoryRegionCache(int size) {
      super(size, PoolArena.SizeClass.Normal);
    }
    
    protected void initBuf(PoolChunk<T> chunk, long handle, PooledByteBuf<T> buf, int reqCapacity) {
      chunk.initBuf(buf, handle, reqCapacity);
    }
  }
  
  private static abstract class MemoryRegionCache<T> {
    private final int size;
    
    private final Queue<Entry<T>> queue;
    
    private final PoolArena.SizeClass sizeClass;
    
    private int allocations;
    
    MemoryRegionCache(int size, PoolArena.SizeClass sizeClass) {
      this.size = MathUtil.safeFindNextPositivePowerOfTwo(size);
      this.queue = PlatformDependent.newFixedMpscQueue(this.size);
      this.sizeClass = sizeClass;
    }
    
    protected abstract void initBuf(PoolChunk<T> param1PoolChunk, long param1Long, PooledByteBuf<T> param1PooledByteBuf, int param1Int);
    
    public final boolean add(PoolChunk<T> chunk, long handle) {
      Entry<T> entry = newEntry(chunk, handle);
      boolean queued = this.queue.offer(entry);
      if (!queued)
        entry.recycle(); 
      return queued;
    }
    
    public final boolean allocate(PooledByteBuf<T> buf, int reqCapacity) {
      Entry<T> entry = this.queue.poll();
      if (entry == null)
        return false; 
      initBuf(entry.chunk, entry.handle, buf, reqCapacity);
      entry.recycle();
      this.allocations++;
      return true;
    }
    
    public final int free() {
      return free(2147483647);
    }
    
    private int free(int max) {
      int numFreed = 0;
      for (; numFreed < max; numFreed++) {
        Entry<T> entry = this.queue.poll();
        if (entry != null) {
          freeEntry(entry);
        } else {
          return numFreed;
        } 
      } 
      return numFreed;
    }
    
    public final void trim() {
      int free = this.size - this.allocations;
      this.allocations = 0;
      if (free > 0)
        free(free); 
    }
    
    private void freeEntry(Entry entry) {
      PoolChunk chunk = entry.chunk;
      long handle = entry.handle;
      entry.recycle();
      chunk.arena.freeChunk(chunk, handle, this.sizeClass);
    }
    
    static final class Entry<T> {
      final Recycler.Handle<Entry<?>> recyclerHandle;
      
      PoolChunk<T> chunk;
      
      long handle = -1L;
      
      Entry(Recycler.Handle<Entry<?>> recyclerHandle) {
        this.recyclerHandle = recyclerHandle;
      }
      
      void recycle() {
        this.chunk = null;
        this.handle = -1L;
        this.recyclerHandle.recycle(this);
      }
    }
    
    private static Entry newEntry(PoolChunk<?> chunk, long handle) {
      Entry entry = (Entry)RECYCLER.get();
      entry.chunk = chunk;
      entry.handle = handle;
      return entry;
    }
    
    private static final Recycler<Entry> RECYCLER = new Recycler<Entry>() {
        protected PoolThreadCache.MemoryRegionCache.Entry newObject(Recycler.Handle<PoolThreadCache.MemoryRegionCache.Entry> handle) {
          return new PoolThreadCache.MemoryRegionCache.Entry((Recycler.Handle)handle);
        }
      };
  }
}

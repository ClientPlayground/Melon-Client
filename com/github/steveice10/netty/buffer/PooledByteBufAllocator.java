package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.NettyRuntime;
import com.github.steveice10.netty.util.concurrent.FastThreadLocal;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PooledByteBufAllocator extends AbstractByteBufAllocator implements ByteBufAllocatorMetricProvider {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(PooledByteBufAllocator.class);
  
  private static final int DEFAULT_NUM_HEAP_ARENA;
  
  private static final int DEFAULT_NUM_DIRECT_ARENA;
  
  private static final int DEFAULT_PAGE_SIZE;
  
  private static final int DEFAULT_MAX_ORDER;
  
  private static final int DEFAULT_TINY_CACHE_SIZE;
  
  private static final int DEFAULT_SMALL_CACHE_SIZE;
  
  private static final int DEFAULT_NORMAL_CACHE_SIZE;
  
  private static final int DEFAULT_MAX_CACHED_BUFFER_CAPACITY;
  
  private static final int DEFAULT_CACHE_TRIM_INTERVAL;
  
  private static final boolean DEFAULT_USE_CACHE_FOR_ALL_THREADS;
  
  private static final int DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT;
  
  private static final int MIN_PAGE_SIZE = 4096;
  
  private static final int MAX_CHUNK_SIZE = 1073741824;
  
  public static final PooledByteBufAllocator DEFAULT;
  
  private final PoolArena<byte[]>[] heapArenas;
  
  private final PoolArena<ByteBuffer>[] directArenas;
  
  private final int tinyCacheSize;
  
  private final int smallCacheSize;
  
  private final int normalCacheSize;
  
  private final List<PoolArenaMetric> heapArenaMetrics;
  
  private final List<PoolArenaMetric> directArenaMetrics;
  
  private final PoolThreadLocalCache threadCache;
  
  private final int chunkSize;
  
  private final PooledByteBufAllocatorMetric metric;
  
  static {
    int defaultPageSize = SystemPropertyUtil.getInt("com.github.steveice10.netty.allocator.pageSize", 8192);
    Throwable pageSizeFallbackCause = null;
    try {
      validateAndCalculatePageShifts(defaultPageSize);
    } catch (Throwable t) {
      pageSizeFallbackCause = t;
      defaultPageSize = 8192;
    } 
    DEFAULT_PAGE_SIZE = defaultPageSize;
    int defaultMaxOrder = SystemPropertyUtil.getInt("com.github.steveice10.netty.allocator.maxOrder", 11);
    Throwable maxOrderFallbackCause = null;
    try {
      validateAndCalculateChunkSize(DEFAULT_PAGE_SIZE, defaultMaxOrder);
    } catch (Throwable t) {
      maxOrderFallbackCause = t;
      defaultMaxOrder = 11;
    } 
    DEFAULT_MAX_ORDER = defaultMaxOrder;
    Runtime runtime = Runtime.getRuntime();
    int defaultMinNumArena = NettyRuntime.availableProcessors() * 2;
    int defaultChunkSize = DEFAULT_PAGE_SIZE << DEFAULT_MAX_ORDER;
    DEFAULT_NUM_HEAP_ARENA = Math.max(0, 
        SystemPropertyUtil.getInt("com.github.steveice10.netty.allocator.numHeapArenas", 
          
          (int)Math.min(defaultMinNumArena, runtime
            
            .maxMemory() / defaultChunkSize / 2L / 3L)));
    DEFAULT_NUM_DIRECT_ARENA = Math.max(0, 
        SystemPropertyUtil.getInt("com.github.steveice10.netty.allocator.numDirectArenas", 
          
          (int)Math.min(defaultMinNumArena, 
            
            PlatformDependent.maxDirectMemory() / defaultChunkSize / 2L / 3L)));
    DEFAULT_TINY_CACHE_SIZE = SystemPropertyUtil.getInt("com.github.steveice10.netty.allocator.tinyCacheSize", 512);
    DEFAULT_SMALL_CACHE_SIZE = SystemPropertyUtil.getInt("com.github.steveice10.netty.allocator.smallCacheSize", 256);
    DEFAULT_NORMAL_CACHE_SIZE = SystemPropertyUtil.getInt("com.github.steveice10.netty.allocator.normalCacheSize", 64);
    DEFAULT_MAX_CACHED_BUFFER_CAPACITY = SystemPropertyUtil.getInt("com.github.steveice10.netty.allocator.maxCachedBufferCapacity", 32768);
    DEFAULT_CACHE_TRIM_INTERVAL = SystemPropertyUtil.getInt("com.github.steveice10.netty.allocator.cacheTrimInterval", 8192);
    DEFAULT_USE_CACHE_FOR_ALL_THREADS = SystemPropertyUtil.getBoolean("com.github.steveice10.netty.allocator.useCacheForAllThreads", true);
    DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT = SystemPropertyUtil.getInt("com.github.steveice10.netty.allocator.directMemoryCacheAlignment", 0);
    if (logger.isDebugEnabled()) {
      logger.debug("-Dio.netty.allocator.numHeapArenas: {}", Integer.valueOf(DEFAULT_NUM_HEAP_ARENA));
      logger.debug("-Dio.netty.allocator.numDirectArenas: {}", Integer.valueOf(DEFAULT_NUM_DIRECT_ARENA));
      if (pageSizeFallbackCause == null) {
        logger.debug("-Dio.netty.allocator.pageSize: {}", Integer.valueOf(DEFAULT_PAGE_SIZE));
      } else {
        logger.debug("-Dio.netty.allocator.pageSize: {}", Integer.valueOf(DEFAULT_PAGE_SIZE), pageSizeFallbackCause);
      } 
      if (maxOrderFallbackCause == null) {
        logger.debug("-Dio.netty.allocator.maxOrder: {}", Integer.valueOf(DEFAULT_MAX_ORDER));
      } else {
        logger.debug("-Dio.netty.allocator.maxOrder: {}", Integer.valueOf(DEFAULT_MAX_ORDER), maxOrderFallbackCause);
      } 
      logger.debug("-Dio.netty.allocator.chunkSize: {}", Integer.valueOf(DEFAULT_PAGE_SIZE << DEFAULT_MAX_ORDER));
      logger.debug("-Dio.netty.allocator.tinyCacheSize: {}", Integer.valueOf(DEFAULT_TINY_CACHE_SIZE));
      logger.debug("-Dio.netty.allocator.smallCacheSize: {}", Integer.valueOf(DEFAULT_SMALL_CACHE_SIZE));
      logger.debug("-Dio.netty.allocator.normalCacheSize: {}", Integer.valueOf(DEFAULT_NORMAL_CACHE_SIZE));
      logger.debug("-Dio.netty.allocator.maxCachedBufferCapacity: {}", Integer.valueOf(DEFAULT_MAX_CACHED_BUFFER_CAPACITY));
      logger.debug("-Dio.netty.allocator.cacheTrimInterval: {}", Integer.valueOf(DEFAULT_CACHE_TRIM_INTERVAL));
      logger.debug("-Dio.netty.allocator.useCacheForAllThreads: {}", Boolean.valueOf(DEFAULT_USE_CACHE_FOR_ALL_THREADS));
    } 
    DEFAULT = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());
  }
  
  public PooledByteBufAllocator() {
    this(false);
  }
  
  public PooledByteBufAllocator(boolean preferDirect) {
    this(preferDirect, DEFAULT_NUM_HEAP_ARENA, DEFAULT_NUM_DIRECT_ARENA, DEFAULT_PAGE_SIZE, DEFAULT_MAX_ORDER);
  }
  
  public PooledByteBufAllocator(int nHeapArena, int nDirectArena, int pageSize, int maxOrder) {
    this(false, nHeapArena, nDirectArena, pageSize, maxOrder);
  }
  
  @Deprecated
  public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder) {
    this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder, DEFAULT_TINY_CACHE_SIZE, DEFAULT_SMALL_CACHE_SIZE, DEFAULT_NORMAL_CACHE_SIZE);
  }
  
  @Deprecated
  public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder, int tinyCacheSize, int smallCacheSize, int normalCacheSize) {
    this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder, tinyCacheSize, smallCacheSize, normalCacheSize, DEFAULT_USE_CACHE_FOR_ALL_THREADS, DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT);
  }
  
  public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder, int tinyCacheSize, int smallCacheSize, int normalCacheSize, boolean useCacheForAllThreads) {
    this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder, tinyCacheSize, smallCacheSize, normalCacheSize, useCacheForAllThreads, DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT);
  }
  
  public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder, int tinyCacheSize, int smallCacheSize, int normalCacheSize, boolean useCacheForAllThreads, int directMemoryCacheAlignment) {
    super(preferDirect);
    this.threadCache = new PoolThreadLocalCache(useCacheForAllThreads);
    this.tinyCacheSize = tinyCacheSize;
    this.smallCacheSize = smallCacheSize;
    this.normalCacheSize = normalCacheSize;
    this.chunkSize = validateAndCalculateChunkSize(pageSize, maxOrder);
    if (nHeapArena < 0)
      throw new IllegalArgumentException("nHeapArena: " + nHeapArena + " (expected: >= 0)"); 
    if (nDirectArena < 0)
      throw new IllegalArgumentException("nDirectArea: " + nDirectArena + " (expected: >= 0)"); 
    if (directMemoryCacheAlignment < 0)
      throw new IllegalArgumentException("directMemoryCacheAlignment: " + directMemoryCacheAlignment + " (expected: >= 0)"); 
    if (directMemoryCacheAlignment > 0 && !isDirectMemoryCacheAlignmentSupported())
      throw new IllegalArgumentException("directMemoryCacheAlignment is not supported"); 
    if ((directMemoryCacheAlignment & -directMemoryCacheAlignment) != directMemoryCacheAlignment)
      throw new IllegalArgumentException("directMemoryCacheAlignment: " + directMemoryCacheAlignment + " (expected: power of two)"); 
    int pageShifts = validateAndCalculatePageShifts(pageSize);
    if (nHeapArena > 0) {
      this.heapArenas = newArenaArray(nHeapArena);
      List<PoolArenaMetric> metrics = new ArrayList<PoolArenaMetric>(this.heapArenas.length);
      for (int i = 0; i < this.heapArenas.length; i++) {
        PoolArena.HeapArena arena = new PoolArena.HeapArena(this, pageSize, maxOrder, pageShifts, this.chunkSize, directMemoryCacheAlignment);
        this.heapArenas[i] = arena;
        metrics.add(arena);
      } 
      this.heapArenaMetrics = Collections.unmodifiableList(metrics);
    } else {
      this.heapArenas = null;
      this.heapArenaMetrics = Collections.emptyList();
    } 
    if (nDirectArena > 0) {
      this.directArenas = newArenaArray(nDirectArena);
      List<PoolArenaMetric> metrics = new ArrayList<PoolArenaMetric>(this.directArenas.length);
      for (int i = 0; i < this.directArenas.length; i++) {
        PoolArena.DirectArena arena = new PoolArena.DirectArena(this, pageSize, maxOrder, pageShifts, this.chunkSize, directMemoryCacheAlignment);
        this.directArenas[i] = arena;
        metrics.add(arena);
      } 
      this.directArenaMetrics = Collections.unmodifiableList(metrics);
    } else {
      this.directArenas = null;
      this.directArenaMetrics = Collections.emptyList();
    } 
    this.metric = new PooledByteBufAllocatorMetric(this);
  }
  
  private static <T> PoolArena<T>[] newArenaArray(int size) {
    return (PoolArena<T>[])new PoolArena[size];
  }
  
  private static int validateAndCalculatePageShifts(int pageSize) {
    if (pageSize < 4096)
      throw new IllegalArgumentException("pageSize: " + pageSize + " (expected: " + 'က' + ")"); 
    if ((pageSize & pageSize - 1) != 0)
      throw new IllegalArgumentException("pageSize: " + pageSize + " (expected: power of 2)"); 
    return 31 - Integer.numberOfLeadingZeros(pageSize);
  }
  
  private static int validateAndCalculateChunkSize(int pageSize, int maxOrder) {
    if (maxOrder > 14)
      throw new IllegalArgumentException("maxOrder: " + maxOrder + " (expected: 0-14)"); 
    int chunkSize = pageSize;
    for (int i = maxOrder; i > 0; i--) {
      if (chunkSize > 536870912)
        throw new IllegalArgumentException(String.format("pageSize (%d) << maxOrder (%d) must not exceed %d", new Object[] { Integer.valueOf(pageSize), Integer.valueOf(maxOrder), Integer.valueOf(1073741824) })); 
      chunkSize <<= 1;
    } 
    return chunkSize;
  }
  
  protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
    ByteBuf buf;
    PoolThreadCache cache = (PoolThreadCache)this.threadCache.get();
    PoolArena<byte[]> heapArena = cache.heapArena;
    if (heapArena != null) {
      buf = heapArena.allocate(cache, initialCapacity, maxCapacity);
    } else {
      buf = PlatformDependent.hasUnsafe() ? new UnpooledUnsafeHeapByteBuf(this, initialCapacity, maxCapacity) : new UnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
    } 
    return toLeakAwareBuffer(buf);
  }
  
  protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
    ByteBuf buf;
    PoolThreadCache cache = (PoolThreadCache)this.threadCache.get();
    PoolArena<ByteBuffer> directArena = cache.directArena;
    if (directArena != null) {
      buf = directArena.allocate(cache, initialCapacity, maxCapacity);
    } else {
      buf = PlatformDependent.hasUnsafe() ? UnsafeByteBufUtil.newUnsafeDirectByteBuf(this, initialCapacity, maxCapacity) : new UnpooledDirectByteBuf(this, initialCapacity, maxCapacity);
    } 
    return toLeakAwareBuffer(buf);
  }
  
  public static int defaultNumHeapArena() {
    return DEFAULT_NUM_HEAP_ARENA;
  }
  
  public static int defaultNumDirectArena() {
    return DEFAULT_NUM_DIRECT_ARENA;
  }
  
  public static int defaultPageSize() {
    return DEFAULT_PAGE_SIZE;
  }
  
  public static int defaultMaxOrder() {
    return DEFAULT_MAX_ORDER;
  }
  
  public static boolean defaultUseCacheForAllThreads() {
    return DEFAULT_USE_CACHE_FOR_ALL_THREADS;
  }
  
  public static boolean defaultPreferDirect() {
    return PlatformDependent.directBufferPreferred();
  }
  
  public static int defaultTinyCacheSize() {
    return DEFAULT_TINY_CACHE_SIZE;
  }
  
  public static int defaultSmallCacheSize() {
    return DEFAULT_SMALL_CACHE_SIZE;
  }
  
  public static int defaultNormalCacheSize() {
    return DEFAULT_NORMAL_CACHE_SIZE;
  }
  
  public static boolean isDirectMemoryCacheAlignmentSupported() {
    return PlatformDependent.hasUnsafe();
  }
  
  public boolean isDirectBufferPooled() {
    return (this.directArenas != null);
  }
  
  @Deprecated
  public boolean hasThreadLocalCache() {
    return this.threadCache.isSet();
  }
  
  @Deprecated
  public void freeThreadLocalCache() {
    this.threadCache.remove();
  }
  
  final class PoolThreadLocalCache extends FastThreadLocal<PoolThreadCache> {
    private final boolean useCacheForAllThreads;
    
    PoolThreadLocalCache(boolean useCacheForAllThreads) {
      this.useCacheForAllThreads = useCacheForAllThreads;
    }
    
    protected synchronized PoolThreadCache initialValue() {
      PoolArena<byte[]> heapArena = (PoolArena)leastUsedArena((PoolArena[])PooledByteBufAllocator.this.heapArenas);
      PoolArena<ByteBuffer> directArena = leastUsedArena(PooledByteBufAllocator.this.directArenas);
      Thread current = Thread.currentThread();
      if (this.useCacheForAllThreads || current instanceof com.github.steveice10.netty.util.concurrent.FastThreadLocalThread)
        return new PoolThreadCache(heapArena, directArena, PooledByteBufAllocator.this
            .tinyCacheSize, PooledByteBufAllocator.this.smallCacheSize, PooledByteBufAllocator.this.normalCacheSize, PooledByteBufAllocator
            .DEFAULT_MAX_CACHED_BUFFER_CAPACITY, PooledByteBufAllocator.DEFAULT_CACHE_TRIM_INTERVAL); 
      return new PoolThreadCache(heapArena, directArena, 0, 0, 0, 0, 0);
    }
    
    protected void onRemoval(PoolThreadCache threadCache) {
      threadCache.free();
    }
    
    private <T> PoolArena<T> leastUsedArena(PoolArena<T>[] arenas) {
      if (arenas == null || arenas.length == 0)
        return null; 
      PoolArena<T> minArena = arenas[0];
      for (int i = 1; i < arenas.length; i++) {
        PoolArena<T> arena = arenas[i];
        if (arena.numThreadCaches.get() < minArena.numThreadCaches.get())
          minArena = arena; 
      } 
      return minArena;
    }
  }
  
  public PooledByteBufAllocatorMetric metric() {
    return this.metric;
  }
  
  @Deprecated
  public int numHeapArenas() {
    return this.heapArenaMetrics.size();
  }
  
  @Deprecated
  public int numDirectArenas() {
    return this.directArenaMetrics.size();
  }
  
  @Deprecated
  public List<PoolArenaMetric> heapArenas() {
    return this.heapArenaMetrics;
  }
  
  @Deprecated
  public List<PoolArenaMetric> directArenas() {
    return this.directArenaMetrics;
  }
  
  @Deprecated
  public int numThreadLocalCaches() {
    PoolArena<?>[] arenas = (this.heapArenas != null) ? (PoolArena<?>[])this.heapArenas : (PoolArena<?>[])this.directArenas;
    if (arenas == null)
      return 0; 
    int total = 0;
    for (PoolArena<?> arena : arenas)
      total += arena.numThreadCaches.get(); 
    return total;
  }
  
  @Deprecated
  public int tinyCacheSize() {
    return this.tinyCacheSize;
  }
  
  @Deprecated
  public int smallCacheSize() {
    return this.smallCacheSize;
  }
  
  @Deprecated
  public int normalCacheSize() {
    return this.normalCacheSize;
  }
  
  @Deprecated
  public final int chunkSize() {
    return this.chunkSize;
  }
  
  final long usedHeapMemory() {
    return usedMemory((PoolArena<?>[])this.heapArenas);
  }
  
  final long usedDirectMemory() {
    return usedMemory((PoolArena<?>[])this.directArenas);
  }
  
  private static long usedMemory(PoolArena<?>... arenas) {
    if (arenas == null)
      return -1L; 
    long used = 0L;
    for (PoolArena<?> arena : arenas) {
      used += arena.numActiveBytes();
      if (used < 0L)
        return Long.MAX_VALUE; 
    } 
    return used;
  }
  
  final PoolThreadCache threadCache() {
    PoolThreadCache cache = (PoolThreadCache)this.threadCache.get();
    assert cache != null;
    return cache;
  }
  
  public String dumpStats() {
    int heapArenasLen = (this.heapArenas == null) ? 0 : this.heapArenas.length;
    StringBuilder buf = (new StringBuilder(512)).append(heapArenasLen).append(" heap arena(s):").append(StringUtil.NEWLINE);
    if (heapArenasLen > 0)
      for (PoolArena<byte[]> a : this.heapArenas)
        buf.append(a);  
    int directArenasLen = (this.directArenas == null) ? 0 : this.directArenas.length;
    buf.append(directArenasLen)
      .append(" direct arena(s):")
      .append(StringUtil.NEWLINE);
    if (directArenasLen > 0)
      for (PoolArena<ByteBuffer> a : this.directArenas)
        buf.append(a);  
    return buf.toString();
  }
}

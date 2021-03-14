package com.github.steveice10.netty.buffer;

final class PoolSubpage<T> implements PoolSubpageMetric {
  final PoolChunk<T> chunk;
  
  private final int memoryMapIdx;
  
  private final int runOffset;
  
  private final int pageSize;
  
  private final long[] bitmap;
  
  PoolSubpage<T> prev;
  
  PoolSubpage<T> next;
  
  boolean doNotDestroy;
  
  int elemSize;
  
  private int maxNumElems;
  
  private int bitmapLength;
  
  private int nextAvail;
  
  private int numAvail;
  
  PoolSubpage(int pageSize) {
    this.chunk = null;
    this.memoryMapIdx = -1;
    this.runOffset = -1;
    this.elemSize = -1;
    this.pageSize = pageSize;
    this.bitmap = null;
  }
  
  PoolSubpage(PoolSubpage<T> head, PoolChunk<T> chunk, int memoryMapIdx, int runOffset, int pageSize, int elemSize) {
    this.chunk = chunk;
    this.memoryMapIdx = memoryMapIdx;
    this.runOffset = runOffset;
    this.pageSize = pageSize;
    this.bitmap = new long[pageSize >>> 10];
    init(head, elemSize);
  }
  
  void init(PoolSubpage<T> head, int elemSize) {
    this.doNotDestroy = true;
    this.elemSize = elemSize;
    if (elemSize != 0) {
      this.maxNumElems = this.numAvail = this.pageSize / elemSize;
      this.nextAvail = 0;
      this.bitmapLength = this.maxNumElems >>> 6;
      if ((this.maxNumElems & 0x3F) != 0)
        this.bitmapLength++; 
      for (int i = 0; i < this.bitmapLength; i++)
        this.bitmap[i] = 0L; 
    } 
    addToPool(head);
  }
  
  long allocate() {
    if (this.elemSize == 0)
      return toHandle(0); 
    if (this.numAvail == 0 || !this.doNotDestroy)
      return -1L; 
    int bitmapIdx = getNextAvail();
    int q = bitmapIdx >>> 6;
    int r = bitmapIdx & 0x3F;
    assert (this.bitmap[q] >>> r & 0x1L) == 0L;
    this.bitmap[q] = this.bitmap[q] | 1L << r;
    if (--this.numAvail == 0)
      removeFromPool(); 
    return toHandle(bitmapIdx);
  }
  
  boolean free(PoolSubpage<T> head, int bitmapIdx) {
    if (this.elemSize == 0)
      return true; 
    int q = bitmapIdx >>> 6;
    int r = bitmapIdx & 0x3F;
    assert (this.bitmap[q] >>> r & 0x1L) != 0L;
    this.bitmap[q] = this.bitmap[q] ^ 1L << r;
    setNextAvail(bitmapIdx);
    if (this.numAvail++ == 0) {
      addToPool(head);
      return true;
    } 
    if (this.numAvail != this.maxNumElems)
      return true; 
    if (this.prev == this.next)
      return true; 
    this.doNotDestroy = false;
    removeFromPool();
    return false;
  }
  
  private void addToPool(PoolSubpage<T> head) {
    assert this.prev == null && this.next == null;
    this.prev = head;
    this.next = head.next;
    this.next.prev = this;
    head.next = this;
  }
  
  private void removeFromPool() {
    assert this.prev != null && this.next != null;
    this.prev.next = this.next;
    this.next.prev = this.prev;
    this.next = null;
    this.prev = null;
  }
  
  private void setNextAvail(int bitmapIdx) {
    this.nextAvail = bitmapIdx;
  }
  
  private int getNextAvail() {
    int nextAvail = this.nextAvail;
    if (nextAvail >= 0) {
      this.nextAvail = -1;
      return nextAvail;
    } 
    return findNextAvail();
  }
  
  private int findNextAvail() {
    long[] bitmap = this.bitmap;
    int bitmapLength = this.bitmapLength;
    for (int i = 0; i < bitmapLength; i++) {
      long bits = bitmap[i];
      if ((bits ^ 0xFFFFFFFFFFFFFFFFL) != 0L)
        return findNextAvail0(i, bits); 
    } 
    return -1;
  }
  
  private int findNextAvail0(int i, long bits) {
    int maxNumElems = this.maxNumElems;
    int baseVal = i << 6;
    for (int j = 0; j < 64; j++) {
      if ((bits & 0x1L) == 0L) {
        int val = baseVal | j;
        if (val < maxNumElems)
          return val; 
        break;
      } 
      bits >>>= 1L;
    } 
    return -1;
  }
  
  private long toHandle(int bitmapIdx) {
    return 0x4000000000000000L | bitmapIdx << 32L | this.memoryMapIdx;
  }
  
  public String toString() {
    boolean doNotDestroy;
    int elemSize;
    int numAvail;
    int maxNumElems;
    synchronized (this.chunk.arena) {
      if (!this.doNotDestroy) {
        doNotDestroy = false;
        maxNumElems = numAvail = elemSize = -1;
      } else {
        doNotDestroy = true;
        maxNumElems = this.maxNumElems;
        numAvail = this.numAvail;
        elemSize = this.elemSize;
      } 
    } 
    if (!doNotDestroy)
      return "(" + this.memoryMapIdx + ": not in use)"; 
    return "(" + this.memoryMapIdx + ": " + (maxNumElems - numAvail) + '/' + maxNumElems + ", offset: " + this.runOffset + ", length: " + this.pageSize + ", elemSize: " + elemSize + ')';
  }
  
  public int maxNumElements() {
    synchronized (this.chunk.arena) {
      return this.maxNumElems;
    } 
  }
  
  public int numAvailable() {
    synchronized (this.chunk.arena) {
      return this.numAvail;
    } 
  }
  
  public int elementSize() {
    synchronized (this.chunk.arena) {
      return this.elemSize;
    } 
  }
  
  public int pageSize() {
    return this.pageSize;
  }
  
  void destroy() {
    if (this.chunk != null)
      this.chunk.destroy(); 
  }
}

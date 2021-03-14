package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class PoolChunkList<T> implements PoolChunkListMetric {
  private static final Iterator<PoolChunkMetric> EMPTY_METRICS = Collections.<PoolChunkMetric>emptyList().iterator();
  
  private final PoolArena<T> arena;
  
  private final PoolChunkList<T> nextList;
  
  private final int minUsage;
  
  private final int maxUsage;
  
  private final int maxCapacity;
  
  private PoolChunk<T> head;
  
  private PoolChunkList<T> prevList;
  
  PoolChunkList(PoolArena<T> arena, PoolChunkList<T> nextList, int minUsage, int maxUsage, int chunkSize) {
    assert minUsage <= maxUsage;
    this.arena = arena;
    this.nextList = nextList;
    this.minUsage = minUsage;
    this.maxUsage = maxUsage;
    this.maxCapacity = calculateMaxCapacity(minUsage, chunkSize);
  }
  
  private static int calculateMaxCapacity(int minUsage, int chunkSize) {
    minUsage = minUsage0(minUsage);
    if (minUsage == 100)
      return 0; 
    return (int)(chunkSize * (100L - minUsage) / 100L);
  }
  
  void prevList(PoolChunkList<T> prevList) {
    assert this.prevList == null;
    this.prevList = prevList;
  }
  
  boolean allocate(PooledByteBuf<T> buf, int reqCapacity, int normCapacity) {
    long handle;
    if (this.head == null || normCapacity > this.maxCapacity)
      return false; 
    PoolChunk<T> cur = this.head;
    while (true) {
      handle = cur.allocate(normCapacity);
      if (handle < 0L) {
        cur = cur.next;
        if (cur == null)
          return false; 
        continue;
      } 
      break;
    } 
    cur.initBuf(buf, handle, reqCapacity);
    if (cur.usage() >= this.maxUsage) {
      remove(cur);
      this.nextList.add(cur);
    } 
    return true;
  }
  
  boolean free(PoolChunk<T> chunk, long handle) {
    chunk.free(handle);
    if (chunk.usage() < this.minUsage) {
      remove(chunk);
      return move0(chunk);
    } 
    return true;
  }
  
  private boolean move(PoolChunk<T> chunk) {
    assert chunk.usage() < this.maxUsage;
    if (chunk.usage() < this.minUsage)
      return move0(chunk); 
    add0(chunk);
    return true;
  }
  
  private boolean move0(PoolChunk<T> chunk) {
    if (this.prevList == null) {
      assert chunk.usage() == 0;
      return false;
    } 
    return this.prevList.move(chunk);
  }
  
  void add(PoolChunk<T> chunk) {
    if (chunk.usage() >= this.maxUsage) {
      this.nextList.add(chunk);
      return;
    } 
    add0(chunk);
  }
  
  void add0(PoolChunk<T> chunk) {
    chunk.parent = this;
    if (this.head == null) {
      this.head = chunk;
      chunk.prev = null;
      chunk.next = null;
    } else {
      chunk.prev = null;
      chunk.next = this.head;
      this.head.prev = chunk;
      this.head = chunk;
    } 
  }
  
  private void remove(PoolChunk<T> cur) {
    if (cur == this.head) {
      this.head = cur.next;
      if (this.head != null)
        this.head.prev = null; 
    } else {
      PoolChunk<T> next = cur.next;
      cur.prev.next = next;
      if (next != null)
        next.prev = cur.prev; 
    } 
  }
  
  public int minUsage() {
    return minUsage0(this.minUsage);
  }
  
  public int maxUsage() {
    return Math.min(this.maxUsage, 100);
  }
  
  private static int minUsage0(int value) {
    return Math.max(1, value);
  }
  
  public Iterator<PoolChunkMetric> iterator() {
    synchronized (this.arena) {
      if (this.head == null)
        return EMPTY_METRICS; 
      List<PoolChunkMetric> metrics = new ArrayList<PoolChunkMetric>();
      PoolChunk<T> cur = this.head;
      do {
        metrics.add(cur);
        cur = cur.next;
      } while (cur != null);
      return metrics.iterator();
    } 
  }
  
  public String toString() {
    StringBuilder buf = new StringBuilder();
    synchronized (this.arena) {
      if (this.head == null)
        return "none"; 
      PoolChunk<T> cur = this.head;
      while (true) {
        buf.append(cur);
        cur = cur.next;
        if (cur == null)
          break; 
        buf.append(StringUtil.NEWLINE);
      } 
    } 
    return buf.toString();
  }
  
  void destroy(PoolArena<T> arena) {
    PoolChunk<T> chunk = this.head;
    while (chunk != null) {
      arena.destroyChunk(chunk);
      chunk = chunk.next;
    } 
    this.head = null;
  }
}

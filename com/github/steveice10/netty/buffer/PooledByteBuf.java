package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.Recycler;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

abstract class PooledByteBuf<T> extends AbstractReferenceCountedByteBuf {
  private final Recycler.Handle<PooledByteBuf<T>> recyclerHandle;
  
  protected PoolChunk<T> chunk;
  
  protected long handle;
  
  protected T memory;
  
  protected int offset;
  
  protected int length;
  
  int maxLength;
  
  PoolThreadCache cache;
  
  private ByteBuffer tmpNioBuf;
  
  private ByteBufAllocator allocator;
  
  protected PooledByteBuf(Recycler.Handle<? extends PooledByteBuf<T>> recyclerHandle, int maxCapacity) {
    super(maxCapacity);
    this.recyclerHandle = (Recycler.Handle)recyclerHandle;
  }
  
  void init(PoolChunk<T> chunk, long handle, int offset, int length, int maxLength, PoolThreadCache cache) {
    init0(chunk, handle, offset, length, maxLength, cache);
  }
  
  void initUnpooled(PoolChunk<T> chunk, int length) {
    init0(chunk, 0L, chunk.offset, length, length, (PoolThreadCache)null);
  }
  
  private void init0(PoolChunk<T> chunk, long handle, int offset, int length, int maxLength, PoolThreadCache cache) {
    assert handle >= 0L;
    assert chunk != null;
    this.chunk = chunk;
    this.memory = chunk.memory;
    this.allocator = chunk.arena.parent;
    this.cache = cache;
    this.handle = handle;
    this.offset = offset;
    this.length = length;
    this.maxLength = maxLength;
    this.tmpNioBuf = null;
  }
  
  final void reuse(int maxCapacity) {
    maxCapacity(maxCapacity);
    setRefCnt(1);
    setIndex0(0, 0);
    discardMarks();
  }
  
  public final int capacity() {
    return this.length;
  }
  
  public final ByteBuf capacity(int newCapacity) {
    checkNewCapacity(newCapacity);
    if (this.chunk.unpooled) {
      if (newCapacity == this.length)
        return this; 
    } else if (newCapacity > this.length) {
      if (newCapacity <= this.maxLength) {
        this.length = newCapacity;
        return this;
      } 
    } else if (newCapacity < this.length) {
      if (newCapacity > this.maxLength >>> 1)
        if (this.maxLength <= 512) {
          if (newCapacity > this.maxLength - 16) {
            this.length = newCapacity;
            setIndex(Math.min(readerIndex(), newCapacity), Math.min(writerIndex(), newCapacity));
            return this;
          } 
        } else {
          this.length = newCapacity;
          setIndex(Math.min(readerIndex(), newCapacity), Math.min(writerIndex(), newCapacity));
          return this;
        }  
    } else {
      return this;
    } 
    this.chunk.arena.reallocate(this, newCapacity, true);
    return this;
  }
  
  public final ByteBufAllocator alloc() {
    return this.allocator;
  }
  
  public final ByteOrder order() {
    return ByteOrder.BIG_ENDIAN;
  }
  
  public final ByteBuf unwrap() {
    return null;
  }
  
  public final ByteBuf retainedDuplicate() {
    return PooledDuplicatedByteBuf.newInstance(this, this, readerIndex(), writerIndex());
  }
  
  public final ByteBuf retainedSlice() {
    int index = readerIndex();
    return retainedSlice(index, writerIndex() - index);
  }
  
  public final ByteBuf retainedSlice(int index, int length) {
    return PooledSlicedByteBuf.newInstance(this, this, index, length);
  }
  
  protected final ByteBuffer internalNioBuffer() {
    ByteBuffer tmpNioBuf = this.tmpNioBuf;
    if (tmpNioBuf == null)
      this.tmpNioBuf = tmpNioBuf = newInternalNioBuffer(this.memory); 
    return tmpNioBuf;
  }
  
  protected abstract ByteBuffer newInternalNioBuffer(T paramT);
  
  protected final void deallocate() {
    if (this.handle >= 0L) {
      long handle = this.handle;
      this.handle = -1L;
      this.memory = null;
      this.tmpNioBuf = null;
      this.chunk.arena.free(this.chunk, handle, this.maxLength, this.cache);
      this.chunk = null;
      recycle();
    } 
  }
  
  private void recycle() {
    this.recyclerHandle.recycle(this);
  }
  
  protected final int idx(int index) {
    return this.offset + index;
  }
}

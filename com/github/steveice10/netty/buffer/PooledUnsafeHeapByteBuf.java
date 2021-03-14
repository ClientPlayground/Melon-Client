package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.Recycler;
import com.github.steveice10.netty.util.internal.PlatformDependent;

final class PooledUnsafeHeapByteBuf extends PooledHeapByteBuf {
  private static final Recycler<PooledUnsafeHeapByteBuf> RECYCLER = new Recycler<PooledUnsafeHeapByteBuf>() {
      protected PooledUnsafeHeapByteBuf newObject(Recycler.Handle<PooledUnsafeHeapByteBuf> handle) {
        return new PooledUnsafeHeapByteBuf(handle, 0);
      }
    };
  
  static PooledUnsafeHeapByteBuf newUnsafeInstance(int maxCapacity) {
    PooledUnsafeHeapByteBuf buf = (PooledUnsafeHeapByteBuf)RECYCLER.get();
    buf.reuse(maxCapacity);
    return buf;
  }
  
  private PooledUnsafeHeapByteBuf(Recycler.Handle<PooledUnsafeHeapByteBuf> recyclerHandle, int maxCapacity) {
    super((Recycler.Handle)recyclerHandle, maxCapacity);
  }
  
  protected byte _getByte(int index) {
    return UnsafeByteBufUtil.getByte(this.memory, idx(index));
  }
  
  protected short _getShort(int index) {
    return UnsafeByteBufUtil.getShort(this.memory, idx(index));
  }
  
  protected short _getShortLE(int index) {
    return UnsafeByteBufUtil.getShortLE(this.memory, idx(index));
  }
  
  protected int _getUnsignedMedium(int index) {
    return UnsafeByteBufUtil.getUnsignedMedium(this.memory, idx(index));
  }
  
  protected int _getUnsignedMediumLE(int index) {
    return UnsafeByteBufUtil.getUnsignedMediumLE(this.memory, idx(index));
  }
  
  protected int _getInt(int index) {
    return UnsafeByteBufUtil.getInt(this.memory, idx(index));
  }
  
  protected int _getIntLE(int index) {
    return UnsafeByteBufUtil.getIntLE(this.memory, idx(index));
  }
  
  protected long _getLong(int index) {
    return UnsafeByteBufUtil.getLong(this.memory, idx(index));
  }
  
  protected long _getLongLE(int index) {
    return UnsafeByteBufUtil.getLongLE(this.memory, idx(index));
  }
  
  protected void _setByte(int index, int value) {
    UnsafeByteBufUtil.setByte(this.memory, idx(index), value);
  }
  
  protected void _setShort(int index, int value) {
    UnsafeByteBufUtil.setShort(this.memory, idx(index), value);
  }
  
  protected void _setShortLE(int index, int value) {
    UnsafeByteBufUtil.setShortLE(this.memory, idx(index), value);
  }
  
  protected void _setMedium(int index, int value) {
    UnsafeByteBufUtil.setMedium(this.memory, idx(index), value);
  }
  
  protected void _setMediumLE(int index, int value) {
    UnsafeByteBufUtil.setMediumLE(this.memory, idx(index), value);
  }
  
  protected void _setInt(int index, int value) {
    UnsafeByteBufUtil.setInt(this.memory, idx(index), value);
  }
  
  protected void _setIntLE(int index, int value) {
    UnsafeByteBufUtil.setIntLE(this.memory, idx(index), value);
  }
  
  protected void _setLong(int index, long value) {
    UnsafeByteBufUtil.setLong(this.memory, idx(index), value);
  }
  
  protected void _setLongLE(int index, long value) {
    UnsafeByteBufUtil.setLongLE(this.memory, idx(index), value);
  }
  
  public ByteBuf setZero(int index, int length) {
    if (PlatformDependent.javaVersion() >= 7) {
      checkIndex(index, length);
      UnsafeByteBufUtil.setZero(this.memory, idx(index), length);
      return this;
    } 
    return super.setZero(index, length);
  }
  
  public ByteBuf writeZero(int length) {
    if (PlatformDependent.javaVersion() >= 7) {
      ensureWritable(length);
      int wIndex = this.writerIndex;
      UnsafeByteBufUtil.setZero(this.memory, idx(wIndex), length);
      this.writerIndex = wIndex + length;
      return this;
    } 
    return super.writeZero(length);
  }
  
  @Deprecated
  protected SwappedByteBuf newSwappedByteBuf() {
    if (PlatformDependent.isUnaligned())
      return new UnsafeHeapSwappedByteBuf(this); 
    return super.newSwappedByteBuf();
  }
}

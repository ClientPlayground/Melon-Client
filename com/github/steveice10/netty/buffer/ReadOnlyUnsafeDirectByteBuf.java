package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.nio.ByteBuffer;

final class ReadOnlyUnsafeDirectByteBuf extends ReadOnlyByteBufferBuf {
  private final long memoryAddress;
  
  ReadOnlyUnsafeDirectByteBuf(ByteBufAllocator allocator, ByteBuffer byteBuffer) {
    super(allocator, byteBuffer);
    this.memoryAddress = PlatformDependent.directBufferAddress(this.buffer);
  }
  
  protected byte _getByte(int index) {
    return UnsafeByteBufUtil.getByte(addr(index));
  }
  
  protected short _getShort(int index) {
    return UnsafeByteBufUtil.getShort(addr(index));
  }
  
  protected int _getUnsignedMedium(int index) {
    return UnsafeByteBufUtil.getUnsignedMedium(addr(index));
  }
  
  protected int _getInt(int index) {
    return UnsafeByteBufUtil.getInt(addr(index));
  }
  
  protected long _getLong(int index) {
    return UnsafeByteBufUtil.getLong(addr(index));
  }
  
  public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
    checkIndex(index, length);
    if (dst == null)
      throw new NullPointerException("dst"); 
    if (dstIndex < 0 || dstIndex > dst.capacity() - length)
      throw new IndexOutOfBoundsException("dstIndex: " + dstIndex); 
    if (dst.hasMemoryAddress()) {
      PlatformDependent.copyMemory(addr(index), dst.memoryAddress() + dstIndex, length);
    } else if (dst.hasArray()) {
      PlatformDependent.copyMemory(addr(index), dst.array(), dst.arrayOffset() + dstIndex, length);
    } else {
      dst.setBytes(dstIndex, this, index, length);
    } 
    return this;
  }
  
  public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
    checkIndex(index, length);
    if (dst == null)
      throw new NullPointerException("dst"); 
    if (dstIndex < 0 || dstIndex > dst.length - length)
      throw new IndexOutOfBoundsException(String.format("dstIndex: %d, length: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(dstIndex), Integer.valueOf(length), Integer.valueOf(dst.length) })); 
    if (length != 0)
      PlatformDependent.copyMemory(addr(index), dst, dstIndex, length); 
    return this;
  }
  
  public ByteBuf getBytes(int index, ByteBuffer dst) {
    checkIndex(index);
    if (dst == null)
      throw new NullPointerException("dst"); 
    int bytesToCopy = Math.min(capacity() - index, dst.remaining());
    ByteBuffer tmpBuf = internalNioBuffer();
    tmpBuf.clear().position(index).limit(index + bytesToCopy);
    dst.put(tmpBuf);
    return this;
  }
  
  public ByteBuf copy(int index, int length) {
    checkIndex(index, length);
    ByteBuf copy = alloc().directBuffer(length, maxCapacity());
    if (length != 0)
      if (copy.hasMemoryAddress()) {
        PlatformDependent.copyMemory(addr(index), copy.memoryAddress(), length);
        copy.setIndex(0, length);
      } else {
        copy.writeBytes(this, index, length);
      }  
    return copy;
  }
  
  public boolean hasMemoryAddress() {
    return true;
  }
  
  public long memoryAddress() {
    return this.memoryAddress;
  }
  
  private long addr(int index) {
    return this.memoryAddress + index;
  }
}

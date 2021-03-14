package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.Recycler;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

class PooledHeapByteBuf extends PooledByteBuf<byte[]> {
  private static final Recycler<PooledHeapByteBuf> RECYCLER = new Recycler<PooledHeapByteBuf>() {
      protected PooledHeapByteBuf newObject(Recycler.Handle<PooledHeapByteBuf> handle) {
        return new PooledHeapByteBuf(handle, 0);
      }
    };
  
  static PooledHeapByteBuf newInstance(int maxCapacity) {
    PooledHeapByteBuf buf = (PooledHeapByteBuf)RECYCLER.get();
    buf.reuse(maxCapacity);
    return buf;
  }
  
  PooledHeapByteBuf(Recycler.Handle<? extends PooledHeapByteBuf> recyclerHandle, int maxCapacity) {
    super((Recycler.Handle)recyclerHandle, maxCapacity);
  }
  
  public final boolean isDirect() {
    return false;
  }
  
  protected byte _getByte(int index) {
    return HeapByteBufUtil.getByte(this.memory, idx(index));
  }
  
  protected short _getShort(int index) {
    return HeapByteBufUtil.getShort(this.memory, idx(index));
  }
  
  protected short _getShortLE(int index) {
    return HeapByteBufUtil.getShortLE(this.memory, idx(index));
  }
  
  protected int _getUnsignedMedium(int index) {
    return HeapByteBufUtil.getUnsignedMedium(this.memory, idx(index));
  }
  
  protected int _getUnsignedMediumLE(int index) {
    return HeapByteBufUtil.getUnsignedMediumLE(this.memory, idx(index));
  }
  
  protected int _getInt(int index) {
    return HeapByteBufUtil.getInt(this.memory, idx(index));
  }
  
  protected int _getIntLE(int index) {
    return HeapByteBufUtil.getIntLE(this.memory, idx(index));
  }
  
  protected long _getLong(int index) {
    return HeapByteBufUtil.getLong(this.memory, idx(index));
  }
  
  protected long _getLongLE(int index) {
    return HeapByteBufUtil.getLongLE(this.memory, idx(index));
  }
  
  public final ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
    checkDstIndex(index, length, dstIndex, dst.capacity());
    if (dst.hasMemoryAddress()) {
      PlatformDependent.copyMemory(this.memory, idx(index), dst.memoryAddress() + dstIndex, length);
    } else if (dst.hasArray()) {
      getBytes(index, dst.array(), dst.arrayOffset() + dstIndex, length);
    } else {
      dst.setBytes(dstIndex, this.memory, idx(index), length);
    } 
    return this;
  }
  
  public final ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
    checkDstIndex(index, length, dstIndex, dst.length);
    System.arraycopy(this.memory, idx(index), dst, dstIndex, length);
    return this;
  }
  
  public final ByteBuf getBytes(int index, ByteBuffer dst) {
    checkIndex(index, dst.remaining());
    dst.put(this.memory, idx(index), dst.remaining());
    return this;
  }
  
  public final ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
    checkIndex(index, length);
    out.write(this.memory, idx(index), length);
    return this;
  }
  
  public final int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
    return getBytes(index, out, length, false);
  }
  
  private int getBytes(int index, GatheringByteChannel out, int length, boolean internal) throws IOException {
    ByteBuffer tmpBuf;
    checkIndex(index, length);
    index = idx(index);
    if (internal) {
      tmpBuf = internalNioBuffer();
    } else {
      tmpBuf = ByteBuffer.wrap(this.memory);
    } 
    return out.write((ByteBuffer)tmpBuf.clear().position(index).limit(index + length));
  }
  
  public final int getBytes(int index, FileChannel out, long position, int length) throws IOException {
    return getBytes(index, out, position, length, false);
  }
  
  private int getBytes(int index, FileChannel out, long position, int length, boolean internal) throws IOException {
    checkIndex(index, length);
    index = idx(index);
    ByteBuffer tmpBuf = internal ? internalNioBuffer() : ByteBuffer.wrap(this.memory);
    return out.write((ByteBuffer)tmpBuf.clear().position(index).limit(index + length), position);
  }
  
  public final int readBytes(GatheringByteChannel out, int length) throws IOException {
    checkReadableBytes(length);
    int readBytes = getBytes(this.readerIndex, out, length, true);
    this.readerIndex += readBytes;
    return readBytes;
  }
  
  public final int readBytes(FileChannel out, long position, int length) throws IOException {
    checkReadableBytes(length);
    int readBytes = getBytes(this.readerIndex, out, position, length, true);
    this.readerIndex += readBytes;
    return readBytes;
  }
  
  protected void _setByte(int index, int value) {
    HeapByteBufUtil.setByte(this.memory, idx(index), value);
  }
  
  protected void _setShort(int index, int value) {
    HeapByteBufUtil.setShort(this.memory, idx(index), value);
  }
  
  protected void _setShortLE(int index, int value) {
    HeapByteBufUtil.setShortLE(this.memory, idx(index), value);
  }
  
  protected void _setMedium(int index, int value) {
    HeapByteBufUtil.setMedium(this.memory, idx(index), value);
  }
  
  protected void _setMediumLE(int index, int value) {
    HeapByteBufUtil.setMediumLE(this.memory, idx(index), value);
  }
  
  protected void _setInt(int index, int value) {
    HeapByteBufUtil.setInt(this.memory, idx(index), value);
  }
  
  protected void _setIntLE(int index, int value) {
    HeapByteBufUtil.setIntLE(this.memory, idx(index), value);
  }
  
  protected void _setLong(int index, long value) {
    HeapByteBufUtil.setLong(this.memory, idx(index), value);
  }
  
  protected void _setLongLE(int index, long value) {
    HeapByteBufUtil.setLongLE(this.memory, idx(index), value);
  }
  
  public final ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
    checkSrcIndex(index, length, srcIndex, src.capacity());
    if (src.hasMemoryAddress()) {
      PlatformDependent.copyMemory(src.memoryAddress() + srcIndex, this.memory, idx(index), length);
    } else if (src.hasArray()) {
      setBytes(index, src.array(), src.arrayOffset() + srcIndex, length);
    } else {
      src.getBytes(srcIndex, this.memory, idx(index), length);
    } 
    return this;
  }
  
  public final ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
    checkSrcIndex(index, length, srcIndex, src.length);
    System.arraycopy(src, srcIndex, this.memory, idx(index), length);
    return this;
  }
  
  public final ByteBuf setBytes(int index, ByteBuffer src) {
    int length = src.remaining();
    checkIndex(index, length);
    src.get(this.memory, idx(index), length);
    return this;
  }
  
  public final int setBytes(int index, InputStream in, int length) throws IOException {
    checkIndex(index, length);
    return in.read(this.memory, idx(index), length);
  }
  
  public final int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
    checkIndex(index, length);
    index = idx(index);
    try {
      return in.read((ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length));
    } catch (ClosedChannelException ignored) {
      return -1;
    } 
  }
  
  public final int setBytes(int index, FileChannel in, long position, int length) throws IOException {
    checkIndex(index, length);
    index = idx(index);
    try {
      return in.read((ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length), position);
    } catch (ClosedChannelException ignored) {
      return -1;
    } 
  }
  
  public final ByteBuf copy(int index, int length) {
    checkIndex(index, length);
    ByteBuf copy = alloc().heapBuffer(length, maxCapacity());
    copy.writeBytes(this.memory, idx(index), length);
    return copy;
  }
  
  public final int nioBufferCount() {
    return 1;
  }
  
  public final ByteBuffer[] nioBuffers(int index, int length) {
    return new ByteBuffer[] { nioBuffer(index, length) };
  }
  
  public final ByteBuffer nioBuffer(int index, int length) {
    checkIndex(index, length);
    index = idx(index);
    ByteBuffer buf = ByteBuffer.wrap(this.memory, index, length);
    return buf.slice();
  }
  
  public final ByteBuffer internalNioBuffer(int index, int length) {
    checkIndex(index, length);
    index = idx(index);
    return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
  }
  
  public final boolean hasArray() {
    return true;
  }
  
  public final byte[] array() {
    ensureAccessible();
    return this.memory;
  }
  
  public final int arrayOffset() {
    return this.offset;
  }
  
  public final boolean hasMemoryAddress() {
    return false;
  }
  
  public final long memoryAddress() {
    throw new UnsupportedOperationException();
  }
  
  protected final ByteBuffer newInternalNioBuffer(byte[] memory) {
    return ByteBuffer.wrap(memory);
  }
}

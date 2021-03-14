package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

public class UnpooledUnsafeDirectByteBuf extends AbstractReferenceCountedByteBuf {
  private final ByteBufAllocator alloc;
  
  private ByteBuffer tmpNioBuf;
  
  private int capacity;
  
  private boolean doNotFree;
  
  ByteBuffer buffer;
  
  long memoryAddress;
  
  public UnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
    super(maxCapacity);
    if (alloc == null)
      throw new NullPointerException("alloc"); 
    if (initialCapacity < 0)
      throw new IllegalArgumentException("initialCapacity: " + initialCapacity); 
    if (maxCapacity < 0)
      throw new IllegalArgumentException("maxCapacity: " + maxCapacity); 
    if (initialCapacity > maxCapacity)
      throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] { Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) })); 
    this.alloc = alloc;
    setByteBuffer(allocateDirect(initialCapacity), false);
  }
  
  protected UnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, ByteBuffer initialBuffer, int maxCapacity) {
    this(alloc, initialBuffer.slice(), maxCapacity, false);
  }
  
  UnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, ByteBuffer initialBuffer, int maxCapacity, boolean doFree) {
    super(maxCapacity);
    if (alloc == null)
      throw new NullPointerException("alloc"); 
    if (initialBuffer == null)
      throw new NullPointerException("initialBuffer"); 
    if (!initialBuffer.isDirect())
      throw new IllegalArgumentException("initialBuffer is not a direct buffer."); 
    if (initialBuffer.isReadOnly())
      throw new IllegalArgumentException("initialBuffer is a read-only buffer."); 
    int initialCapacity = initialBuffer.remaining();
    if (initialCapacity > maxCapacity)
      throw new IllegalArgumentException(String.format("initialCapacity(%d) > maxCapacity(%d)", new Object[] { Integer.valueOf(initialCapacity), Integer.valueOf(maxCapacity) })); 
    this.alloc = alloc;
    this.doNotFree = !doFree;
    setByteBuffer(initialBuffer.order(ByteOrder.BIG_ENDIAN), false);
    writerIndex(initialCapacity);
  }
  
  protected ByteBuffer allocateDirect(int initialCapacity) {
    return ByteBuffer.allocateDirect(initialCapacity);
  }
  
  protected void freeDirect(ByteBuffer buffer) {
    PlatformDependent.freeDirectBuffer(buffer);
  }
  
  final void setByteBuffer(ByteBuffer buffer, boolean tryFree) {
    if (tryFree) {
      ByteBuffer oldBuffer = this.buffer;
      if (oldBuffer != null)
        if (this.doNotFree) {
          this.doNotFree = false;
        } else {
          freeDirect(oldBuffer);
        }  
    } 
    this.buffer = buffer;
    this.memoryAddress = PlatformDependent.directBufferAddress(buffer);
    this.tmpNioBuf = null;
    this.capacity = buffer.remaining();
  }
  
  public boolean isDirect() {
    return true;
  }
  
  public int capacity() {
    return this.capacity;
  }
  
  public ByteBuf capacity(int newCapacity) {
    checkNewCapacity(newCapacity);
    int readerIndex = readerIndex();
    int writerIndex = writerIndex();
    int oldCapacity = this.capacity;
    if (newCapacity > oldCapacity) {
      ByteBuffer oldBuffer = this.buffer;
      ByteBuffer newBuffer = allocateDirect(newCapacity);
      oldBuffer.position(0).limit(oldBuffer.capacity());
      newBuffer.position(0).limit(oldBuffer.capacity());
      newBuffer.put(oldBuffer);
      newBuffer.clear();
      setByteBuffer(newBuffer, true);
    } else if (newCapacity < oldCapacity) {
      ByteBuffer oldBuffer = this.buffer;
      ByteBuffer newBuffer = allocateDirect(newCapacity);
      if (readerIndex < newCapacity) {
        if (writerIndex > newCapacity)
          writerIndex(writerIndex = newCapacity); 
        oldBuffer.position(readerIndex).limit(writerIndex);
        newBuffer.position(readerIndex).limit(writerIndex);
        newBuffer.put(oldBuffer);
        newBuffer.clear();
      } else {
        setIndex(newCapacity, newCapacity);
      } 
      setByteBuffer(newBuffer, true);
    } 
    return this;
  }
  
  public ByteBufAllocator alloc() {
    return this.alloc;
  }
  
  public ByteOrder order() {
    return ByteOrder.BIG_ENDIAN;
  }
  
  public boolean hasArray() {
    return false;
  }
  
  public byte[] array() {
    throw new UnsupportedOperationException("direct buffer");
  }
  
  public int arrayOffset() {
    throw new UnsupportedOperationException("direct buffer");
  }
  
  public boolean hasMemoryAddress() {
    return true;
  }
  
  public long memoryAddress() {
    ensureAccessible();
    return this.memoryAddress;
  }
  
  protected byte _getByte(int index) {
    return UnsafeByteBufUtil.getByte(addr(index));
  }
  
  protected short _getShort(int index) {
    return UnsafeByteBufUtil.getShort(addr(index));
  }
  
  protected short _getShortLE(int index) {
    return UnsafeByteBufUtil.getShortLE(addr(index));
  }
  
  protected int _getUnsignedMedium(int index) {
    return UnsafeByteBufUtil.getUnsignedMedium(addr(index));
  }
  
  protected int _getUnsignedMediumLE(int index) {
    return UnsafeByteBufUtil.getUnsignedMediumLE(addr(index));
  }
  
  protected int _getInt(int index) {
    return UnsafeByteBufUtil.getInt(addr(index));
  }
  
  protected int _getIntLE(int index) {
    return UnsafeByteBufUtil.getIntLE(addr(index));
  }
  
  protected long _getLong(int index) {
    return UnsafeByteBufUtil.getLong(addr(index));
  }
  
  protected long _getLongLE(int index) {
    return UnsafeByteBufUtil.getLongLE(addr(index));
  }
  
  public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
    UnsafeByteBufUtil.getBytes(this, addr(index), index, dst, dstIndex, length);
    return this;
  }
  
  public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
    UnsafeByteBufUtil.getBytes(this, addr(index), index, dst, dstIndex, length);
    return this;
  }
  
  public ByteBuf getBytes(int index, ByteBuffer dst) {
    UnsafeByteBufUtil.getBytes(this, addr(index), index, dst);
    return this;
  }
  
  public ByteBuf readBytes(ByteBuffer dst) {
    int length = dst.remaining();
    checkReadableBytes(length);
    getBytes(this.readerIndex, dst);
    this.readerIndex += length;
    return this;
  }
  
  protected void _setByte(int index, int value) {
    UnsafeByteBufUtil.setByte(addr(index), value);
  }
  
  protected void _setShort(int index, int value) {
    UnsafeByteBufUtil.setShort(addr(index), value);
  }
  
  protected void _setShortLE(int index, int value) {
    UnsafeByteBufUtil.setShortLE(addr(index), value);
  }
  
  protected void _setMedium(int index, int value) {
    UnsafeByteBufUtil.setMedium(addr(index), value);
  }
  
  protected void _setMediumLE(int index, int value) {
    UnsafeByteBufUtil.setMediumLE(addr(index), value);
  }
  
  protected void _setInt(int index, int value) {
    UnsafeByteBufUtil.setInt(addr(index), value);
  }
  
  protected void _setIntLE(int index, int value) {
    UnsafeByteBufUtil.setIntLE(addr(index), value);
  }
  
  protected void _setLong(int index, long value) {
    UnsafeByteBufUtil.setLong(addr(index), value);
  }
  
  protected void _setLongLE(int index, long value) {
    UnsafeByteBufUtil.setLongLE(addr(index), value);
  }
  
  public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
    UnsafeByteBufUtil.setBytes(this, addr(index), index, src, srcIndex, length);
    return this;
  }
  
  public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
    UnsafeByteBufUtil.setBytes(this, addr(index), index, src, srcIndex, length);
    return this;
  }
  
  public ByteBuf setBytes(int index, ByteBuffer src) {
    UnsafeByteBufUtil.setBytes(this, addr(index), index, src);
    return this;
  }
  
  public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
    UnsafeByteBufUtil.getBytes(this, addr(index), index, out, length);
    return this;
  }
  
  public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
    return getBytes(index, out, length, false);
  }
  
  private int getBytes(int index, GatheringByteChannel out, int length, boolean internal) throws IOException {
    ByteBuffer tmpBuf;
    ensureAccessible();
    if (length == 0)
      return 0; 
    if (internal) {
      tmpBuf = internalNioBuffer();
    } else {
      tmpBuf = this.buffer.duplicate();
    } 
    tmpBuf.clear().position(index).limit(index + length);
    return out.write(tmpBuf);
  }
  
  public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
    return getBytes(index, out, position, length, false);
  }
  
  private int getBytes(int index, FileChannel out, long position, int length, boolean internal) throws IOException {
    ensureAccessible();
    if (length == 0)
      return 0; 
    ByteBuffer tmpBuf = internal ? internalNioBuffer() : this.buffer.duplicate();
    tmpBuf.clear().position(index).limit(index + length);
    return out.write(tmpBuf, position);
  }
  
  public int readBytes(GatheringByteChannel out, int length) throws IOException {
    checkReadableBytes(length);
    int readBytes = getBytes(this.readerIndex, out, length, true);
    this.readerIndex += readBytes;
    return readBytes;
  }
  
  public int readBytes(FileChannel out, long position, int length) throws IOException {
    checkReadableBytes(length);
    int readBytes = getBytes(this.readerIndex, out, position, length, true);
    this.readerIndex += readBytes;
    return readBytes;
  }
  
  public int setBytes(int index, InputStream in, int length) throws IOException {
    return UnsafeByteBufUtil.setBytes(this, addr(index), index, in, length);
  }
  
  public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
    ensureAccessible();
    ByteBuffer tmpBuf = internalNioBuffer();
    tmpBuf.clear().position(index).limit(index + length);
    try {
      return in.read(tmpBuf);
    } catch (ClosedChannelException ignored) {
      return -1;
    } 
  }
  
  public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
    ensureAccessible();
    ByteBuffer tmpBuf = internalNioBuffer();
    tmpBuf.clear().position(index).limit(index + length);
    try {
      return in.read(tmpBuf, position);
    } catch (ClosedChannelException ignored) {
      return -1;
    } 
  }
  
  public int nioBufferCount() {
    return 1;
  }
  
  public ByteBuffer[] nioBuffers(int index, int length) {
    return new ByteBuffer[] { nioBuffer(index, length) };
  }
  
  public ByteBuf copy(int index, int length) {
    return UnsafeByteBufUtil.copy(this, addr(index), index, length);
  }
  
  public ByteBuffer internalNioBuffer(int index, int length) {
    checkIndex(index, length);
    return (ByteBuffer)internalNioBuffer().clear().position(index).limit(index + length);
  }
  
  private ByteBuffer internalNioBuffer() {
    ByteBuffer tmpNioBuf = this.tmpNioBuf;
    if (tmpNioBuf == null)
      this.tmpNioBuf = tmpNioBuf = this.buffer.duplicate(); 
    return tmpNioBuf;
  }
  
  public ByteBuffer nioBuffer(int index, int length) {
    checkIndex(index, length);
    return ((ByteBuffer)this.buffer.duplicate().position(index).limit(index + length)).slice();
  }
  
  protected void deallocate() {
    ByteBuffer buffer = this.buffer;
    if (buffer == null)
      return; 
    this.buffer = null;
    if (!this.doNotFree)
      freeDirect(buffer); 
  }
  
  public ByteBuf unwrap() {
    return null;
  }
  
  long addr(int index) {
    return this.memoryAddress + index;
  }
  
  protected SwappedByteBuf newSwappedByteBuf() {
    if (PlatformDependent.isUnaligned())
      return new UnsafeDirectSwappedByteBuf(this); 
    return super.newSwappedByteBuf();
  }
  
  public ByteBuf setZero(int index, int length) {
    checkIndex(index, length);
    UnsafeByteBufUtil.setZero(addr(index), length);
    return this;
  }
  
  public ByteBuf writeZero(int length) {
    ensureWritable(length);
    int wIndex = this.writerIndex;
    UnsafeByteBufUtil.setZero(addr(wIndex), length);
    this.writerIndex = wIndex + length;
    return this;
  }
}

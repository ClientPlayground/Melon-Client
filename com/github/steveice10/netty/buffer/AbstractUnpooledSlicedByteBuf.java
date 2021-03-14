package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ByteProcessor;
import com.github.steveice10.netty.util.internal.MathUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

abstract class AbstractUnpooledSlicedByteBuf extends AbstractDerivedByteBuf {
  private final ByteBuf buffer;
  
  private final int adjustment;
  
  AbstractUnpooledSlicedByteBuf(ByteBuf buffer, int index, int length) {
    super(length);
    checkSliceOutOfBounds(index, length, buffer);
    if (buffer instanceof AbstractUnpooledSlicedByteBuf) {
      this.buffer = ((AbstractUnpooledSlicedByteBuf)buffer).buffer;
      ((AbstractUnpooledSlicedByteBuf)buffer).adjustment += index;
    } else if (buffer instanceof DuplicatedByteBuf) {
      this.buffer = buffer.unwrap();
      this.adjustment = index;
    } else {
      this.buffer = buffer;
      this.adjustment = index;
    } 
    initLength(length);
    writerIndex(length);
  }
  
  void initLength(int length) {}
  
  int length() {
    return capacity();
  }
  
  public ByteBuf unwrap() {
    return this.buffer;
  }
  
  public ByteBufAllocator alloc() {
    return unwrap().alloc();
  }
  
  @Deprecated
  public ByteOrder order() {
    return unwrap().order();
  }
  
  public boolean isDirect() {
    return unwrap().isDirect();
  }
  
  public ByteBuf capacity(int newCapacity) {
    throw new UnsupportedOperationException("sliced buffer");
  }
  
  public boolean hasArray() {
    return unwrap().hasArray();
  }
  
  public byte[] array() {
    return unwrap().array();
  }
  
  public int arrayOffset() {
    return idx(unwrap().arrayOffset());
  }
  
  public boolean hasMemoryAddress() {
    return unwrap().hasMemoryAddress();
  }
  
  public long memoryAddress() {
    return unwrap().memoryAddress() + this.adjustment;
  }
  
  public byte getByte(int index) {
    checkIndex0(index, 1);
    return unwrap().getByte(idx(index));
  }
  
  protected byte _getByte(int index) {
    return unwrap().getByte(idx(index));
  }
  
  public short getShort(int index) {
    checkIndex0(index, 2);
    return unwrap().getShort(idx(index));
  }
  
  protected short _getShort(int index) {
    return unwrap().getShort(idx(index));
  }
  
  public short getShortLE(int index) {
    checkIndex0(index, 2);
    return unwrap().getShortLE(idx(index));
  }
  
  protected short _getShortLE(int index) {
    return unwrap().getShortLE(idx(index));
  }
  
  public int getUnsignedMedium(int index) {
    checkIndex0(index, 3);
    return unwrap().getUnsignedMedium(idx(index));
  }
  
  protected int _getUnsignedMedium(int index) {
    return unwrap().getUnsignedMedium(idx(index));
  }
  
  public int getUnsignedMediumLE(int index) {
    checkIndex0(index, 3);
    return unwrap().getUnsignedMediumLE(idx(index));
  }
  
  protected int _getUnsignedMediumLE(int index) {
    return unwrap().getUnsignedMediumLE(idx(index));
  }
  
  public int getInt(int index) {
    checkIndex0(index, 4);
    return unwrap().getInt(idx(index));
  }
  
  protected int _getInt(int index) {
    return unwrap().getInt(idx(index));
  }
  
  public int getIntLE(int index) {
    checkIndex0(index, 4);
    return unwrap().getIntLE(idx(index));
  }
  
  protected int _getIntLE(int index) {
    return unwrap().getIntLE(idx(index));
  }
  
  public long getLong(int index) {
    checkIndex0(index, 8);
    return unwrap().getLong(idx(index));
  }
  
  protected long _getLong(int index) {
    return unwrap().getLong(idx(index));
  }
  
  public long getLongLE(int index) {
    checkIndex0(index, 8);
    return unwrap().getLongLE(idx(index));
  }
  
  protected long _getLongLE(int index) {
    return unwrap().getLongLE(idx(index));
  }
  
  public ByteBuf duplicate() {
    return unwrap().duplicate().setIndex(idx(readerIndex()), idx(writerIndex()));
  }
  
  public ByteBuf copy(int index, int length) {
    checkIndex0(index, length);
    return unwrap().copy(idx(index), length);
  }
  
  public ByteBuf slice(int index, int length) {
    checkIndex0(index, length);
    return unwrap().slice(idx(index), length);
  }
  
  public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
    checkIndex0(index, length);
    unwrap().getBytes(idx(index), dst, dstIndex, length);
    return this;
  }
  
  public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
    checkIndex0(index, length);
    unwrap().getBytes(idx(index), dst, dstIndex, length);
    return this;
  }
  
  public ByteBuf getBytes(int index, ByteBuffer dst) {
    checkIndex0(index, dst.remaining());
    unwrap().getBytes(idx(index), dst);
    return this;
  }
  
  public ByteBuf setByte(int index, int value) {
    checkIndex0(index, 1);
    unwrap().setByte(idx(index), value);
    return this;
  }
  
  public CharSequence getCharSequence(int index, int length, Charset charset) {
    checkIndex0(index, length);
    return unwrap().getCharSequence(idx(index), length, charset);
  }
  
  protected void _setByte(int index, int value) {
    unwrap().setByte(idx(index), value);
  }
  
  public ByteBuf setShort(int index, int value) {
    checkIndex0(index, 2);
    unwrap().setShort(idx(index), value);
    return this;
  }
  
  protected void _setShort(int index, int value) {
    unwrap().setShort(idx(index), value);
  }
  
  public ByteBuf setShortLE(int index, int value) {
    checkIndex0(index, 2);
    unwrap().setShortLE(idx(index), value);
    return this;
  }
  
  protected void _setShortLE(int index, int value) {
    unwrap().setShortLE(idx(index), value);
  }
  
  public ByteBuf setMedium(int index, int value) {
    checkIndex0(index, 3);
    unwrap().setMedium(idx(index), value);
    return this;
  }
  
  protected void _setMedium(int index, int value) {
    unwrap().setMedium(idx(index), value);
  }
  
  public ByteBuf setMediumLE(int index, int value) {
    checkIndex0(index, 3);
    unwrap().setMediumLE(idx(index), value);
    return this;
  }
  
  protected void _setMediumLE(int index, int value) {
    unwrap().setMediumLE(idx(index), value);
  }
  
  public ByteBuf setInt(int index, int value) {
    checkIndex0(index, 4);
    unwrap().setInt(idx(index), value);
    return this;
  }
  
  protected void _setInt(int index, int value) {
    unwrap().setInt(idx(index), value);
  }
  
  public ByteBuf setIntLE(int index, int value) {
    checkIndex0(index, 4);
    unwrap().setIntLE(idx(index), value);
    return this;
  }
  
  protected void _setIntLE(int index, int value) {
    unwrap().setIntLE(idx(index), value);
  }
  
  public ByteBuf setLong(int index, long value) {
    checkIndex0(index, 8);
    unwrap().setLong(idx(index), value);
    return this;
  }
  
  protected void _setLong(int index, long value) {
    unwrap().setLong(idx(index), value);
  }
  
  public ByteBuf setLongLE(int index, long value) {
    checkIndex0(index, 8);
    unwrap().setLongLE(idx(index), value);
    return this;
  }
  
  protected void _setLongLE(int index, long value) {
    unwrap().setLongLE(idx(index), value);
  }
  
  public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
    checkIndex0(index, length);
    unwrap().setBytes(idx(index), src, srcIndex, length);
    return this;
  }
  
  public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
    checkIndex0(index, length);
    unwrap().setBytes(idx(index), src, srcIndex, length);
    return this;
  }
  
  public ByteBuf setBytes(int index, ByteBuffer src) {
    checkIndex0(index, src.remaining());
    unwrap().setBytes(idx(index), src);
    return this;
  }
  
  public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
    checkIndex0(index, length);
    unwrap().getBytes(idx(index), out, length);
    return this;
  }
  
  public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
    checkIndex0(index, length);
    return unwrap().getBytes(idx(index), out, length);
  }
  
  public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
    checkIndex0(index, length);
    return unwrap().getBytes(idx(index), out, position, length);
  }
  
  public int setBytes(int index, InputStream in, int length) throws IOException {
    checkIndex0(index, length);
    return unwrap().setBytes(idx(index), in, length);
  }
  
  public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
    checkIndex0(index, length);
    return unwrap().setBytes(idx(index), in, length);
  }
  
  public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
    checkIndex0(index, length);
    return unwrap().setBytes(idx(index), in, position, length);
  }
  
  public int nioBufferCount() {
    return unwrap().nioBufferCount();
  }
  
  public ByteBuffer nioBuffer(int index, int length) {
    checkIndex0(index, length);
    return unwrap().nioBuffer(idx(index), length);
  }
  
  public ByteBuffer[] nioBuffers(int index, int length) {
    checkIndex0(index, length);
    return unwrap().nioBuffers(idx(index), length);
  }
  
  public int forEachByte(int index, int length, ByteProcessor processor) {
    checkIndex0(index, length);
    int ret = unwrap().forEachByte(idx(index), length, processor);
    if (ret >= this.adjustment)
      return ret - this.adjustment; 
    return -1;
  }
  
  public int forEachByteDesc(int index, int length, ByteProcessor processor) {
    checkIndex0(index, length);
    int ret = unwrap().forEachByteDesc(idx(index), length, processor);
    if (ret >= this.adjustment)
      return ret - this.adjustment; 
    return -1;
  }
  
  final int idx(int index) {
    return index + this.adjustment;
  }
  
  static void checkSliceOutOfBounds(int index, int length, ByteBuf buffer) {
    if (MathUtil.isOutOfBounds(index, length, buffer.capacity()))
      throw new IndexOutOfBoundsException(buffer + ".slice(" + index + ", " + length + ')'); 
  }
}

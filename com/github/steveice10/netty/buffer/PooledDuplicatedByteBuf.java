package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ByteProcessor;
import com.github.steveice10.netty.util.Recycler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

final class PooledDuplicatedByteBuf extends AbstractPooledDerivedByteBuf {
  private static final Recycler<PooledDuplicatedByteBuf> RECYCLER = new Recycler<PooledDuplicatedByteBuf>() {
      protected PooledDuplicatedByteBuf newObject(Recycler.Handle<PooledDuplicatedByteBuf> handle) {
        return new PooledDuplicatedByteBuf(handle);
      }
    };
  
  static PooledDuplicatedByteBuf newInstance(AbstractByteBuf unwrapped, ByteBuf wrapped, int readerIndex, int writerIndex) {
    PooledDuplicatedByteBuf duplicate = (PooledDuplicatedByteBuf)RECYCLER.get();
    duplicate.init(unwrapped, wrapped, readerIndex, writerIndex, unwrapped.maxCapacity());
    duplicate.markReaderIndex();
    duplicate.markWriterIndex();
    return duplicate;
  }
  
  private PooledDuplicatedByteBuf(Recycler.Handle<PooledDuplicatedByteBuf> handle) {
    super((Recycler.Handle)handle);
  }
  
  public int capacity() {
    return unwrap().capacity();
  }
  
  public ByteBuf capacity(int newCapacity) {
    unwrap().capacity(newCapacity);
    return this;
  }
  
  public int arrayOffset() {
    return unwrap().arrayOffset();
  }
  
  public long memoryAddress() {
    return unwrap().memoryAddress();
  }
  
  public ByteBuffer nioBuffer(int index, int length) {
    return unwrap().nioBuffer(index, length);
  }
  
  public ByteBuffer[] nioBuffers(int index, int length) {
    return unwrap().nioBuffers(index, length);
  }
  
  public ByteBuf copy(int index, int length) {
    return unwrap().copy(index, length);
  }
  
  public ByteBuf retainedSlice(int index, int length) {
    return PooledSlicedByteBuf.newInstance(unwrap(), this, index, length);
  }
  
  public ByteBuf duplicate() {
    return duplicate0().setIndex(readerIndex(), writerIndex());
  }
  
  public ByteBuf retainedDuplicate() {
    return newInstance(unwrap(), this, readerIndex(), writerIndex());
  }
  
  public byte getByte(int index) {
    return unwrap().getByte(index);
  }
  
  protected byte _getByte(int index) {
    return unwrap()._getByte(index);
  }
  
  public short getShort(int index) {
    return unwrap().getShort(index);
  }
  
  protected short _getShort(int index) {
    return unwrap()._getShort(index);
  }
  
  public short getShortLE(int index) {
    return unwrap().getShortLE(index);
  }
  
  protected short _getShortLE(int index) {
    return unwrap()._getShortLE(index);
  }
  
  public int getUnsignedMedium(int index) {
    return unwrap().getUnsignedMedium(index);
  }
  
  protected int _getUnsignedMedium(int index) {
    return unwrap()._getUnsignedMedium(index);
  }
  
  public int getUnsignedMediumLE(int index) {
    return unwrap().getUnsignedMediumLE(index);
  }
  
  protected int _getUnsignedMediumLE(int index) {
    return unwrap()._getUnsignedMediumLE(index);
  }
  
  public int getInt(int index) {
    return unwrap().getInt(index);
  }
  
  protected int _getInt(int index) {
    return unwrap()._getInt(index);
  }
  
  public int getIntLE(int index) {
    return unwrap().getIntLE(index);
  }
  
  protected int _getIntLE(int index) {
    return unwrap()._getIntLE(index);
  }
  
  public long getLong(int index) {
    return unwrap().getLong(index);
  }
  
  protected long _getLong(int index) {
    return unwrap()._getLong(index);
  }
  
  public long getLongLE(int index) {
    return unwrap().getLongLE(index);
  }
  
  protected long _getLongLE(int index) {
    return unwrap()._getLongLE(index);
  }
  
  public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
    unwrap().getBytes(index, dst, dstIndex, length);
    return this;
  }
  
  public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
    unwrap().getBytes(index, dst, dstIndex, length);
    return this;
  }
  
  public ByteBuf getBytes(int index, ByteBuffer dst) {
    unwrap().getBytes(index, dst);
    return this;
  }
  
  public ByteBuf setByte(int index, int value) {
    unwrap().setByte(index, value);
    return this;
  }
  
  protected void _setByte(int index, int value) {
    unwrap()._setByte(index, value);
  }
  
  public ByteBuf setShort(int index, int value) {
    unwrap().setShort(index, value);
    return this;
  }
  
  protected void _setShort(int index, int value) {
    unwrap()._setShort(index, value);
  }
  
  public ByteBuf setShortLE(int index, int value) {
    unwrap().setShortLE(index, value);
    return this;
  }
  
  protected void _setShortLE(int index, int value) {
    unwrap()._setShortLE(index, value);
  }
  
  public ByteBuf setMedium(int index, int value) {
    unwrap().setMedium(index, value);
    return this;
  }
  
  protected void _setMedium(int index, int value) {
    unwrap()._setMedium(index, value);
  }
  
  public ByteBuf setMediumLE(int index, int value) {
    unwrap().setMediumLE(index, value);
    return this;
  }
  
  protected void _setMediumLE(int index, int value) {
    unwrap()._setMediumLE(index, value);
  }
  
  public ByteBuf setInt(int index, int value) {
    unwrap().setInt(index, value);
    return this;
  }
  
  protected void _setInt(int index, int value) {
    unwrap()._setInt(index, value);
  }
  
  public ByteBuf setIntLE(int index, int value) {
    unwrap().setIntLE(index, value);
    return this;
  }
  
  protected void _setIntLE(int index, int value) {
    unwrap()._setIntLE(index, value);
  }
  
  public ByteBuf setLong(int index, long value) {
    unwrap().setLong(index, value);
    return this;
  }
  
  protected void _setLong(int index, long value) {
    unwrap()._setLong(index, value);
  }
  
  public ByteBuf setLongLE(int index, long value) {
    unwrap().setLongLE(index, value);
    return this;
  }
  
  protected void _setLongLE(int index, long value) {
    unwrap().setLongLE(index, value);
  }
  
  public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
    unwrap().setBytes(index, src, srcIndex, length);
    return this;
  }
  
  public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
    unwrap().setBytes(index, src, srcIndex, length);
    return this;
  }
  
  public ByteBuf setBytes(int index, ByteBuffer src) {
    unwrap().setBytes(index, src);
    return this;
  }
  
  public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
    unwrap().getBytes(index, out, length);
    return this;
  }
  
  public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
    return unwrap().getBytes(index, out, length);
  }
  
  public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
    return unwrap().getBytes(index, out, position, length);
  }
  
  public int setBytes(int index, InputStream in, int length) throws IOException {
    return unwrap().setBytes(index, in, length);
  }
  
  public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
    return unwrap().setBytes(index, in, length);
  }
  
  public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
    return unwrap().setBytes(index, in, position, length);
  }
  
  public int forEachByte(int index, int length, ByteProcessor processor) {
    return unwrap().forEachByte(index, length, processor);
  }
  
  public int forEachByteDesc(int index, int length, ByteProcessor processor) {
    return unwrap().forEachByteDesc(index, length, processor);
  }
}

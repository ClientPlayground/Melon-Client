package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ByteProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

@Deprecated
public class ReadOnlyByteBuf extends AbstractDerivedByteBuf {
  private final ByteBuf buffer;
  
  public ReadOnlyByteBuf(ByteBuf buffer) {
    super(buffer.maxCapacity());
    if (buffer instanceof ReadOnlyByteBuf || buffer instanceof DuplicatedByteBuf) {
      this.buffer = buffer.unwrap();
    } else {
      this.buffer = buffer;
    } 
    setIndex(buffer.readerIndex(), buffer.writerIndex());
  }
  
  public boolean isReadOnly() {
    return true;
  }
  
  public boolean isWritable() {
    return false;
  }
  
  public boolean isWritable(int numBytes) {
    return false;
  }
  
  public int ensureWritable(int minWritableBytes, boolean force) {
    return 1;
  }
  
  public ByteBuf ensureWritable(int minWritableBytes) {
    throw new ReadOnlyBufferException();
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
  
  public boolean hasArray() {
    return false;
  }
  
  public byte[] array() {
    throw new ReadOnlyBufferException();
  }
  
  public int arrayOffset() {
    throw new ReadOnlyBufferException();
  }
  
  public boolean hasMemoryAddress() {
    return unwrap().hasMemoryAddress();
  }
  
  public long memoryAddress() {
    return unwrap().memoryAddress();
  }
  
  public ByteBuf discardReadBytes() {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setBytes(int index, ByteBuffer src) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setByte(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  protected void _setByte(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setShort(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  protected void _setShort(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setShortLE(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  protected void _setShortLE(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setMedium(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  protected void _setMedium(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setMediumLE(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  protected void _setMediumLE(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setInt(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  protected void _setInt(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setIntLE(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  protected void _setIntLE(int index, int value) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setLong(int index, long value) {
    throw new ReadOnlyBufferException();
  }
  
  protected void _setLong(int index, long value) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf setLongLE(int index, long value) {
    throw new ReadOnlyBufferException();
  }
  
  protected void _setLongLE(int index, long value) {
    throw new ReadOnlyBufferException();
  }
  
  public int setBytes(int index, InputStream in, int length) {
    throw new ReadOnlyBufferException();
  }
  
  public int setBytes(int index, ScatteringByteChannel in, int length) {
    throw new ReadOnlyBufferException();
  }
  
  public int setBytes(int index, FileChannel in, long position, int length) {
    throw new ReadOnlyBufferException();
  }
  
  public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
    return unwrap().getBytes(index, out, length);
  }
  
  public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
    return unwrap().getBytes(index, out, position, length);
  }
  
  public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
    unwrap().getBytes(index, out, length);
    return this;
  }
  
  public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
    unwrap().getBytes(index, dst, dstIndex, length);
    return this;
  }
  
  public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
    unwrap().getBytes(index, dst, dstIndex, length);
    return this;
  }
  
  public ByteBuf getBytes(int index, ByteBuffer dst) {
    unwrap().getBytes(index, dst);
    return this;
  }
  
  public ByteBuf duplicate() {
    return new ReadOnlyByteBuf(this);
  }
  
  public ByteBuf copy(int index, int length) {
    return unwrap().copy(index, length);
  }
  
  public ByteBuf slice(int index, int length) {
    return Unpooled.unmodifiableBuffer(unwrap().slice(index, length));
  }
  
  public byte getByte(int index) {
    return unwrap().getByte(index);
  }
  
  protected byte _getByte(int index) {
    return unwrap().getByte(index);
  }
  
  public short getShort(int index) {
    return unwrap().getShort(index);
  }
  
  protected short _getShort(int index) {
    return unwrap().getShort(index);
  }
  
  public short getShortLE(int index) {
    return unwrap().getShortLE(index);
  }
  
  protected short _getShortLE(int index) {
    return unwrap().getShortLE(index);
  }
  
  public int getUnsignedMedium(int index) {
    return unwrap().getUnsignedMedium(index);
  }
  
  protected int _getUnsignedMedium(int index) {
    return unwrap().getUnsignedMedium(index);
  }
  
  public int getUnsignedMediumLE(int index) {
    return unwrap().getUnsignedMediumLE(index);
  }
  
  protected int _getUnsignedMediumLE(int index) {
    return unwrap().getUnsignedMediumLE(index);
  }
  
  public int getInt(int index) {
    return unwrap().getInt(index);
  }
  
  protected int _getInt(int index) {
    return unwrap().getInt(index);
  }
  
  public int getIntLE(int index) {
    return unwrap().getIntLE(index);
  }
  
  protected int _getIntLE(int index) {
    return unwrap().getIntLE(index);
  }
  
  public long getLong(int index) {
    return unwrap().getLong(index);
  }
  
  protected long _getLong(int index) {
    return unwrap().getLong(index);
  }
  
  public long getLongLE(int index) {
    return unwrap().getLongLE(index);
  }
  
  protected long _getLongLE(int index) {
    return unwrap().getLongLE(index);
  }
  
  public int nioBufferCount() {
    return unwrap().nioBufferCount();
  }
  
  public ByteBuffer nioBuffer(int index, int length) {
    return unwrap().nioBuffer(index, length).asReadOnlyBuffer();
  }
  
  public ByteBuffer[] nioBuffers(int index, int length) {
    return unwrap().nioBuffers(index, length);
  }
  
  public int forEachByte(int index, int length, ByteProcessor processor) {
    return unwrap().forEachByte(index, length, processor);
  }
  
  public int forEachByteDesc(int index, int length, ByteProcessor processor) {
    return unwrap().forEachByteDesc(index, length, processor);
  }
  
  public int capacity() {
    return unwrap().capacity();
  }
  
  public ByteBuf capacity(int newCapacity) {
    throw new ReadOnlyBufferException();
  }
  
  public ByteBuf asReadOnly() {
    return this;
  }
}

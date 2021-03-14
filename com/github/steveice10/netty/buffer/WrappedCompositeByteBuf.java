package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ByteProcessor;
import com.github.steveice10.netty.util.ReferenceCounted;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

class WrappedCompositeByteBuf extends CompositeByteBuf {
  private final CompositeByteBuf wrapped;
  
  WrappedCompositeByteBuf(CompositeByteBuf wrapped) {
    super(wrapped.alloc());
    this.wrapped = wrapped;
  }
  
  public boolean release() {
    return this.wrapped.release();
  }
  
  public boolean release(int decrement) {
    return this.wrapped.release(decrement);
  }
  
  public final int maxCapacity() {
    return this.wrapped.maxCapacity();
  }
  
  public final int readerIndex() {
    return this.wrapped.readerIndex();
  }
  
  public final int writerIndex() {
    return this.wrapped.writerIndex();
  }
  
  public final boolean isReadable() {
    return this.wrapped.isReadable();
  }
  
  public final boolean isReadable(int numBytes) {
    return this.wrapped.isReadable(numBytes);
  }
  
  public final boolean isWritable() {
    return this.wrapped.isWritable();
  }
  
  public final boolean isWritable(int numBytes) {
    return this.wrapped.isWritable(numBytes);
  }
  
  public final int readableBytes() {
    return this.wrapped.readableBytes();
  }
  
  public final int writableBytes() {
    return this.wrapped.writableBytes();
  }
  
  public final int maxWritableBytes() {
    return this.wrapped.maxWritableBytes();
  }
  
  public int ensureWritable(int minWritableBytes, boolean force) {
    return this.wrapped.ensureWritable(minWritableBytes, force);
  }
  
  public ByteBuf order(ByteOrder endianness) {
    return this.wrapped.order(endianness);
  }
  
  public boolean getBoolean(int index) {
    return this.wrapped.getBoolean(index);
  }
  
  public short getUnsignedByte(int index) {
    return this.wrapped.getUnsignedByte(index);
  }
  
  public short getShort(int index) {
    return this.wrapped.getShort(index);
  }
  
  public short getShortLE(int index) {
    return this.wrapped.getShortLE(index);
  }
  
  public int getUnsignedShort(int index) {
    return this.wrapped.getUnsignedShort(index);
  }
  
  public int getUnsignedShortLE(int index) {
    return this.wrapped.getUnsignedShortLE(index);
  }
  
  public int getUnsignedMedium(int index) {
    return this.wrapped.getUnsignedMedium(index);
  }
  
  public int getUnsignedMediumLE(int index) {
    return this.wrapped.getUnsignedMediumLE(index);
  }
  
  public int getMedium(int index) {
    return this.wrapped.getMedium(index);
  }
  
  public int getMediumLE(int index) {
    return this.wrapped.getMediumLE(index);
  }
  
  public int getInt(int index) {
    return this.wrapped.getInt(index);
  }
  
  public int getIntLE(int index) {
    return this.wrapped.getIntLE(index);
  }
  
  public long getUnsignedInt(int index) {
    return this.wrapped.getUnsignedInt(index);
  }
  
  public long getUnsignedIntLE(int index) {
    return this.wrapped.getUnsignedIntLE(index);
  }
  
  public long getLong(int index) {
    return this.wrapped.getLong(index);
  }
  
  public long getLongLE(int index) {
    return this.wrapped.getLongLE(index);
  }
  
  public char getChar(int index) {
    return this.wrapped.getChar(index);
  }
  
  public float getFloat(int index) {
    return this.wrapped.getFloat(index);
  }
  
  public double getDouble(int index) {
    return this.wrapped.getDouble(index);
  }
  
  public ByteBuf setShortLE(int index, int value) {
    return this.wrapped.setShortLE(index, value);
  }
  
  public ByteBuf setMediumLE(int index, int value) {
    return this.wrapped.setMediumLE(index, value);
  }
  
  public ByteBuf setIntLE(int index, int value) {
    return this.wrapped.setIntLE(index, value);
  }
  
  public ByteBuf setLongLE(int index, long value) {
    return this.wrapped.setLongLE(index, value);
  }
  
  public byte readByte() {
    return this.wrapped.readByte();
  }
  
  public boolean readBoolean() {
    return this.wrapped.readBoolean();
  }
  
  public short readUnsignedByte() {
    return this.wrapped.readUnsignedByte();
  }
  
  public short readShort() {
    return this.wrapped.readShort();
  }
  
  public short readShortLE() {
    return this.wrapped.readShortLE();
  }
  
  public int readUnsignedShort() {
    return this.wrapped.readUnsignedShort();
  }
  
  public int readUnsignedShortLE() {
    return this.wrapped.readUnsignedShortLE();
  }
  
  public int readMedium() {
    return this.wrapped.readMedium();
  }
  
  public int readMediumLE() {
    return this.wrapped.readMediumLE();
  }
  
  public int readUnsignedMedium() {
    return this.wrapped.readUnsignedMedium();
  }
  
  public int readUnsignedMediumLE() {
    return this.wrapped.readUnsignedMediumLE();
  }
  
  public int readInt() {
    return this.wrapped.readInt();
  }
  
  public int readIntLE() {
    return this.wrapped.readIntLE();
  }
  
  public long readUnsignedInt() {
    return this.wrapped.readUnsignedInt();
  }
  
  public long readUnsignedIntLE() {
    return this.wrapped.readUnsignedIntLE();
  }
  
  public long readLong() {
    return this.wrapped.readLong();
  }
  
  public long readLongLE() {
    return this.wrapped.readLongLE();
  }
  
  public char readChar() {
    return this.wrapped.readChar();
  }
  
  public float readFloat() {
    return this.wrapped.readFloat();
  }
  
  public double readDouble() {
    return this.wrapped.readDouble();
  }
  
  public ByteBuf readBytes(int length) {
    return this.wrapped.readBytes(length);
  }
  
  public ByteBuf slice() {
    return this.wrapped.slice();
  }
  
  public ByteBuf retainedSlice() {
    return this.wrapped.retainedSlice();
  }
  
  public ByteBuf slice(int index, int length) {
    return this.wrapped.slice(index, length);
  }
  
  public ByteBuf retainedSlice(int index, int length) {
    return this.wrapped.retainedSlice(index, length);
  }
  
  public ByteBuffer nioBuffer() {
    return this.wrapped.nioBuffer();
  }
  
  public String toString(Charset charset) {
    return this.wrapped.toString(charset);
  }
  
  public String toString(int index, int length, Charset charset) {
    return this.wrapped.toString(index, length, charset);
  }
  
  public int indexOf(int fromIndex, int toIndex, byte value) {
    return this.wrapped.indexOf(fromIndex, toIndex, value);
  }
  
  public int bytesBefore(byte value) {
    return this.wrapped.bytesBefore(value);
  }
  
  public int bytesBefore(int length, byte value) {
    return this.wrapped.bytesBefore(length, value);
  }
  
  public int bytesBefore(int index, int length, byte value) {
    return this.wrapped.bytesBefore(index, length, value);
  }
  
  public int forEachByte(ByteProcessor processor) {
    return this.wrapped.forEachByte(processor);
  }
  
  public int forEachByte(int index, int length, ByteProcessor processor) {
    return this.wrapped.forEachByte(index, length, processor);
  }
  
  public int forEachByteDesc(ByteProcessor processor) {
    return this.wrapped.forEachByteDesc(processor);
  }
  
  public int forEachByteDesc(int index, int length, ByteProcessor processor) {
    return this.wrapped.forEachByteDesc(index, length, processor);
  }
  
  public final int hashCode() {
    return this.wrapped.hashCode();
  }
  
  public final boolean equals(Object o) {
    return this.wrapped.equals(o);
  }
  
  public final int compareTo(ByteBuf that) {
    return this.wrapped.compareTo(that);
  }
  
  public final int refCnt() {
    return this.wrapped.refCnt();
  }
  
  public ByteBuf duplicate() {
    return this.wrapped.duplicate();
  }
  
  public ByteBuf retainedDuplicate() {
    return this.wrapped.retainedDuplicate();
  }
  
  public ByteBuf readSlice(int length) {
    return this.wrapped.readSlice(length);
  }
  
  public ByteBuf readRetainedSlice(int length) {
    return this.wrapped.readRetainedSlice(length);
  }
  
  public int readBytes(GatheringByteChannel out, int length) throws IOException {
    return this.wrapped.readBytes(out, length);
  }
  
  public ByteBuf writeShortLE(int value) {
    return this.wrapped.writeShortLE(value);
  }
  
  public ByteBuf writeMediumLE(int value) {
    return this.wrapped.writeMediumLE(value);
  }
  
  public ByteBuf writeIntLE(int value) {
    return this.wrapped.writeIntLE(value);
  }
  
  public ByteBuf writeLongLE(long value) {
    return this.wrapped.writeLongLE(value);
  }
  
  public int writeBytes(InputStream in, int length) throws IOException {
    return this.wrapped.writeBytes(in, length);
  }
  
  public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
    return this.wrapped.writeBytes(in, length);
  }
  
  public ByteBuf copy() {
    return this.wrapped.copy();
  }
  
  public CompositeByteBuf addComponent(ByteBuf buffer) {
    this.wrapped.addComponent(buffer);
    return this;
  }
  
  public CompositeByteBuf addComponents(ByteBuf... buffers) {
    this.wrapped.addComponents(buffers);
    return this;
  }
  
  public CompositeByteBuf addComponents(Iterable<ByteBuf> buffers) {
    this.wrapped.addComponents(buffers);
    return this;
  }
  
  public CompositeByteBuf addComponent(int cIndex, ByteBuf buffer) {
    this.wrapped.addComponent(cIndex, buffer);
    return this;
  }
  
  public CompositeByteBuf addComponents(int cIndex, ByteBuf... buffers) {
    this.wrapped.addComponents(cIndex, buffers);
    return this;
  }
  
  public CompositeByteBuf addComponents(int cIndex, Iterable<ByteBuf> buffers) {
    this.wrapped.addComponents(cIndex, buffers);
    return this;
  }
  
  public CompositeByteBuf addComponent(boolean increaseWriterIndex, ByteBuf buffer) {
    this.wrapped.addComponent(increaseWriterIndex, buffer);
    return this;
  }
  
  public CompositeByteBuf addComponents(boolean increaseWriterIndex, ByteBuf... buffers) {
    this.wrapped.addComponents(increaseWriterIndex, buffers);
    return this;
  }
  
  public CompositeByteBuf addComponents(boolean increaseWriterIndex, Iterable<ByteBuf> buffers) {
    this.wrapped.addComponents(increaseWriterIndex, buffers);
    return this;
  }
  
  public CompositeByteBuf addComponent(boolean increaseWriterIndex, int cIndex, ByteBuf buffer) {
    this.wrapped.addComponent(increaseWriterIndex, cIndex, buffer);
    return this;
  }
  
  public CompositeByteBuf removeComponent(int cIndex) {
    this.wrapped.removeComponent(cIndex);
    return this;
  }
  
  public CompositeByteBuf removeComponents(int cIndex, int numComponents) {
    this.wrapped.removeComponents(cIndex, numComponents);
    return this;
  }
  
  public Iterator<ByteBuf> iterator() {
    return this.wrapped.iterator();
  }
  
  public List<ByteBuf> decompose(int offset, int length) {
    return this.wrapped.decompose(offset, length);
  }
  
  public final boolean isDirect() {
    return this.wrapped.isDirect();
  }
  
  public final boolean hasArray() {
    return this.wrapped.hasArray();
  }
  
  public final byte[] array() {
    return this.wrapped.array();
  }
  
  public final int arrayOffset() {
    return this.wrapped.arrayOffset();
  }
  
  public final boolean hasMemoryAddress() {
    return this.wrapped.hasMemoryAddress();
  }
  
  public final long memoryAddress() {
    return this.wrapped.memoryAddress();
  }
  
  public final int capacity() {
    return this.wrapped.capacity();
  }
  
  public CompositeByteBuf capacity(int newCapacity) {
    this.wrapped.capacity(newCapacity);
    return this;
  }
  
  public final ByteBufAllocator alloc() {
    return this.wrapped.alloc();
  }
  
  public final ByteOrder order() {
    return this.wrapped.order();
  }
  
  public final int numComponents() {
    return this.wrapped.numComponents();
  }
  
  public final int maxNumComponents() {
    return this.wrapped.maxNumComponents();
  }
  
  public final int toComponentIndex(int offset) {
    return this.wrapped.toComponentIndex(offset);
  }
  
  public final int toByteIndex(int cIndex) {
    return this.wrapped.toByteIndex(cIndex);
  }
  
  public byte getByte(int index) {
    return this.wrapped.getByte(index);
  }
  
  protected final byte _getByte(int index) {
    return this.wrapped._getByte(index);
  }
  
  protected final short _getShort(int index) {
    return this.wrapped._getShort(index);
  }
  
  protected final short _getShortLE(int index) {
    return this.wrapped._getShortLE(index);
  }
  
  protected final int _getUnsignedMedium(int index) {
    return this.wrapped._getUnsignedMedium(index);
  }
  
  protected final int _getUnsignedMediumLE(int index) {
    return this.wrapped._getUnsignedMediumLE(index);
  }
  
  protected final int _getInt(int index) {
    return this.wrapped._getInt(index);
  }
  
  protected final int _getIntLE(int index) {
    return this.wrapped._getIntLE(index);
  }
  
  protected final long _getLong(int index) {
    return this.wrapped._getLong(index);
  }
  
  protected final long _getLongLE(int index) {
    return this.wrapped._getLongLE(index);
  }
  
  public CompositeByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
    this.wrapped.getBytes(index, dst, dstIndex, length);
    return this;
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuffer dst) {
    this.wrapped.getBytes(index, dst);
    return this;
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
    this.wrapped.getBytes(index, dst, dstIndex, length);
    return this;
  }
  
  public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
    return this.wrapped.getBytes(index, out, length);
  }
  
  public CompositeByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
    this.wrapped.getBytes(index, out, length);
    return this;
  }
  
  public CompositeByteBuf setByte(int index, int value) {
    this.wrapped.setByte(index, value);
    return this;
  }
  
  protected final void _setByte(int index, int value) {
    this.wrapped._setByte(index, value);
  }
  
  public CompositeByteBuf setShort(int index, int value) {
    this.wrapped.setShort(index, value);
    return this;
  }
  
  protected final void _setShort(int index, int value) {
    this.wrapped._setShort(index, value);
  }
  
  protected final void _setShortLE(int index, int value) {
    this.wrapped._setShortLE(index, value);
  }
  
  public CompositeByteBuf setMedium(int index, int value) {
    this.wrapped.setMedium(index, value);
    return this;
  }
  
  protected final void _setMedium(int index, int value) {
    this.wrapped._setMedium(index, value);
  }
  
  protected final void _setMediumLE(int index, int value) {
    this.wrapped._setMediumLE(index, value);
  }
  
  public CompositeByteBuf setInt(int index, int value) {
    this.wrapped.setInt(index, value);
    return this;
  }
  
  protected final void _setInt(int index, int value) {
    this.wrapped._setInt(index, value);
  }
  
  protected final void _setIntLE(int index, int value) {
    this.wrapped._setIntLE(index, value);
  }
  
  public CompositeByteBuf setLong(int index, long value) {
    this.wrapped.setLong(index, value);
    return this;
  }
  
  protected final void _setLong(int index, long value) {
    this.wrapped._setLong(index, value);
  }
  
  protected final void _setLongLE(int index, long value) {
    this.wrapped._setLongLE(index, value);
  }
  
  public CompositeByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
    this.wrapped.setBytes(index, src, srcIndex, length);
    return this;
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuffer src) {
    this.wrapped.setBytes(index, src);
    return this;
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
    this.wrapped.setBytes(index, src, srcIndex, length);
    return this;
  }
  
  public int setBytes(int index, InputStream in, int length) throws IOException {
    return this.wrapped.setBytes(index, in, length);
  }
  
  public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
    return this.wrapped.setBytes(index, in, length);
  }
  
  public ByteBuf copy(int index, int length) {
    return this.wrapped.copy(index, length);
  }
  
  public final ByteBuf component(int cIndex) {
    return this.wrapped.component(cIndex);
  }
  
  public final ByteBuf componentAtOffset(int offset) {
    return this.wrapped.componentAtOffset(offset);
  }
  
  public final ByteBuf internalComponent(int cIndex) {
    return this.wrapped.internalComponent(cIndex);
  }
  
  public final ByteBuf internalComponentAtOffset(int offset) {
    return this.wrapped.internalComponentAtOffset(offset);
  }
  
  public int nioBufferCount() {
    return this.wrapped.nioBufferCount();
  }
  
  public ByteBuffer internalNioBuffer(int index, int length) {
    return this.wrapped.internalNioBuffer(index, length);
  }
  
  public ByteBuffer nioBuffer(int index, int length) {
    return this.wrapped.nioBuffer(index, length);
  }
  
  public ByteBuffer[] nioBuffers(int index, int length) {
    return this.wrapped.nioBuffers(index, length);
  }
  
  public CompositeByteBuf consolidate() {
    this.wrapped.consolidate();
    return this;
  }
  
  public CompositeByteBuf consolidate(int cIndex, int numComponents) {
    this.wrapped.consolidate(cIndex, numComponents);
    return this;
  }
  
  public CompositeByteBuf discardReadComponents() {
    this.wrapped.discardReadComponents();
    return this;
  }
  
  public CompositeByteBuf discardReadBytes() {
    this.wrapped.discardReadBytes();
    return this;
  }
  
  public final String toString() {
    return this.wrapped.toString();
  }
  
  public final CompositeByteBuf readerIndex(int readerIndex) {
    this.wrapped.readerIndex(readerIndex);
    return this;
  }
  
  public final CompositeByteBuf writerIndex(int writerIndex) {
    this.wrapped.writerIndex(writerIndex);
    return this;
  }
  
  public final CompositeByteBuf setIndex(int readerIndex, int writerIndex) {
    this.wrapped.setIndex(readerIndex, writerIndex);
    return this;
  }
  
  public final CompositeByteBuf clear() {
    this.wrapped.clear();
    return this;
  }
  
  public final CompositeByteBuf markReaderIndex() {
    this.wrapped.markReaderIndex();
    return this;
  }
  
  public final CompositeByteBuf resetReaderIndex() {
    this.wrapped.resetReaderIndex();
    return this;
  }
  
  public final CompositeByteBuf markWriterIndex() {
    this.wrapped.markWriterIndex();
    return this;
  }
  
  public final CompositeByteBuf resetWriterIndex() {
    this.wrapped.resetWriterIndex();
    return this;
  }
  
  public CompositeByteBuf ensureWritable(int minWritableBytes) {
    this.wrapped.ensureWritable(minWritableBytes);
    return this;
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuf dst) {
    this.wrapped.getBytes(index, dst);
    return this;
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuf dst, int length) {
    this.wrapped.getBytes(index, dst, length);
    return this;
  }
  
  public CompositeByteBuf getBytes(int index, byte[] dst) {
    this.wrapped.getBytes(index, dst);
    return this;
  }
  
  public CompositeByteBuf setBoolean(int index, boolean value) {
    this.wrapped.setBoolean(index, value);
    return this;
  }
  
  public CompositeByteBuf setChar(int index, int value) {
    this.wrapped.setChar(index, value);
    return this;
  }
  
  public CompositeByteBuf setFloat(int index, float value) {
    this.wrapped.setFloat(index, value);
    return this;
  }
  
  public CompositeByteBuf setDouble(int index, double value) {
    this.wrapped.setDouble(index, value);
    return this;
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuf src) {
    this.wrapped.setBytes(index, src);
    return this;
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuf src, int length) {
    this.wrapped.setBytes(index, src, length);
    return this;
  }
  
  public CompositeByteBuf setBytes(int index, byte[] src) {
    this.wrapped.setBytes(index, src);
    return this;
  }
  
  public CompositeByteBuf setZero(int index, int length) {
    this.wrapped.setZero(index, length);
    return this;
  }
  
  public CompositeByteBuf readBytes(ByteBuf dst) {
    this.wrapped.readBytes(dst);
    return this;
  }
  
  public CompositeByteBuf readBytes(ByteBuf dst, int length) {
    this.wrapped.readBytes(dst, length);
    return this;
  }
  
  public CompositeByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
    this.wrapped.readBytes(dst, dstIndex, length);
    return this;
  }
  
  public CompositeByteBuf readBytes(byte[] dst) {
    this.wrapped.readBytes(dst);
    return this;
  }
  
  public CompositeByteBuf readBytes(byte[] dst, int dstIndex, int length) {
    this.wrapped.readBytes(dst, dstIndex, length);
    return this;
  }
  
  public CompositeByteBuf readBytes(ByteBuffer dst) {
    this.wrapped.readBytes(dst);
    return this;
  }
  
  public CompositeByteBuf readBytes(OutputStream out, int length) throws IOException {
    this.wrapped.readBytes(out, length);
    return this;
  }
  
  public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
    return this.wrapped.getBytes(index, out, position, length);
  }
  
  public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
    return this.wrapped.setBytes(index, in, position, length);
  }
  
  public boolean isReadOnly() {
    return this.wrapped.isReadOnly();
  }
  
  public ByteBuf asReadOnly() {
    return this.wrapped.asReadOnly();
  }
  
  protected SwappedByteBuf newSwappedByteBuf() {
    return this.wrapped.newSwappedByteBuf();
  }
  
  public CharSequence getCharSequence(int index, int length, Charset charset) {
    return this.wrapped.getCharSequence(index, length, charset);
  }
  
  public CharSequence readCharSequence(int length, Charset charset) {
    return this.wrapped.readCharSequence(length, charset);
  }
  
  public int setCharSequence(int index, CharSequence sequence, Charset charset) {
    return this.wrapped.setCharSequence(index, sequence, charset);
  }
  
  public int readBytes(FileChannel out, long position, int length) throws IOException {
    return this.wrapped.readBytes(out, position, length);
  }
  
  public int writeBytes(FileChannel in, long position, int length) throws IOException {
    return this.wrapped.writeBytes(in, position, length);
  }
  
  public int writeCharSequence(CharSequence sequence, Charset charset) {
    return this.wrapped.writeCharSequence(sequence, charset);
  }
  
  public CompositeByteBuf skipBytes(int length) {
    this.wrapped.skipBytes(length);
    return this;
  }
  
  public CompositeByteBuf writeBoolean(boolean value) {
    this.wrapped.writeBoolean(value);
    return this;
  }
  
  public CompositeByteBuf writeByte(int value) {
    this.wrapped.writeByte(value);
    return this;
  }
  
  public CompositeByteBuf writeShort(int value) {
    this.wrapped.writeShort(value);
    return this;
  }
  
  public CompositeByteBuf writeMedium(int value) {
    this.wrapped.writeMedium(value);
    return this;
  }
  
  public CompositeByteBuf writeInt(int value) {
    this.wrapped.writeInt(value);
    return this;
  }
  
  public CompositeByteBuf writeLong(long value) {
    this.wrapped.writeLong(value);
    return this;
  }
  
  public CompositeByteBuf writeChar(int value) {
    this.wrapped.writeChar(value);
    return this;
  }
  
  public CompositeByteBuf writeFloat(float value) {
    this.wrapped.writeFloat(value);
    return this;
  }
  
  public CompositeByteBuf writeDouble(double value) {
    this.wrapped.writeDouble(value);
    return this;
  }
  
  public CompositeByteBuf writeBytes(ByteBuf src) {
    this.wrapped.writeBytes(src);
    return this;
  }
  
  public CompositeByteBuf writeBytes(ByteBuf src, int length) {
    this.wrapped.writeBytes(src, length);
    return this;
  }
  
  public CompositeByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
    this.wrapped.writeBytes(src, srcIndex, length);
    return this;
  }
  
  public CompositeByteBuf writeBytes(byte[] src) {
    this.wrapped.writeBytes(src);
    return this;
  }
  
  public CompositeByteBuf writeBytes(byte[] src, int srcIndex, int length) {
    this.wrapped.writeBytes(src, srcIndex, length);
    return this;
  }
  
  public CompositeByteBuf writeBytes(ByteBuffer src) {
    this.wrapped.writeBytes(src);
    return this;
  }
  
  public CompositeByteBuf writeZero(int length) {
    this.wrapped.writeZero(length);
    return this;
  }
  
  public CompositeByteBuf retain(int increment) {
    this.wrapped.retain(increment);
    return this;
  }
  
  public CompositeByteBuf retain() {
    this.wrapped.retain();
    return this;
  }
  
  public CompositeByteBuf touch() {
    this.wrapped.touch();
    return this;
  }
  
  public CompositeByteBuf touch(Object hint) {
    this.wrapped.touch(hint);
    return this;
  }
  
  public ByteBuffer[] nioBuffers() {
    return this.wrapped.nioBuffers();
  }
  
  public CompositeByteBuf discardSomeReadBytes() {
    this.wrapped.discardSomeReadBytes();
    return this;
  }
  
  public final void deallocate() {
    this.wrapped.deallocate();
  }
  
  public final ByteBuf unwrap() {
    return this.wrapped;
  }
}

package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ByteProcessor;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.ResourceLeakDetector;
import com.github.steveice10.netty.util.ResourceLeakTracker;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

final class AdvancedLeakAwareByteBuf extends SimpleLeakAwareByteBuf {
  private static final String PROP_ACQUIRE_AND_RELEASE_ONLY = "com.github.steveice10.netty.leakDetection.acquireAndReleaseOnly";
  
  private static final boolean ACQUIRE_AND_RELEASE_ONLY;
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AdvancedLeakAwareByteBuf.class);
  
  static {
    ACQUIRE_AND_RELEASE_ONLY = SystemPropertyUtil.getBoolean("com.github.steveice10.netty.leakDetection.acquireAndReleaseOnly", false);
    if (logger.isDebugEnabled())
      logger.debug("-D{}: {}", "com.github.steveice10.netty.leakDetection.acquireAndReleaseOnly", Boolean.valueOf(ACQUIRE_AND_RELEASE_ONLY)); 
    ResourceLeakDetector.addExclusions(AdvancedLeakAwareByteBuf.class, new String[] { "touch", "recordLeakNonRefCountingOperation" });
  }
  
  AdvancedLeakAwareByteBuf(ByteBuf buf, ResourceLeakTracker<ByteBuf> leak) {
    super(buf, leak);
  }
  
  AdvancedLeakAwareByteBuf(ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leak) {
    super(wrapped, trackedByteBuf, leak);
  }
  
  static void recordLeakNonRefCountingOperation(ResourceLeakTracker<ByteBuf> leak) {
    if (!ACQUIRE_AND_RELEASE_ONLY)
      leak.record(); 
  }
  
  public ByteBuf order(ByteOrder endianness) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.order(endianness);
  }
  
  public ByteBuf slice() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.slice();
  }
  
  public ByteBuf slice(int index, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.slice(index, length);
  }
  
  public ByteBuf retainedSlice() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.retainedSlice();
  }
  
  public ByteBuf retainedSlice(int index, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.retainedSlice(index, length);
  }
  
  public ByteBuf retainedDuplicate() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.retainedDuplicate();
  }
  
  public ByteBuf readRetainedSlice(int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readRetainedSlice(length);
  }
  
  public ByteBuf duplicate() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.duplicate();
  }
  
  public ByteBuf readSlice(int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readSlice(length);
  }
  
  public ByteBuf discardReadBytes() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.discardReadBytes();
  }
  
  public ByteBuf discardSomeReadBytes() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.discardSomeReadBytes();
  }
  
  public ByteBuf ensureWritable(int minWritableBytes) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.ensureWritable(minWritableBytes);
  }
  
  public int ensureWritable(int minWritableBytes, boolean force) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.ensureWritable(minWritableBytes, force);
  }
  
  public boolean getBoolean(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getBoolean(index);
  }
  
  public byte getByte(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getByte(index);
  }
  
  public short getUnsignedByte(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedByte(index);
  }
  
  public short getShort(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getShort(index);
  }
  
  public int getUnsignedShort(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedShort(index);
  }
  
  public int getMedium(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getMedium(index);
  }
  
  public int getUnsignedMedium(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedMedium(index);
  }
  
  public int getInt(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getInt(index);
  }
  
  public long getUnsignedInt(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedInt(index);
  }
  
  public long getLong(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getLong(index);
  }
  
  public char getChar(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getChar(index);
  }
  
  public float getFloat(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getFloat(index);
  }
  
  public double getDouble(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getDouble(index);
  }
  
  public ByteBuf getBytes(int index, ByteBuf dst) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst);
  }
  
  public ByteBuf getBytes(int index, ByteBuf dst, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst, length);
  }
  
  public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst, dstIndex, length);
  }
  
  public ByteBuf getBytes(int index, byte[] dst) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst);
  }
  
  public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst, dstIndex, length);
  }
  
  public ByteBuf getBytes(int index, ByteBuffer dst) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst);
  }
  
  public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, out, length);
  }
  
  public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, out, length);
  }
  
  public CharSequence getCharSequence(int index, int length, Charset charset) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getCharSequence(index, length, charset);
  }
  
  public ByteBuf setBoolean(int index, boolean value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setBoolean(index, value);
  }
  
  public ByteBuf setByte(int index, int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setByte(index, value);
  }
  
  public ByteBuf setShort(int index, int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setShort(index, value);
  }
  
  public ByteBuf setMedium(int index, int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setMedium(index, value);
  }
  
  public ByteBuf setInt(int index, int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setInt(index, value);
  }
  
  public ByteBuf setLong(int index, long value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setLong(index, value);
  }
  
  public ByteBuf setChar(int index, int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setChar(index, value);
  }
  
  public ByteBuf setFloat(int index, float value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setFloat(index, value);
  }
  
  public ByteBuf setDouble(int index, double value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setDouble(index, value);
  }
  
  public ByteBuf setBytes(int index, ByteBuf src) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src);
  }
  
  public ByteBuf setBytes(int index, ByteBuf src, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src, length);
  }
  
  public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src, srcIndex, length);
  }
  
  public ByteBuf setBytes(int index, byte[] src) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src);
  }
  
  public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src, srcIndex, length);
  }
  
  public ByteBuf setBytes(int index, ByteBuffer src) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src);
  }
  
  public int setBytes(int index, InputStream in, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, in, length);
  }
  
  public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, in, length);
  }
  
  public ByteBuf setZero(int index, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setZero(index, length);
  }
  
  public int setCharSequence(int index, CharSequence sequence, Charset charset) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setCharSequence(index, sequence, charset);
  }
  
  public boolean readBoolean() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readBoolean();
  }
  
  public byte readByte() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readByte();
  }
  
  public short readUnsignedByte() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedByte();
  }
  
  public short readShort() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readShort();
  }
  
  public int readUnsignedShort() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedShort();
  }
  
  public int readMedium() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readMedium();
  }
  
  public int readUnsignedMedium() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedMedium();
  }
  
  public int readInt() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readInt();
  }
  
  public long readUnsignedInt() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedInt();
  }
  
  public long readLong() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readLong();
  }
  
  public char readChar() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readChar();
  }
  
  public float readFloat() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readFloat();
  }
  
  public double readDouble() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readDouble();
  }
  
  public ByteBuf readBytes(int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(length);
  }
  
  public ByteBuf readBytes(ByteBuf dst) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst);
  }
  
  public ByteBuf readBytes(ByteBuf dst, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst, length);
  }
  
  public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst, dstIndex, length);
  }
  
  public ByteBuf readBytes(byte[] dst) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst);
  }
  
  public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst, dstIndex, length);
  }
  
  public ByteBuf readBytes(ByteBuffer dst) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst);
  }
  
  public ByteBuf readBytes(OutputStream out, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(out, length);
  }
  
  public int readBytes(GatheringByteChannel out, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(out, length);
  }
  
  public CharSequence readCharSequence(int length, Charset charset) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readCharSequence(length, charset);
  }
  
  public ByteBuf skipBytes(int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.skipBytes(length);
  }
  
  public ByteBuf writeBoolean(boolean value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeBoolean(value);
  }
  
  public ByteBuf writeByte(int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeByte(value);
  }
  
  public ByteBuf writeShort(int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeShort(value);
  }
  
  public ByteBuf writeMedium(int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeMedium(value);
  }
  
  public ByteBuf writeInt(int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeInt(value);
  }
  
  public ByteBuf writeLong(long value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeLong(value);
  }
  
  public ByteBuf writeChar(int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeChar(value);
  }
  
  public ByteBuf writeFloat(float value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeFloat(value);
  }
  
  public ByteBuf writeDouble(double value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeDouble(value);
  }
  
  public ByteBuf writeBytes(ByteBuf src) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src);
  }
  
  public ByteBuf writeBytes(ByteBuf src, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src, length);
  }
  
  public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src, srcIndex, length);
  }
  
  public ByteBuf writeBytes(byte[] src) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src);
  }
  
  public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src, srcIndex, length);
  }
  
  public ByteBuf writeBytes(ByteBuffer src) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src);
  }
  
  public int writeBytes(InputStream in, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(in, length);
  }
  
  public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(in, length);
  }
  
  public ByteBuf writeZero(int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeZero(length);
  }
  
  public int indexOf(int fromIndex, int toIndex, byte value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.indexOf(fromIndex, toIndex, value);
  }
  
  public int bytesBefore(byte value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.bytesBefore(value);
  }
  
  public int bytesBefore(int length, byte value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.bytesBefore(length, value);
  }
  
  public int bytesBefore(int index, int length, byte value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.bytesBefore(index, length, value);
  }
  
  public int forEachByte(ByteProcessor processor) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.forEachByte(processor);
  }
  
  public int forEachByte(int index, int length, ByteProcessor processor) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.forEachByte(index, length, processor);
  }
  
  public int forEachByteDesc(ByteProcessor processor) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.forEachByteDesc(processor);
  }
  
  public int forEachByteDesc(int index, int length, ByteProcessor processor) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.forEachByteDesc(index, length, processor);
  }
  
  public ByteBuf copy() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.copy();
  }
  
  public ByteBuf copy(int index, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.copy(index, length);
  }
  
  public int nioBufferCount() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.nioBufferCount();
  }
  
  public ByteBuffer nioBuffer() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.nioBuffer();
  }
  
  public ByteBuffer nioBuffer(int index, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.nioBuffer(index, length);
  }
  
  public ByteBuffer[] nioBuffers() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.nioBuffers();
  }
  
  public ByteBuffer[] nioBuffers(int index, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.nioBuffers(index, length);
  }
  
  public ByteBuffer internalNioBuffer(int index, int length) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.internalNioBuffer(index, length);
  }
  
  public String toString(Charset charset) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.toString(charset);
  }
  
  public String toString(int index, int length, Charset charset) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.toString(index, length, charset);
  }
  
  public ByteBuf capacity(int newCapacity) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.capacity(newCapacity);
  }
  
  public short getShortLE(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getShortLE(index);
  }
  
  public int getUnsignedShortLE(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedShortLE(index);
  }
  
  public int getMediumLE(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getMediumLE(index);
  }
  
  public int getUnsignedMediumLE(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedMediumLE(index);
  }
  
  public int getIntLE(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getIntLE(index);
  }
  
  public long getUnsignedIntLE(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedIntLE(index);
  }
  
  public long getLongLE(int index) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getLongLE(index);
  }
  
  public ByteBuf setShortLE(int index, int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setShortLE(index, value);
  }
  
  public ByteBuf setIntLE(int index, int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setIntLE(index, value);
  }
  
  public ByteBuf setMediumLE(int index, int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setMediumLE(index, value);
  }
  
  public ByteBuf setLongLE(int index, long value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setLongLE(index, value);
  }
  
  public short readShortLE() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readShortLE();
  }
  
  public int readUnsignedShortLE() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedShortLE();
  }
  
  public int readMediumLE() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readMediumLE();
  }
  
  public int readUnsignedMediumLE() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedMediumLE();
  }
  
  public int readIntLE() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readIntLE();
  }
  
  public long readUnsignedIntLE() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedIntLE();
  }
  
  public long readLongLE() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readLongLE();
  }
  
  public ByteBuf writeShortLE(int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeShortLE(value);
  }
  
  public ByteBuf writeMediumLE(int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeMediumLE(value);
  }
  
  public ByteBuf writeIntLE(int value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeIntLE(value);
  }
  
  public ByteBuf writeLongLE(long value) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeLongLE(value);
  }
  
  public int writeCharSequence(CharSequence sequence, Charset charset) {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeCharSequence(sequence, charset);
  }
  
  public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, out, position, length);
  }
  
  public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, in, position, length);
  }
  
  public int readBytes(FileChannel out, long position, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(out, position, length);
  }
  
  public int writeBytes(FileChannel in, long position, int length) throws IOException {
    recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(in, position, length);
  }
  
  public ByteBuf asReadOnly() {
    recordLeakNonRefCountingOperation(this.leak);
    return super.asReadOnly();
  }
  
  public ByteBuf retain() {
    this.leak.record();
    return super.retain();
  }
  
  public ByteBuf retain(int increment) {
    this.leak.record();
    return super.retain(increment);
  }
  
  public boolean release() {
    this.leak.record();
    return super.release();
  }
  
  public boolean release(int decrement) {
    this.leak.record();
    return super.release(decrement);
  }
  
  public ByteBuf touch() {
    this.leak.record();
    return this;
  }
  
  public ByteBuf touch(Object hint) {
    this.leak.record(hint);
    return this;
  }
  
  protected AdvancedLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf buf, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker) {
    return new AdvancedLeakAwareByteBuf(buf, trackedByteBuf, leakTracker);
  }
}

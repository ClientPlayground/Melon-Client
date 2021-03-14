package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ByteProcessor;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.ResourceLeakTracker;
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

final class AdvancedLeakAwareCompositeByteBuf extends SimpleLeakAwareCompositeByteBuf {
  AdvancedLeakAwareCompositeByteBuf(CompositeByteBuf wrapped, ResourceLeakTracker<ByteBuf> leak) {
    super(wrapped, leak);
  }
  
  public ByteBuf order(ByteOrder endianness) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.order(endianness);
  }
  
  public ByteBuf slice() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.slice();
  }
  
  public ByteBuf retainedSlice() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.retainedSlice();
  }
  
  public ByteBuf slice(int index, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.slice(index, length);
  }
  
  public ByteBuf retainedSlice(int index, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.retainedSlice(index, length);
  }
  
  public ByteBuf duplicate() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.duplicate();
  }
  
  public ByteBuf retainedDuplicate() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.retainedDuplicate();
  }
  
  public ByteBuf readSlice(int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readSlice(length);
  }
  
  public ByteBuf readRetainedSlice(int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readRetainedSlice(length);
  }
  
  public ByteBuf asReadOnly() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.asReadOnly();
  }
  
  public boolean isReadOnly() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.isReadOnly();
  }
  
  public CompositeByteBuf discardReadBytes() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.discardReadBytes();
  }
  
  public CompositeByteBuf discardSomeReadBytes() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.discardSomeReadBytes();
  }
  
  public CompositeByteBuf ensureWritable(int minWritableBytes) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.ensureWritable(minWritableBytes);
  }
  
  public int ensureWritable(int minWritableBytes, boolean force) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.ensureWritable(minWritableBytes, force);
  }
  
  public boolean getBoolean(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getBoolean(index);
  }
  
  public byte getByte(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getByte(index);
  }
  
  public short getUnsignedByte(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedByte(index);
  }
  
  public short getShort(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getShort(index);
  }
  
  public int getUnsignedShort(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedShort(index);
  }
  
  public int getMedium(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getMedium(index);
  }
  
  public int getUnsignedMedium(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedMedium(index);
  }
  
  public int getInt(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getInt(index);
  }
  
  public long getUnsignedInt(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedInt(index);
  }
  
  public long getLong(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getLong(index);
  }
  
  public char getChar(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getChar(index);
  }
  
  public float getFloat(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getFloat(index);
  }
  
  public double getDouble(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getDouble(index);
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuf dst) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst);
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuf dst, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst, length);
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst, dstIndex, length);
  }
  
  public CompositeByteBuf getBytes(int index, byte[] dst) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst);
  }
  
  public CompositeByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst, dstIndex, length);
  }
  
  public CompositeByteBuf getBytes(int index, ByteBuffer dst) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, dst);
  }
  
  public CompositeByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, out, length);
  }
  
  public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, out, length);
  }
  
  public CharSequence getCharSequence(int index, int length, Charset charset) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getCharSequence(index, length, charset);
  }
  
  public CompositeByteBuf setBoolean(int index, boolean value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setBoolean(index, value);
  }
  
  public CompositeByteBuf setByte(int index, int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setByte(index, value);
  }
  
  public CompositeByteBuf setShort(int index, int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setShort(index, value);
  }
  
  public CompositeByteBuf setMedium(int index, int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setMedium(index, value);
  }
  
  public CompositeByteBuf setInt(int index, int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setInt(index, value);
  }
  
  public CompositeByteBuf setLong(int index, long value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setLong(index, value);
  }
  
  public CompositeByteBuf setChar(int index, int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setChar(index, value);
  }
  
  public CompositeByteBuf setFloat(int index, float value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setFloat(index, value);
  }
  
  public CompositeByteBuf setDouble(int index, double value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setDouble(index, value);
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuf src) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src);
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuf src, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src, length);
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src, srcIndex, length);
  }
  
  public CompositeByteBuf setBytes(int index, byte[] src) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src);
  }
  
  public CompositeByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src, srcIndex, length);
  }
  
  public CompositeByteBuf setBytes(int index, ByteBuffer src) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, src);
  }
  
  public int setBytes(int index, InputStream in, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, in, length);
  }
  
  public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, in, length);
  }
  
  public CompositeByteBuf setZero(int index, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setZero(index, length);
  }
  
  public boolean readBoolean() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readBoolean();
  }
  
  public byte readByte() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readByte();
  }
  
  public short readUnsignedByte() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedByte();
  }
  
  public short readShort() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readShort();
  }
  
  public int readUnsignedShort() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedShort();
  }
  
  public int readMedium() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readMedium();
  }
  
  public int readUnsignedMedium() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedMedium();
  }
  
  public int readInt() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readInt();
  }
  
  public long readUnsignedInt() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedInt();
  }
  
  public long readLong() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readLong();
  }
  
  public char readChar() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readChar();
  }
  
  public float readFloat() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readFloat();
  }
  
  public double readDouble() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readDouble();
  }
  
  public ByteBuf readBytes(int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(length);
  }
  
  public CompositeByteBuf readBytes(ByteBuf dst) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst);
  }
  
  public CompositeByteBuf readBytes(ByteBuf dst, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst, length);
  }
  
  public CompositeByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst, dstIndex, length);
  }
  
  public CompositeByteBuf readBytes(byte[] dst) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst);
  }
  
  public CompositeByteBuf readBytes(byte[] dst, int dstIndex, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst, dstIndex, length);
  }
  
  public CompositeByteBuf readBytes(ByteBuffer dst) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(dst);
  }
  
  public CompositeByteBuf readBytes(OutputStream out, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(out, length);
  }
  
  public int readBytes(GatheringByteChannel out, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(out, length);
  }
  
  public CharSequence readCharSequence(int length, Charset charset) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readCharSequence(length, charset);
  }
  
  public CompositeByteBuf skipBytes(int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.skipBytes(length);
  }
  
  public CompositeByteBuf writeBoolean(boolean value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeBoolean(value);
  }
  
  public CompositeByteBuf writeByte(int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeByte(value);
  }
  
  public CompositeByteBuf writeShort(int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeShort(value);
  }
  
  public CompositeByteBuf writeMedium(int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeMedium(value);
  }
  
  public CompositeByteBuf writeInt(int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeInt(value);
  }
  
  public CompositeByteBuf writeLong(long value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeLong(value);
  }
  
  public CompositeByteBuf writeChar(int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeChar(value);
  }
  
  public CompositeByteBuf writeFloat(float value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeFloat(value);
  }
  
  public CompositeByteBuf writeDouble(double value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeDouble(value);
  }
  
  public CompositeByteBuf writeBytes(ByteBuf src) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src);
  }
  
  public CompositeByteBuf writeBytes(ByteBuf src, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src, length);
  }
  
  public CompositeByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src, srcIndex, length);
  }
  
  public CompositeByteBuf writeBytes(byte[] src) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src);
  }
  
  public CompositeByteBuf writeBytes(byte[] src, int srcIndex, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src, srcIndex, length);
  }
  
  public CompositeByteBuf writeBytes(ByteBuffer src) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(src);
  }
  
  public int writeBytes(InputStream in, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(in, length);
  }
  
  public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(in, length);
  }
  
  public CompositeByteBuf writeZero(int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeZero(length);
  }
  
  public int writeCharSequence(CharSequence sequence, Charset charset) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeCharSequence(sequence, charset);
  }
  
  public int indexOf(int fromIndex, int toIndex, byte value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.indexOf(fromIndex, toIndex, value);
  }
  
  public int bytesBefore(byte value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.bytesBefore(value);
  }
  
  public int bytesBefore(int length, byte value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.bytesBefore(length, value);
  }
  
  public int bytesBefore(int index, int length, byte value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.bytesBefore(index, length, value);
  }
  
  public int forEachByte(ByteProcessor processor) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.forEachByte(processor);
  }
  
  public int forEachByte(int index, int length, ByteProcessor processor) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.forEachByte(index, length, processor);
  }
  
  public int forEachByteDesc(ByteProcessor processor) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.forEachByteDesc(processor);
  }
  
  public int forEachByteDesc(int index, int length, ByteProcessor processor) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.forEachByteDesc(index, length, processor);
  }
  
  public ByteBuf copy() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.copy();
  }
  
  public ByteBuf copy(int index, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.copy(index, length);
  }
  
  public int nioBufferCount() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.nioBufferCount();
  }
  
  public ByteBuffer nioBuffer() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.nioBuffer();
  }
  
  public ByteBuffer nioBuffer(int index, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.nioBuffer(index, length);
  }
  
  public ByteBuffer[] nioBuffers() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.nioBuffers();
  }
  
  public ByteBuffer[] nioBuffers(int index, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.nioBuffers(index, length);
  }
  
  public ByteBuffer internalNioBuffer(int index, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.internalNioBuffer(index, length);
  }
  
  public String toString(Charset charset) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.toString(charset);
  }
  
  public String toString(int index, int length, Charset charset) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.toString(index, length, charset);
  }
  
  public CompositeByteBuf capacity(int newCapacity) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.capacity(newCapacity);
  }
  
  public short getShortLE(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getShortLE(index);
  }
  
  public int getUnsignedShortLE(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedShortLE(index);
  }
  
  public int getUnsignedMediumLE(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedMediumLE(index);
  }
  
  public int getMediumLE(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getMediumLE(index);
  }
  
  public int getIntLE(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getIntLE(index);
  }
  
  public long getUnsignedIntLE(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getUnsignedIntLE(index);
  }
  
  public long getLongLE(int index) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getLongLE(index);
  }
  
  public ByteBuf setShortLE(int index, int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setShortLE(index, value);
  }
  
  public ByteBuf setMediumLE(int index, int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setMediumLE(index, value);
  }
  
  public ByteBuf setIntLE(int index, int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setIntLE(index, value);
  }
  
  public ByteBuf setLongLE(int index, long value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setLongLE(index, value);
  }
  
  public int setCharSequence(int index, CharSequence sequence, Charset charset) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setCharSequence(index, sequence, charset);
  }
  
  public short readShortLE() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readShortLE();
  }
  
  public int readUnsignedShortLE() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedShortLE();
  }
  
  public int readMediumLE() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readMediumLE();
  }
  
  public int readUnsignedMediumLE() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedMediumLE();
  }
  
  public int readIntLE() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readIntLE();
  }
  
  public long readUnsignedIntLE() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readUnsignedIntLE();
  }
  
  public long readLongLE() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readLongLE();
  }
  
  public ByteBuf writeShortLE(int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeShortLE(value);
  }
  
  public ByteBuf writeMediumLE(int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeMediumLE(value);
  }
  
  public ByteBuf writeIntLE(int value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeIntLE(value);
  }
  
  public ByteBuf writeLongLE(long value) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeLongLE(value);
  }
  
  public CompositeByteBuf addComponent(ByteBuf buffer) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.addComponent(buffer);
  }
  
  public CompositeByteBuf addComponents(ByteBuf... buffers) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.addComponents(buffers);
  }
  
  public CompositeByteBuf addComponents(Iterable<ByteBuf> buffers) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.addComponents(buffers);
  }
  
  public CompositeByteBuf addComponent(int cIndex, ByteBuf buffer) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.addComponent(cIndex, buffer);
  }
  
  public CompositeByteBuf addComponents(int cIndex, ByteBuf... buffers) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.addComponents(cIndex, buffers);
  }
  
  public CompositeByteBuf addComponents(int cIndex, Iterable<ByteBuf> buffers) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.addComponents(cIndex, buffers);
  }
  
  public CompositeByteBuf addComponent(boolean increaseWriterIndex, ByteBuf buffer) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.addComponent(increaseWriterIndex, buffer);
  }
  
  public CompositeByteBuf addComponents(boolean increaseWriterIndex, ByteBuf... buffers) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.addComponents(increaseWriterIndex, buffers);
  }
  
  public CompositeByteBuf addComponents(boolean increaseWriterIndex, Iterable<ByteBuf> buffers) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.addComponents(increaseWriterIndex, buffers);
  }
  
  public CompositeByteBuf addComponent(boolean increaseWriterIndex, int cIndex, ByteBuf buffer) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.addComponent(increaseWriterIndex, cIndex, buffer);
  }
  
  public CompositeByteBuf removeComponent(int cIndex) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.removeComponent(cIndex);
  }
  
  public CompositeByteBuf removeComponents(int cIndex, int numComponents) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.removeComponents(cIndex, numComponents);
  }
  
  public Iterator<ByteBuf> iterator() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.iterator();
  }
  
  public List<ByteBuf> decompose(int offset, int length) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.decompose(offset, length);
  }
  
  public CompositeByteBuf consolidate() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.consolidate();
  }
  
  public CompositeByteBuf discardReadComponents() {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.discardReadComponents();
  }
  
  public CompositeByteBuf consolidate(int cIndex, int numComponents) {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.consolidate(cIndex, numComponents);
  }
  
  public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.getBytes(index, out, position, length);
  }
  
  public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.setBytes(index, in, position, length);
  }
  
  public int readBytes(FileChannel out, long position, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.readBytes(out, position, length);
  }
  
  public int writeBytes(FileChannel in, long position, int length) throws IOException {
    AdvancedLeakAwareByteBuf.recordLeakNonRefCountingOperation(this.leak);
    return super.writeBytes(in, position, length);
  }
  
  public CompositeByteBuf retain() {
    this.leak.record();
    return super.retain();
  }
  
  public CompositeByteBuf retain(int increment) {
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
  
  public CompositeByteBuf touch() {
    this.leak.record();
    return this;
  }
  
  public CompositeByteBuf touch(Object hint) {
    this.leak.record(hint);
    return this;
  }
  
  protected AdvancedLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker) {
    return new AdvancedLeakAwareByteBuf(wrapped, trackedByteBuf, leakTracker);
  }
}

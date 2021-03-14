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

public abstract class ByteBuf implements ReferenceCounted, Comparable<ByteBuf> {
  public float getFloatLE(int index) {
    return Float.intBitsToFloat(getIntLE(index));
  }
  
  public double getDoubleLE(int index) {
    return Double.longBitsToDouble(getLongLE(index));
  }
  
  public ByteBuf setFloatLE(int index, float value) {
    return setIntLE(index, Float.floatToRawIntBits(value));
  }
  
  public ByteBuf setDoubleLE(int index, double value) {
    return setLongLE(index, Double.doubleToRawLongBits(value));
  }
  
  public float readFloatLE() {
    return Float.intBitsToFloat(readIntLE());
  }
  
  public double readDoubleLE() {
    return Double.longBitsToDouble(readLongLE());
  }
  
  public ByteBuf writeFloatLE(float value) {
    return writeIntLE(Float.floatToRawIntBits(value));
  }
  
  public ByteBuf writeDoubleLE(double value) {
    return writeLongLE(Double.doubleToRawLongBits(value));
  }
  
  public abstract int capacity();
  
  public abstract ByteBuf capacity(int paramInt);
  
  public abstract int maxCapacity();
  
  public abstract ByteBufAllocator alloc();
  
  @Deprecated
  public abstract ByteOrder order();
  
  @Deprecated
  public abstract ByteBuf order(ByteOrder paramByteOrder);
  
  public abstract ByteBuf unwrap();
  
  public abstract boolean isDirect();
  
  public abstract boolean isReadOnly();
  
  public abstract ByteBuf asReadOnly();
  
  public abstract int readerIndex();
  
  public abstract ByteBuf readerIndex(int paramInt);
  
  public abstract int writerIndex();
  
  public abstract ByteBuf writerIndex(int paramInt);
  
  public abstract ByteBuf setIndex(int paramInt1, int paramInt2);
  
  public abstract int readableBytes();
  
  public abstract int writableBytes();
  
  public abstract int maxWritableBytes();
  
  public abstract boolean isReadable();
  
  public abstract boolean isReadable(int paramInt);
  
  public abstract boolean isWritable();
  
  public abstract boolean isWritable(int paramInt);
  
  public abstract ByteBuf clear();
  
  public abstract ByteBuf markReaderIndex();
  
  public abstract ByteBuf resetReaderIndex();
  
  public abstract ByteBuf markWriterIndex();
  
  public abstract ByteBuf resetWriterIndex();
  
  public abstract ByteBuf discardReadBytes();
  
  public abstract ByteBuf discardSomeReadBytes();
  
  public abstract ByteBuf ensureWritable(int paramInt);
  
  public abstract int ensureWritable(int paramInt, boolean paramBoolean);
  
  public abstract boolean getBoolean(int paramInt);
  
  public abstract byte getByte(int paramInt);
  
  public abstract short getUnsignedByte(int paramInt);
  
  public abstract short getShort(int paramInt);
  
  public abstract short getShortLE(int paramInt);
  
  public abstract int getUnsignedShort(int paramInt);
  
  public abstract int getUnsignedShortLE(int paramInt);
  
  public abstract int getMedium(int paramInt);
  
  public abstract int getMediumLE(int paramInt);
  
  public abstract int getUnsignedMedium(int paramInt);
  
  public abstract int getUnsignedMediumLE(int paramInt);
  
  public abstract int getInt(int paramInt);
  
  public abstract int getIntLE(int paramInt);
  
  public abstract long getUnsignedInt(int paramInt);
  
  public abstract long getUnsignedIntLE(int paramInt);
  
  public abstract long getLong(int paramInt);
  
  public abstract long getLongLE(int paramInt);
  
  public abstract char getChar(int paramInt);
  
  public abstract float getFloat(int paramInt);
  
  public abstract double getDouble(int paramInt);
  
  public abstract ByteBuf getBytes(int paramInt, ByteBuf paramByteBuf);
  
  public abstract ByteBuf getBytes(int paramInt1, ByteBuf paramByteBuf, int paramInt2);
  
  public abstract ByteBuf getBytes(int paramInt1, ByteBuf paramByteBuf, int paramInt2, int paramInt3);
  
  public abstract ByteBuf getBytes(int paramInt, byte[] paramArrayOfbyte);
  
  public abstract ByteBuf getBytes(int paramInt1, byte[] paramArrayOfbyte, int paramInt2, int paramInt3);
  
  public abstract ByteBuf getBytes(int paramInt, ByteBuffer paramByteBuffer);
  
  public abstract ByteBuf getBytes(int paramInt1, OutputStream paramOutputStream, int paramInt2) throws IOException;
  
  public abstract int getBytes(int paramInt1, GatheringByteChannel paramGatheringByteChannel, int paramInt2) throws IOException;
  
  public abstract int getBytes(int paramInt1, FileChannel paramFileChannel, long paramLong, int paramInt2) throws IOException;
  
  public abstract CharSequence getCharSequence(int paramInt1, int paramInt2, Charset paramCharset);
  
  public abstract ByteBuf setBoolean(int paramInt, boolean paramBoolean);
  
  public abstract ByteBuf setByte(int paramInt1, int paramInt2);
  
  public abstract ByteBuf setShort(int paramInt1, int paramInt2);
  
  public abstract ByteBuf setShortLE(int paramInt1, int paramInt2);
  
  public abstract ByteBuf setMedium(int paramInt1, int paramInt2);
  
  public abstract ByteBuf setMediumLE(int paramInt1, int paramInt2);
  
  public abstract ByteBuf setInt(int paramInt1, int paramInt2);
  
  public abstract ByteBuf setIntLE(int paramInt1, int paramInt2);
  
  public abstract ByteBuf setLong(int paramInt, long paramLong);
  
  public abstract ByteBuf setLongLE(int paramInt, long paramLong);
  
  public abstract ByteBuf setChar(int paramInt1, int paramInt2);
  
  public abstract ByteBuf setFloat(int paramInt, float paramFloat);
  
  public abstract ByteBuf setDouble(int paramInt, double paramDouble);
  
  public abstract ByteBuf setBytes(int paramInt, ByteBuf paramByteBuf);
  
  public abstract ByteBuf setBytes(int paramInt1, ByteBuf paramByteBuf, int paramInt2);
  
  public abstract ByteBuf setBytes(int paramInt1, ByteBuf paramByteBuf, int paramInt2, int paramInt3);
  
  public abstract ByteBuf setBytes(int paramInt, byte[] paramArrayOfbyte);
  
  public abstract ByteBuf setBytes(int paramInt1, byte[] paramArrayOfbyte, int paramInt2, int paramInt3);
  
  public abstract ByteBuf setBytes(int paramInt, ByteBuffer paramByteBuffer);
  
  public abstract int setBytes(int paramInt1, InputStream paramInputStream, int paramInt2) throws IOException;
  
  public abstract int setBytes(int paramInt1, ScatteringByteChannel paramScatteringByteChannel, int paramInt2) throws IOException;
  
  public abstract int setBytes(int paramInt1, FileChannel paramFileChannel, long paramLong, int paramInt2) throws IOException;
  
  public abstract ByteBuf setZero(int paramInt1, int paramInt2);
  
  public abstract int setCharSequence(int paramInt, CharSequence paramCharSequence, Charset paramCharset);
  
  public abstract boolean readBoolean();
  
  public abstract byte readByte();
  
  public abstract short readUnsignedByte();
  
  public abstract short readShort();
  
  public abstract short readShortLE();
  
  public abstract int readUnsignedShort();
  
  public abstract int readUnsignedShortLE();
  
  public abstract int readMedium();
  
  public abstract int readMediumLE();
  
  public abstract int readUnsignedMedium();
  
  public abstract int readUnsignedMediumLE();
  
  public abstract int readInt();
  
  public abstract int readIntLE();
  
  public abstract long readUnsignedInt();
  
  public abstract long readUnsignedIntLE();
  
  public abstract long readLong();
  
  public abstract long readLongLE();
  
  public abstract char readChar();
  
  public abstract float readFloat();
  
  public abstract double readDouble();
  
  public abstract ByteBuf readBytes(int paramInt);
  
  public abstract ByteBuf readSlice(int paramInt);
  
  public abstract ByteBuf readRetainedSlice(int paramInt);
  
  public abstract ByteBuf readBytes(ByteBuf paramByteBuf);
  
  public abstract ByteBuf readBytes(ByteBuf paramByteBuf, int paramInt);
  
  public abstract ByteBuf readBytes(ByteBuf paramByteBuf, int paramInt1, int paramInt2);
  
  public abstract ByteBuf readBytes(byte[] paramArrayOfbyte);
  
  public abstract ByteBuf readBytes(byte[] paramArrayOfbyte, int paramInt1, int paramInt2);
  
  public abstract ByteBuf readBytes(ByteBuffer paramByteBuffer);
  
  public abstract ByteBuf readBytes(OutputStream paramOutputStream, int paramInt) throws IOException;
  
  public abstract int readBytes(GatheringByteChannel paramGatheringByteChannel, int paramInt) throws IOException;
  
  public abstract CharSequence readCharSequence(int paramInt, Charset paramCharset);
  
  public abstract int readBytes(FileChannel paramFileChannel, long paramLong, int paramInt) throws IOException;
  
  public abstract ByteBuf skipBytes(int paramInt);
  
  public abstract ByteBuf writeBoolean(boolean paramBoolean);
  
  public abstract ByteBuf writeByte(int paramInt);
  
  public abstract ByteBuf writeShort(int paramInt);
  
  public abstract ByteBuf writeShortLE(int paramInt);
  
  public abstract ByteBuf writeMedium(int paramInt);
  
  public abstract ByteBuf writeMediumLE(int paramInt);
  
  public abstract ByteBuf writeInt(int paramInt);
  
  public abstract ByteBuf writeIntLE(int paramInt);
  
  public abstract ByteBuf writeLong(long paramLong);
  
  public abstract ByteBuf writeLongLE(long paramLong);
  
  public abstract ByteBuf writeChar(int paramInt);
  
  public abstract ByteBuf writeFloat(float paramFloat);
  
  public abstract ByteBuf writeDouble(double paramDouble);
  
  public abstract ByteBuf writeBytes(ByteBuf paramByteBuf);
  
  public abstract ByteBuf writeBytes(ByteBuf paramByteBuf, int paramInt);
  
  public abstract ByteBuf writeBytes(ByteBuf paramByteBuf, int paramInt1, int paramInt2);
  
  public abstract ByteBuf writeBytes(byte[] paramArrayOfbyte);
  
  public abstract ByteBuf writeBytes(byte[] paramArrayOfbyte, int paramInt1, int paramInt2);
  
  public abstract ByteBuf writeBytes(ByteBuffer paramByteBuffer);
  
  public abstract int writeBytes(InputStream paramInputStream, int paramInt) throws IOException;
  
  public abstract int writeBytes(ScatteringByteChannel paramScatteringByteChannel, int paramInt) throws IOException;
  
  public abstract int writeBytes(FileChannel paramFileChannel, long paramLong, int paramInt) throws IOException;
  
  public abstract ByteBuf writeZero(int paramInt);
  
  public abstract int writeCharSequence(CharSequence paramCharSequence, Charset paramCharset);
  
  public abstract int indexOf(int paramInt1, int paramInt2, byte paramByte);
  
  public abstract int bytesBefore(byte paramByte);
  
  public abstract int bytesBefore(int paramInt, byte paramByte);
  
  public abstract int bytesBefore(int paramInt1, int paramInt2, byte paramByte);
  
  public abstract int forEachByte(ByteProcessor paramByteProcessor);
  
  public abstract int forEachByte(int paramInt1, int paramInt2, ByteProcessor paramByteProcessor);
  
  public abstract int forEachByteDesc(ByteProcessor paramByteProcessor);
  
  public abstract int forEachByteDesc(int paramInt1, int paramInt2, ByteProcessor paramByteProcessor);
  
  public abstract ByteBuf copy();
  
  public abstract ByteBuf copy(int paramInt1, int paramInt2);
  
  public abstract ByteBuf slice();
  
  public abstract ByteBuf retainedSlice();
  
  public abstract ByteBuf slice(int paramInt1, int paramInt2);
  
  public abstract ByteBuf retainedSlice(int paramInt1, int paramInt2);
  
  public abstract ByteBuf duplicate();
  
  public abstract ByteBuf retainedDuplicate();
  
  public abstract int nioBufferCount();
  
  public abstract ByteBuffer nioBuffer();
  
  public abstract ByteBuffer nioBuffer(int paramInt1, int paramInt2);
  
  public abstract ByteBuffer internalNioBuffer(int paramInt1, int paramInt2);
  
  public abstract ByteBuffer[] nioBuffers();
  
  public abstract ByteBuffer[] nioBuffers(int paramInt1, int paramInt2);
  
  public abstract boolean hasArray();
  
  public abstract byte[] array();
  
  public abstract int arrayOffset();
  
  public abstract boolean hasMemoryAddress();
  
  public abstract long memoryAddress();
  
  public abstract String toString(Charset paramCharset);
  
  public abstract String toString(int paramInt1, int paramInt2, Charset paramCharset);
  
  public abstract int hashCode();
  
  public abstract boolean equals(Object paramObject);
  
  public abstract int compareTo(ByteBuf paramByteBuf);
  
  public abstract String toString();
  
  public abstract ByteBuf retain(int paramInt);
  
  public abstract ByteBuf retain();
  
  public abstract ByteBuf touch();
  
  public abstract ByteBuf touch(Object paramObject);
}

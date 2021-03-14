package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.nio.ByteOrder;

abstract class AbstractUnsafeSwappedByteBuf extends SwappedByteBuf {
  private final boolean nativeByteOrder;
  
  private final AbstractByteBuf wrapped;
  
  AbstractUnsafeSwappedByteBuf(AbstractByteBuf buf) {
    super(buf);
    assert PlatformDependent.isUnaligned();
    this.wrapped = buf;
    this.nativeByteOrder = (PlatformDependent.BIG_ENDIAN_NATIVE_ORDER == ((order() == ByteOrder.BIG_ENDIAN)));
  }
  
  public final long getLong(int index) {
    this.wrapped.checkIndex(index, 8);
    long v = _getLong(this.wrapped, index);
    return this.nativeByteOrder ? v : Long.reverseBytes(v);
  }
  
  public final float getFloat(int index) {
    return Float.intBitsToFloat(getInt(index));
  }
  
  public final double getDouble(int index) {
    return Double.longBitsToDouble(getLong(index));
  }
  
  public final char getChar(int index) {
    return (char)getShort(index);
  }
  
  public final long getUnsignedInt(int index) {
    return getInt(index) & 0xFFFFFFFFL;
  }
  
  public final int getInt(int index) {
    this.wrapped.checkIndex(index, 4);
    int v = _getInt(this.wrapped, index);
    return this.nativeByteOrder ? v : Integer.reverseBytes(v);
  }
  
  public final int getUnsignedShort(int index) {
    return getShort(index) & 0xFFFF;
  }
  
  public final short getShort(int index) {
    this.wrapped.checkIndex(index, 2);
    short v = _getShort(this.wrapped, index);
    return this.nativeByteOrder ? v : Short.reverseBytes(v);
  }
  
  public final ByteBuf setShort(int index, int value) {
    this.wrapped.checkIndex(index, 2);
    _setShort(this.wrapped, index, this.nativeByteOrder ? (short)value : Short.reverseBytes((short)value));
    return this;
  }
  
  public final ByteBuf setInt(int index, int value) {
    this.wrapped.checkIndex(index, 4);
    _setInt(this.wrapped, index, this.nativeByteOrder ? value : Integer.reverseBytes(value));
    return this;
  }
  
  public final ByteBuf setLong(int index, long value) {
    this.wrapped.checkIndex(index, 8);
    _setLong(this.wrapped, index, this.nativeByteOrder ? value : Long.reverseBytes(value));
    return this;
  }
  
  public final ByteBuf setChar(int index, int value) {
    setShort(index, value);
    return this;
  }
  
  public final ByteBuf setFloat(int index, float value) {
    setInt(index, Float.floatToRawIntBits(value));
    return this;
  }
  
  public final ByteBuf setDouble(int index, double value) {
    setLong(index, Double.doubleToRawLongBits(value));
    return this;
  }
  
  public final ByteBuf writeShort(int value) {
    this.wrapped.ensureWritable0(2);
    _setShort(this.wrapped, this.wrapped.writerIndex, this.nativeByteOrder ? (short)value : Short.reverseBytes((short)value));
    this.wrapped.writerIndex += 2;
    return this;
  }
  
  public final ByteBuf writeInt(int value) {
    this.wrapped.ensureWritable0(4);
    _setInt(this.wrapped, this.wrapped.writerIndex, this.nativeByteOrder ? value : Integer.reverseBytes(value));
    this.wrapped.writerIndex += 4;
    return this;
  }
  
  public final ByteBuf writeLong(long value) {
    this.wrapped.ensureWritable0(8);
    _setLong(this.wrapped, this.wrapped.writerIndex, this.nativeByteOrder ? value : Long.reverseBytes(value));
    this.wrapped.writerIndex += 8;
    return this;
  }
  
  public final ByteBuf writeChar(int value) {
    writeShort(value);
    return this;
  }
  
  public final ByteBuf writeFloat(float value) {
    writeInt(Float.floatToRawIntBits(value));
    return this;
  }
  
  public final ByteBuf writeDouble(double value) {
    writeLong(Double.doubleToRawLongBits(value));
    return this;
  }
  
  protected abstract short _getShort(AbstractByteBuf paramAbstractByteBuf, int paramInt);
  
  protected abstract int _getInt(AbstractByteBuf paramAbstractByteBuf, int paramInt);
  
  protected abstract long _getLong(AbstractByteBuf paramAbstractByteBuf, int paramInt);
  
  protected abstract void _setShort(AbstractByteBuf paramAbstractByteBuf, int paramInt, short paramShort);
  
  protected abstract void _setInt(AbstractByteBuf paramAbstractByteBuf, int paramInt1, int paramInt2);
  
  protected abstract void _setLong(AbstractByteBuf paramAbstractByteBuf, int paramInt, long paramLong);
}

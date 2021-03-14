package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ByteProcessor;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.IllegalReferenceCountException;
import com.github.steveice10.netty.util.ResourceLeakDetector;
import com.github.steveice10.netty.util.ResourceLeakDetectorFactory;
import com.github.steveice10.netty.util.internal.MathUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
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

public abstract class AbstractByteBuf extends ByteBuf {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractByteBuf.class);
  
  private static final String PROP_MODE = "com.github.steveice10.netty.buffer.bytebuf.checkAccessible";
  
  private static final boolean checkAccessible = SystemPropertyUtil.getBoolean("com.github.steveice10.netty.buffer.bytebuf.checkAccessible", true);
  
  static {
    if (logger.isDebugEnabled())
      logger.debug("-D{}: {}", "com.github.steveice10.netty.buffer.bytebuf.checkAccessible", Boolean.valueOf(checkAccessible)); 
  }
  
  static final ResourceLeakDetector<ByteBuf> leakDetector = ResourceLeakDetectorFactory.instance().newResourceLeakDetector(ByteBuf.class);
  
  int readerIndex;
  
  int writerIndex;
  
  private int markedReaderIndex;
  
  private int markedWriterIndex;
  
  private int maxCapacity;
  
  protected AbstractByteBuf(int maxCapacity) {
    if (maxCapacity < 0)
      throw new IllegalArgumentException("maxCapacity: " + maxCapacity + " (expected: >= 0)"); 
    this.maxCapacity = maxCapacity;
  }
  
  public boolean isReadOnly() {
    return false;
  }
  
  public ByteBuf asReadOnly() {
    if (isReadOnly())
      return this; 
    return Unpooled.unmodifiableBuffer(this);
  }
  
  public int maxCapacity() {
    return this.maxCapacity;
  }
  
  protected final void maxCapacity(int maxCapacity) {
    this.maxCapacity = maxCapacity;
  }
  
  public int readerIndex() {
    return this.readerIndex;
  }
  
  public ByteBuf readerIndex(int readerIndex) {
    if (readerIndex < 0 || readerIndex > this.writerIndex)
      throw new IndexOutOfBoundsException(String.format("readerIndex: %d (expected: 0 <= readerIndex <= writerIndex(%d))", new Object[] { Integer.valueOf(readerIndex), Integer.valueOf(this.writerIndex) })); 
    this.readerIndex = readerIndex;
    return this;
  }
  
  public int writerIndex() {
    return this.writerIndex;
  }
  
  public ByteBuf writerIndex(int writerIndex) {
    if (writerIndex < this.readerIndex || writerIndex > capacity())
      throw new IndexOutOfBoundsException(String.format("writerIndex: %d (expected: readerIndex(%d) <= writerIndex <= capacity(%d))", new Object[] { Integer.valueOf(writerIndex), Integer.valueOf(this.readerIndex), Integer.valueOf(capacity()) })); 
    this.writerIndex = writerIndex;
    return this;
  }
  
  public ByteBuf setIndex(int readerIndex, int writerIndex) {
    if (readerIndex < 0 || readerIndex > writerIndex || writerIndex > capacity())
      throw new IndexOutOfBoundsException(String.format("readerIndex: %d, writerIndex: %d (expected: 0 <= readerIndex <= writerIndex <= capacity(%d))", new Object[] { Integer.valueOf(readerIndex), Integer.valueOf(writerIndex), Integer.valueOf(capacity()) })); 
    setIndex0(readerIndex, writerIndex);
    return this;
  }
  
  public ByteBuf clear() {
    this.readerIndex = this.writerIndex = 0;
    return this;
  }
  
  public boolean isReadable() {
    return (this.writerIndex > this.readerIndex);
  }
  
  public boolean isReadable(int numBytes) {
    return (this.writerIndex - this.readerIndex >= numBytes);
  }
  
  public boolean isWritable() {
    return (capacity() > this.writerIndex);
  }
  
  public boolean isWritable(int numBytes) {
    return (capacity() - this.writerIndex >= numBytes);
  }
  
  public int readableBytes() {
    return this.writerIndex - this.readerIndex;
  }
  
  public int writableBytes() {
    return capacity() - this.writerIndex;
  }
  
  public int maxWritableBytes() {
    return maxCapacity() - this.writerIndex;
  }
  
  public ByteBuf markReaderIndex() {
    this.markedReaderIndex = this.readerIndex;
    return this;
  }
  
  public ByteBuf resetReaderIndex() {
    readerIndex(this.markedReaderIndex);
    return this;
  }
  
  public ByteBuf markWriterIndex() {
    this.markedWriterIndex = this.writerIndex;
    return this;
  }
  
  public ByteBuf resetWriterIndex() {
    writerIndex(this.markedWriterIndex);
    return this;
  }
  
  public ByteBuf discardReadBytes() {
    ensureAccessible();
    if (this.readerIndex == 0)
      return this; 
    if (this.readerIndex != this.writerIndex) {
      setBytes(0, this, this.readerIndex, this.writerIndex - this.readerIndex);
      this.writerIndex -= this.readerIndex;
      adjustMarkers(this.readerIndex);
      this.readerIndex = 0;
    } else {
      adjustMarkers(this.readerIndex);
      this.writerIndex = this.readerIndex = 0;
    } 
    return this;
  }
  
  public ByteBuf discardSomeReadBytes() {
    ensureAccessible();
    if (this.readerIndex == 0)
      return this; 
    if (this.readerIndex == this.writerIndex) {
      adjustMarkers(this.readerIndex);
      this.writerIndex = this.readerIndex = 0;
      return this;
    } 
    if (this.readerIndex >= capacity() >>> 1) {
      setBytes(0, this, this.readerIndex, this.writerIndex - this.readerIndex);
      this.writerIndex -= this.readerIndex;
      adjustMarkers(this.readerIndex);
      this.readerIndex = 0;
    } 
    return this;
  }
  
  protected final void adjustMarkers(int decrement) {
    int markedReaderIndex = this.markedReaderIndex;
    if (markedReaderIndex <= decrement) {
      this.markedReaderIndex = 0;
      int markedWriterIndex = this.markedWriterIndex;
      if (markedWriterIndex <= decrement) {
        this.markedWriterIndex = 0;
      } else {
        this.markedWriterIndex = markedWriterIndex - decrement;
      } 
    } else {
      this.markedReaderIndex = markedReaderIndex - decrement;
      this.markedWriterIndex -= decrement;
    } 
  }
  
  public ByteBuf ensureWritable(int minWritableBytes) {
    if (minWritableBytes < 0)
      throw new IllegalArgumentException(String.format("minWritableBytes: %d (expected: >= 0)", new Object[] { Integer.valueOf(minWritableBytes) })); 
    ensureWritable0(minWritableBytes);
    return this;
  }
  
  final void ensureWritable0(int minWritableBytes) {
    ensureAccessible();
    if (minWritableBytes <= writableBytes())
      return; 
    if (minWritableBytes > this.maxCapacity - this.writerIndex)
      throw new IndexOutOfBoundsException(String.format("writerIndex(%d) + minWritableBytes(%d) exceeds maxCapacity(%d): %s", new Object[] { Integer.valueOf(this.writerIndex), Integer.valueOf(minWritableBytes), Integer.valueOf(this.maxCapacity), this })); 
    int newCapacity = alloc().calculateNewCapacity(this.writerIndex + minWritableBytes, this.maxCapacity);
    capacity(newCapacity);
  }
  
  public int ensureWritable(int minWritableBytes, boolean force) {
    ensureAccessible();
    if (minWritableBytes < 0)
      throw new IllegalArgumentException(String.format("minWritableBytes: %d (expected: >= 0)", new Object[] { Integer.valueOf(minWritableBytes) })); 
    if (minWritableBytes <= writableBytes())
      return 0; 
    int maxCapacity = maxCapacity();
    int writerIndex = writerIndex();
    if (minWritableBytes > maxCapacity - writerIndex) {
      if (!force || capacity() == maxCapacity)
        return 1; 
      capacity(maxCapacity);
      return 3;
    } 
    int newCapacity = alloc().calculateNewCapacity(writerIndex + minWritableBytes, maxCapacity);
    capacity(newCapacity);
    return 2;
  }
  
  public ByteBuf order(ByteOrder endianness) {
    if (endianness == null)
      throw new NullPointerException("endianness"); 
    if (endianness == order())
      return this; 
    return newSwappedByteBuf();
  }
  
  protected SwappedByteBuf newSwappedByteBuf() {
    return new SwappedByteBuf(this);
  }
  
  public byte getByte(int index) {
    checkIndex(index);
    return _getByte(index);
  }
  
  public boolean getBoolean(int index) {
    return (getByte(index) != 0);
  }
  
  public short getUnsignedByte(int index) {
    return (short)(getByte(index) & 0xFF);
  }
  
  public short getShort(int index) {
    checkIndex(index, 2);
    return _getShort(index);
  }
  
  public short getShortLE(int index) {
    checkIndex(index, 2);
    return _getShortLE(index);
  }
  
  public int getUnsignedShort(int index) {
    return getShort(index) & 0xFFFF;
  }
  
  public int getUnsignedShortLE(int index) {
    return getShortLE(index) & 0xFFFF;
  }
  
  public int getUnsignedMedium(int index) {
    checkIndex(index, 3);
    return _getUnsignedMedium(index);
  }
  
  public int getUnsignedMediumLE(int index) {
    checkIndex(index, 3);
    return _getUnsignedMediumLE(index);
  }
  
  public int getMedium(int index) {
    int value = getUnsignedMedium(index);
    if ((value & 0x800000) != 0)
      value |= 0xFF000000; 
    return value;
  }
  
  public int getMediumLE(int index) {
    int value = getUnsignedMediumLE(index);
    if ((value & 0x800000) != 0)
      value |= 0xFF000000; 
    return value;
  }
  
  public int getInt(int index) {
    checkIndex(index, 4);
    return _getInt(index);
  }
  
  public int getIntLE(int index) {
    checkIndex(index, 4);
    return _getIntLE(index);
  }
  
  public long getUnsignedInt(int index) {
    return getInt(index) & 0xFFFFFFFFL;
  }
  
  public long getUnsignedIntLE(int index) {
    return getIntLE(index) & 0xFFFFFFFFL;
  }
  
  public long getLong(int index) {
    checkIndex(index, 8);
    return _getLong(index);
  }
  
  public long getLongLE(int index) {
    checkIndex(index, 8);
    return _getLongLE(index);
  }
  
  public char getChar(int index) {
    return (char)getShort(index);
  }
  
  public float getFloat(int index) {
    return Float.intBitsToFloat(getInt(index));
  }
  
  public double getDouble(int index) {
    return Double.longBitsToDouble(getLong(index));
  }
  
  public ByteBuf getBytes(int index, byte[] dst) {
    getBytes(index, dst, 0, dst.length);
    return this;
  }
  
  public ByteBuf getBytes(int index, ByteBuf dst) {
    getBytes(index, dst, dst.writableBytes());
    return this;
  }
  
  public ByteBuf getBytes(int index, ByteBuf dst, int length) {
    getBytes(index, dst, dst.writerIndex(), length);
    dst.writerIndex(dst.writerIndex() + length);
    return this;
  }
  
  public CharSequence getCharSequence(int index, int length, Charset charset) {
    return toString(index, length, charset);
  }
  
  public CharSequence readCharSequence(int length, Charset charset) {
    CharSequence sequence = getCharSequence(this.readerIndex, length, charset);
    this.readerIndex += length;
    return sequence;
  }
  
  public ByteBuf setByte(int index, int value) {
    checkIndex(index);
    _setByte(index, value);
    return this;
  }
  
  public ByteBuf setBoolean(int index, boolean value) {
    setByte(index, value ? 1 : 0);
    return this;
  }
  
  public ByteBuf setShort(int index, int value) {
    checkIndex(index, 2);
    _setShort(index, value);
    return this;
  }
  
  public ByteBuf setShortLE(int index, int value) {
    checkIndex(index, 2);
    _setShortLE(index, value);
    return this;
  }
  
  public ByteBuf setChar(int index, int value) {
    setShort(index, value);
    return this;
  }
  
  public ByteBuf setMedium(int index, int value) {
    checkIndex(index, 3);
    _setMedium(index, value);
    return this;
  }
  
  public ByteBuf setMediumLE(int index, int value) {
    checkIndex(index, 3);
    _setMediumLE(index, value);
    return this;
  }
  
  public ByteBuf setInt(int index, int value) {
    checkIndex(index, 4);
    _setInt(index, value);
    return this;
  }
  
  public ByteBuf setIntLE(int index, int value) {
    checkIndex(index, 4);
    _setIntLE(index, value);
    return this;
  }
  
  public ByteBuf setFloat(int index, float value) {
    setInt(index, Float.floatToRawIntBits(value));
    return this;
  }
  
  public ByteBuf setLong(int index, long value) {
    checkIndex(index, 8);
    _setLong(index, value);
    return this;
  }
  
  public ByteBuf setLongLE(int index, long value) {
    checkIndex(index, 8);
    _setLongLE(index, value);
    return this;
  }
  
  public ByteBuf setDouble(int index, double value) {
    setLong(index, Double.doubleToRawLongBits(value));
    return this;
  }
  
  public ByteBuf setBytes(int index, byte[] src) {
    setBytes(index, src, 0, src.length);
    return this;
  }
  
  public ByteBuf setBytes(int index, ByteBuf src) {
    setBytes(index, src, src.readableBytes());
    return this;
  }
  
  public ByteBuf setBytes(int index, ByteBuf src, int length) {
    checkIndex(index, length);
    if (src == null)
      throw new NullPointerException("src"); 
    if (length > src.readableBytes())
      throw new IndexOutOfBoundsException(String.format("length(%d) exceeds src.readableBytes(%d) where src is: %s", new Object[] { Integer.valueOf(length), Integer.valueOf(src.readableBytes()), src })); 
    setBytes(index, src, src.readerIndex(), length);
    src.readerIndex(src.readerIndex() + length);
    return this;
  }
  
  public ByteBuf setZero(int index, int length) {
    if (length == 0)
      return this; 
    checkIndex(index, length);
    int nLong = length >>> 3;
    int nBytes = length & 0x7;
    int i;
    for (i = nLong; i > 0; i--) {
      _setLong(index, 0L);
      index += 8;
    } 
    if (nBytes == 4) {
      _setInt(index, 0);
    } else if (nBytes < 4) {
      for (i = nBytes; i > 0; i--) {
        _setByte(index, 0);
        index++;
      } 
    } else {
      _setInt(index, 0);
      index += 4;
      for (i = nBytes - 4; i > 0; i--) {
        _setByte(index, 0);
        index++;
      } 
    } 
    return this;
  }
  
  public int setCharSequence(int index, CharSequence sequence, Charset charset) {
    return setCharSequence0(index, sequence, charset, false);
  }
  
  private int setCharSequence0(int index, CharSequence sequence, Charset charset, boolean expand) {
    if (charset.equals(CharsetUtil.UTF_8)) {
      int length = ByteBufUtil.utf8MaxBytes(sequence);
      if (expand) {
        ensureWritable0(length);
        checkIndex0(index, length);
      } else {
        checkIndex(index, length);
      } 
      return ByteBufUtil.writeUtf8(this, index, sequence, sequence.length());
    } 
    if (charset.equals(CharsetUtil.US_ASCII) || charset.equals(CharsetUtil.ISO_8859_1)) {
      int length = sequence.length();
      if (expand) {
        ensureWritable0(length);
        checkIndex0(index, length);
      } else {
        checkIndex(index, length);
      } 
      return ByteBufUtil.writeAscii(this, index, sequence, length);
    } 
    byte[] bytes = sequence.toString().getBytes(charset);
    if (expand)
      ensureWritable0(bytes.length); 
    setBytes(index, bytes);
    return bytes.length;
  }
  
  public byte readByte() {
    checkReadableBytes0(1);
    int i = this.readerIndex;
    byte b = _getByte(i);
    this.readerIndex = i + 1;
    return b;
  }
  
  public boolean readBoolean() {
    return (readByte() != 0);
  }
  
  public short readUnsignedByte() {
    return (short)(readByte() & 0xFF);
  }
  
  public short readShort() {
    checkReadableBytes0(2);
    short v = _getShort(this.readerIndex);
    this.readerIndex += 2;
    return v;
  }
  
  public short readShortLE() {
    checkReadableBytes0(2);
    short v = _getShortLE(this.readerIndex);
    this.readerIndex += 2;
    return v;
  }
  
  public int readUnsignedShort() {
    return readShort() & 0xFFFF;
  }
  
  public int readUnsignedShortLE() {
    return readShortLE() & 0xFFFF;
  }
  
  public int readMedium() {
    int value = readUnsignedMedium();
    if ((value & 0x800000) != 0)
      value |= 0xFF000000; 
    return value;
  }
  
  public int readMediumLE() {
    int value = readUnsignedMediumLE();
    if ((value & 0x800000) != 0)
      value |= 0xFF000000; 
    return value;
  }
  
  public int readUnsignedMedium() {
    checkReadableBytes0(3);
    int v = _getUnsignedMedium(this.readerIndex);
    this.readerIndex += 3;
    return v;
  }
  
  public int readUnsignedMediumLE() {
    checkReadableBytes0(3);
    int v = _getUnsignedMediumLE(this.readerIndex);
    this.readerIndex += 3;
    return v;
  }
  
  public int readInt() {
    checkReadableBytes0(4);
    int v = _getInt(this.readerIndex);
    this.readerIndex += 4;
    return v;
  }
  
  public int readIntLE() {
    checkReadableBytes0(4);
    int v = _getIntLE(this.readerIndex);
    this.readerIndex += 4;
    return v;
  }
  
  public long readUnsignedInt() {
    return readInt() & 0xFFFFFFFFL;
  }
  
  public long readUnsignedIntLE() {
    return readIntLE() & 0xFFFFFFFFL;
  }
  
  public long readLong() {
    checkReadableBytes0(8);
    long v = _getLong(this.readerIndex);
    this.readerIndex += 8;
    return v;
  }
  
  public long readLongLE() {
    checkReadableBytes0(8);
    long v = _getLongLE(this.readerIndex);
    this.readerIndex += 8;
    return v;
  }
  
  public char readChar() {
    return (char)readShort();
  }
  
  public float readFloat() {
    return Float.intBitsToFloat(readInt());
  }
  
  public double readDouble() {
    return Double.longBitsToDouble(readLong());
  }
  
  public ByteBuf readBytes(int length) {
    checkReadableBytes(length);
    if (length == 0)
      return Unpooled.EMPTY_BUFFER; 
    ByteBuf buf = alloc().buffer(length, this.maxCapacity);
    buf.writeBytes(this, this.readerIndex, length);
    this.readerIndex += length;
    return buf;
  }
  
  public ByteBuf readSlice(int length) {
    checkReadableBytes(length);
    ByteBuf slice = slice(this.readerIndex, length);
    this.readerIndex += length;
    return slice;
  }
  
  public ByteBuf readRetainedSlice(int length) {
    checkReadableBytes(length);
    ByteBuf slice = retainedSlice(this.readerIndex, length);
    this.readerIndex += length;
    return slice;
  }
  
  public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
    checkReadableBytes(length);
    getBytes(this.readerIndex, dst, dstIndex, length);
    this.readerIndex += length;
    return this;
  }
  
  public ByteBuf readBytes(byte[] dst) {
    readBytes(dst, 0, dst.length);
    return this;
  }
  
  public ByteBuf readBytes(ByteBuf dst) {
    readBytes(dst, dst.writableBytes());
    return this;
  }
  
  public ByteBuf readBytes(ByteBuf dst, int length) {
    if (length > dst.writableBytes())
      throw new IndexOutOfBoundsException(String.format("length(%d) exceeds dst.writableBytes(%d) where dst is: %s", new Object[] { Integer.valueOf(length), Integer.valueOf(dst.writableBytes()), dst })); 
    readBytes(dst, dst.writerIndex(), length);
    dst.writerIndex(dst.writerIndex() + length);
    return this;
  }
  
  public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
    checkReadableBytes(length);
    getBytes(this.readerIndex, dst, dstIndex, length);
    this.readerIndex += length;
    return this;
  }
  
  public ByteBuf readBytes(ByteBuffer dst) {
    int length = dst.remaining();
    checkReadableBytes(length);
    getBytes(this.readerIndex, dst);
    this.readerIndex += length;
    return this;
  }
  
  public int readBytes(GatheringByteChannel out, int length) throws IOException {
    checkReadableBytes(length);
    int readBytes = getBytes(this.readerIndex, out, length);
    this.readerIndex += readBytes;
    return readBytes;
  }
  
  public int readBytes(FileChannel out, long position, int length) throws IOException {
    checkReadableBytes(length);
    int readBytes = getBytes(this.readerIndex, out, position, length);
    this.readerIndex += readBytes;
    return readBytes;
  }
  
  public ByteBuf readBytes(OutputStream out, int length) throws IOException {
    checkReadableBytes(length);
    getBytes(this.readerIndex, out, length);
    this.readerIndex += length;
    return this;
  }
  
  public ByteBuf skipBytes(int length) {
    checkReadableBytes(length);
    this.readerIndex += length;
    return this;
  }
  
  public ByteBuf writeBoolean(boolean value) {
    writeByte(value ? 1 : 0);
    return this;
  }
  
  public ByteBuf writeByte(int value) {
    ensureWritable0(1);
    _setByte(this.writerIndex++, value);
    return this;
  }
  
  public ByteBuf writeShort(int value) {
    ensureWritable0(2);
    _setShort(this.writerIndex, value);
    this.writerIndex += 2;
    return this;
  }
  
  public ByteBuf writeShortLE(int value) {
    ensureWritable0(2);
    _setShortLE(this.writerIndex, value);
    this.writerIndex += 2;
    return this;
  }
  
  public ByteBuf writeMedium(int value) {
    ensureWritable0(3);
    _setMedium(this.writerIndex, value);
    this.writerIndex += 3;
    return this;
  }
  
  public ByteBuf writeMediumLE(int value) {
    ensureWritable0(3);
    _setMediumLE(this.writerIndex, value);
    this.writerIndex += 3;
    return this;
  }
  
  public ByteBuf writeInt(int value) {
    ensureWritable0(4);
    _setInt(this.writerIndex, value);
    this.writerIndex += 4;
    return this;
  }
  
  public ByteBuf writeIntLE(int value) {
    ensureWritable0(4);
    _setIntLE(this.writerIndex, value);
    this.writerIndex += 4;
    return this;
  }
  
  public ByteBuf writeLong(long value) {
    ensureWritable0(8);
    _setLong(this.writerIndex, value);
    this.writerIndex += 8;
    return this;
  }
  
  public ByteBuf writeLongLE(long value) {
    ensureWritable0(8);
    _setLongLE(this.writerIndex, value);
    this.writerIndex += 8;
    return this;
  }
  
  public ByteBuf writeChar(int value) {
    writeShort(value);
    return this;
  }
  
  public ByteBuf writeFloat(float value) {
    writeInt(Float.floatToRawIntBits(value));
    return this;
  }
  
  public ByteBuf writeDouble(double value) {
    writeLong(Double.doubleToRawLongBits(value));
    return this;
  }
  
  public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
    ensureWritable(length);
    setBytes(this.writerIndex, src, srcIndex, length);
    this.writerIndex += length;
    return this;
  }
  
  public ByteBuf writeBytes(byte[] src) {
    writeBytes(src, 0, src.length);
    return this;
  }
  
  public ByteBuf writeBytes(ByteBuf src) {
    writeBytes(src, src.readableBytes());
    return this;
  }
  
  public ByteBuf writeBytes(ByteBuf src, int length) {
    if (length > src.readableBytes())
      throw new IndexOutOfBoundsException(String.format("length(%d) exceeds src.readableBytes(%d) where src is: %s", new Object[] { Integer.valueOf(length), Integer.valueOf(src.readableBytes()), src })); 
    writeBytes(src, src.readerIndex(), length);
    src.readerIndex(src.readerIndex() + length);
    return this;
  }
  
  public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
    ensureWritable(length);
    setBytes(this.writerIndex, src, srcIndex, length);
    this.writerIndex += length;
    return this;
  }
  
  public ByteBuf writeBytes(ByteBuffer src) {
    int length = src.remaining();
    ensureWritable0(length);
    setBytes(this.writerIndex, src);
    this.writerIndex += length;
    return this;
  }
  
  public int writeBytes(InputStream in, int length) throws IOException {
    ensureWritable(length);
    int writtenBytes = setBytes(this.writerIndex, in, length);
    if (writtenBytes > 0)
      this.writerIndex += writtenBytes; 
    return writtenBytes;
  }
  
  public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
    ensureWritable(length);
    int writtenBytes = setBytes(this.writerIndex, in, length);
    if (writtenBytes > 0)
      this.writerIndex += writtenBytes; 
    return writtenBytes;
  }
  
  public int writeBytes(FileChannel in, long position, int length) throws IOException {
    ensureWritable(length);
    int writtenBytes = setBytes(this.writerIndex, in, position, length);
    if (writtenBytes > 0)
      this.writerIndex += writtenBytes; 
    return writtenBytes;
  }
  
  public ByteBuf writeZero(int length) {
    if (length == 0)
      return this; 
    ensureWritable(length);
    int wIndex = this.writerIndex;
    checkIndex0(wIndex, length);
    int nLong = length >>> 3;
    int nBytes = length & 0x7;
    int i;
    for (i = nLong; i > 0; i--) {
      _setLong(wIndex, 0L);
      wIndex += 8;
    } 
    if (nBytes == 4) {
      _setInt(wIndex, 0);
      wIndex += 4;
    } else if (nBytes < 4) {
      for (i = nBytes; i > 0; i--) {
        _setByte(wIndex, 0);
        wIndex++;
      } 
    } else {
      _setInt(wIndex, 0);
      wIndex += 4;
      for (i = nBytes - 4; i > 0; i--) {
        _setByte(wIndex, 0);
        wIndex++;
      } 
    } 
    this.writerIndex = wIndex;
    return this;
  }
  
  public int writeCharSequence(CharSequence sequence, Charset charset) {
    int written = setCharSequence0(this.writerIndex, sequence, charset, true);
    this.writerIndex += written;
    return written;
  }
  
  public ByteBuf copy() {
    return copy(this.readerIndex, readableBytes());
  }
  
  public ByteBuf duplicate() {
    ensureAccessible();
    return new UnpooledDuplicatedByteBuf(this);
  }
  
  public ByteBuf retainedDuplicate() {
    return duplicate().retain();
  }
  
  public ByteBuf slice() {
    return slice(this.readerIndex, readableBytes());
  }
  
  public ByteBuf retainedSlice() {
    return slice().retain();
  }
  
  public ByteBuf slice(int index, int length) {
    ensureAccessible();
    return new UnpooledSlicedByteBuf(this, index, length);
  }
  
  public ByteBuf retainedSlice(int index, int length) {
    return slice(index, length).retain();
  }
  
  public ByteBuffer nioBuffer() {
    return nioBuffer(this.readerIndex, readableBytes());
  }
  
  public ByteBuffer[] nioBuffers() {
    return nioBuffers(this.readerIndex, readableBytes());
  }
  
  public String toString(Charset charset) {
    return toString(this.readerIndex, readableBytes(), charset);
  }
  
  public String toString(int index, int length, Charset charset) {
    return ByteBufUtil.decodeString(this, index, length, charset);
  }
  
  public int indexOf(int fromIndex, int toIndex, byte value) {
    return ByteBufUtil.indexOf(this, fromIndex, toIndex, value);
  }
  
  public int bytesBefore(byte value) {
    return bytesBefore(readerIndex(), readableBytes(), value);
  }
  
  public int bytesBefore(int length, byte value) {
    checkReadableBytes(length);
    return bytesBefore(readerIndex(), length, value);
  }
  
  public int bytesBefore(int index, int length, byte value) {
    int endIndex = indexOf(index, index + length, value);
    if (endIndex < 0)
      return -1; 
    return endIndex - index;
  }
  
  public int forEachByte(ByteProcessor processor) {
    ensureAccessible();
    try {
      return forEachByteAsc0(this.readerIndex, this.writerIndex, processor);
    } catch (Exception e) {
      PlatformDependent.throwException(e);
      return -1;
    } 
  }
  
  public int forEachByte(int index, int length, ByteProcessor processor) {
    checkIndex(index, length);
    try {
      return forEachByteAsc0(index, index + length, processor);
    } catch (Exception e) {
      PlatformDependent.throwException(e);
      return -1;
    } 
  }
  
  private int forEachByteAsc0(int start, int end, ByteProcessor processor) throws Exception {
    for (; start < end; start++) {
      if (!processor.process(_getByte(start)))
        return start; 
    } 
    return -1;
  }
  
  public int forEachByteDesc(ByteProcessor processor) {
    ensureAccessible();
    try {
      return forEachByteDesc0(this.writerIndex - 1, this.readerIndex, processor);
    } catch (Exception e) {
      PlatformDependent.throwException(e);
      return -1;
    } 
  }
  
  public int forEachByteDesc(int index, int length, ByteProcessor processor) {
    checkIndex(index, length);
    try {
      return forEachByteDesc0(index + length - 1, index, processor);
    } catch (Exception e) {
      PlatformDependent.throwException(e);
      return -1;
    } 
  }
  
  private int forEachByteDesc0(int rStart, int rEnd, ByteProcessor processor) throws Exception {
    for (; rStart >= rEnd; rStart--) {
      if (!processor.process(_getByte(rStart)))
        return rStart; 
    } 
    return -1;
  }
  
  public int hashCode() {
    return ByteBufUtil.hashCode(this);
  }
  
  public boolean equals(Object o) {
    return (this == o || (o instanceof ByteBuf && ByteBufUtil.equals(this, (ByteBuf)o)));
  }
  
  public int compareTo(ByteBuf that) {
    return ByteBufUtil.compare(this, that);
  }
  
  public String toString() {
    if (refCnt() == 0)
      return StringUtil.simpleClassName(this) + "(freed)"; 
    StringBuilder buf = (new StringBuilder()).append(StringUtil.simpleClassName(this)).append("(ridx: ").append(this.readerIndex).append(", widx: ").append(this.writerIndex).append(", cap: ").append(capacity());
    if (this.maxCapacity != Integer.MAX_VALUE)
      buf.append('/').append(this.maxCapacity); 
    ByteBuf unwrapped = unwrap();
    if (unwrapped != null)
      buf.append(", unwrapped: ").append(unwrapped); 
    buf.append(')');
    return buf.toString();
  }
  
  protected final void checkIndex(int index) {
    checkIndex(index, 1);
  }
  
  protected final void checkIndex(int index, int fieldLength) {
    ensureAccessible();
    checkIndex0(index, fieldLength);
  }
  
  final void checkIndex0(int index, int fieldLength) {
    if (MathUtil.isOutOfBounds(index, fieldLength, capacity()))
      throw new IndexOutOfBoundsException(String.format("index: %d, length: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(index), Integer.valueOf(fieldLength), Integer.valueOf(capacity()) })); 
  }
  
  protected final void checkSrcIndex(int index, int length, int srcIndex, int srcCapacity) {
    checkIndex(index, length);
    if (MathUtil.isOutOfBounds(srcIndex, length, srcCapacity))
      throw new IndexOutOfBoundsException(String.format("srcIndex: %d, length: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(srcIndex), Integer.valueOf(length), Integer.valueOf(srcCapacity) })); 
  }
  
  protected final void checkDstIndex(int index, int length, int dstIndex, int dstCapacity) {
    checkIndex(index, length);
    if (MathUtil.isOutOfBounds(dstIndex, length, dstCapacity))
      throw new IndexOutOfBoundsException(String.format("dstIndex: %d, length: %d (expected: range(0, %d))", new Object[] { Integer.valueOf(dstIndex), Integer.valueOf(length), Integer.valueOf(dstCapacity) })); 
  }
  
  protected final void checkReadableBytes(int minimumReadableBytes) {
    if (minimumReadableBytes < 0)
      throw new IllegalArgumentException("minimumReadableBytes: " + minimumReadableBytes + " (expected: >= 0)"); 
    checkReadableBytes0(minimumReadableBytes);
  }
  
  protected final void checkNewCapacity(int newCapacity) {
    ensureAccessible();
    if (newCapacity < 0 || newCapacity > maxCapacity())
      throw new IllegalArgumentException("newCapacity: " + newCapacity + " (expected: 0-" + maxCapacity() + ')'); 
  }
  
  private void checkReadableBytes0(int minimumReadableBytes) {
    ensureAccessible();
    if (this.readerIndex > this.writerIndex - minimumReadableBytes)
      throw new IndexOutOfBoundsException(String.format("readerIndex(%d) + length(%d) exceeds writerIndex(%d): %s", new Object[] { Integer.valueOf(this.readerIndex), Integer.valueOf(minimumReadableBytes), Integer.valueOf(this.writerIndex), this })); 
  }
  
  protected final void ensureAccessible() {
    if (checkAccessible && refCnt() == 0)
      throw new IllegalReferenceCountException(0); 
  }
  
  final void setIndex0(int readerIndex, int writerIndex) {
    this.readerIndex = readerIndex;
    this.writerIndex = writerIndex;
  }
  
  final void discardMarks() {
    this.markedReaderIndex = this.markedWriterIndex = 0;
  }
  
  protected abstract byte _getByte(int paramInt);
  
  protected abstract short _getShort(int paramInt);
  
  protected abstract short _getShortLE(int paramInt);
  
  protected abstract int _getUnsignedMedium(int paramInt);
  
  protected abstract int _getUnsignedMediumLE(int paramInt);
  
  protected abstract int _getInt(int paramInt);
  
  protected abstract int _getIntLE(int paramInt);
  
  protected abstract long _getLong(int paramInt);
  
  protected abstract long _getLongLE(int paramInt);
  
  protected abstract void _setByte(int paramInt1, int paramInt2);
  
  protected abstract void _setShort(int paramInt1, int paramInt2);
  
  protected abstract void _setShortLE(int paramInt1, int paramInt2);
  
  protected abstract void _setMedium(int paramInt1, int paramInt2);
  
  protected abstract void _setMediumLE(int paramInt1, int paramInt2);
  
  protected abstract void _setInt(int paramInt1, int paramInt2);
  
  protected abstract void _setIntLE(int paramInt1, int paramInt2);
  
  protected abstract void _setLong(int paramInt, long paramLong);
  
  protected abstract void _setLongLE(int paramInt, long paramLong);
}

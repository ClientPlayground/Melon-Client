package com.github.steveice10.packetlib.io.buffer;

import com.github.steveice10.packetlib.io.NetInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class ByteBufferNetInput implements NetInput {
  private ByteBuffer buffer;
  
  public ByteBufferNetInput(ByteBuffer buffer) {
    this.buffer = buffer;
  }
  
  public ByteBuffer getByteBuffer() {
    return this.buffer;
  }
  
  public boolean readBoolean() throws IOException {
    return (this.buffer.get() == 1);
  }
  
  public byte readByte() throws IOException {
    return this.buffer.get();
  }
  
  public int readUnsignedByte() throws IOException {
    return this.buffer.get() & 0xFF;
  }
  
  public short readShort() throws IOException {
    return this.buffer.getShort();
  }
  
  public int readUnsignedShort() throws IOException {
    return this.buffer.getShort() & 0xFFFF;
  }
  
  public char readChar() throws IOException {
    return this.buffer.getChar();
  }
  
  public int readInt() throws IOException {
    return this.buffer.getInt();
  }
  
  public int readVarInt() throws IOException {
    int value = 0;
    int size = 0;
    int b;
    while (((b = readByte()) & 0x80) == 128) {
      value |= (b & 0x7F) << size++ * 7;
      if (size > 5)
        throw new IOException("VarInt too long (length must be <= 5)"); 
    } 
    return value | (b & 0x7F) << size * 7;
  }
  
  public long readLong() throws IOException {
    return this.buffer.getLong();
  }
  
  public long readVarLong() throws IOException {
    long value = 0L;
    int size = 0;
    int b;
    while (((b = readByte()) & 0x80) == 128) {
      value |= (b & 0x7F) << size++ * 7;
      if (size > 10)
        throw new IOException("VarLong too long (length must be <= 10)"); 
    } 
    return value | (b & 0x7F) << size * 7;
  }
  
  public float readFloat() throws IOException {
    return this.buffer.getFloat();
  }
  
  public double readDouble() throws IOException {
    return this.buffer.getDouble();
  }
  
  public byte[] readBytes(int length) throws IOException {
    if (length < 0)
      throw new IllegalArgumentException("Array cannot have length less than 0."); 
    byte[] b = new byte[length];
    this.buffer.get(b);
    return b;
  }
  
  public int readBytes(byte[] b) throws IOException {
    return readBytes(b, 0, b.length);
  }
  
  public int readBytes(byte[] b, int offset, int length) throws IOException {
    int readable = this.buffer.remaining();
    if (readable <= 0)
      return -1; 
    if (readable < length)
      length = readable; 
    this.buffer.get(b, offset, length);
    return length;
  }
  
  public short[] readShorts(int length) throws IOException {
    if (length < 0)
      throw new IllegalArgumentException("Array cannot have length less than 0."); 
    short[] s = new short[length];
    for (int index = 0; index < length; index++)
      s[index] = readShort(); 
    return s;
  }
  
  public int readShorts(short[] s) throws IOException {
    return readShorts(s, 0, s.length);
  }
  
  public int readShorts(short[] s, int offset, int length) throws IOException {
    int readable = this.buffer.remaining();
    if (readable <= 0)
      return -1; 
    if (readable < length * 2)
      length = readable / 2; 
    for (int index = offset; index < offset + length; index++)
      s[index] = readShort(); 
    return length;
  }
  
  public int[] readInts(int length) throws IOException {
    if (length < 0)
      throw new IllegalArgumentException("Array cannot have length less than 0."); 
    int[] i = new int[length];
    for (int index = 0; index < length; index++)
      i[index] = readInt(); 
    return i;
  }
  
  public int readInts(int[] i) throws IOException {
    return readInts(i, 0, i.length);
  }
  
  public int readInts(int[] i, int offset, int length) throws IOException {
    int readable = this.buffer.remaining();
    if (readable <= 0)
      return -1; 
    if (readable < length * 4)
      length = readable / 4; 
    for (int index = offset; index < offset + length; index++)
      i[index] = readInt(); 
    return length;
  }
  
  public long[] readLongs(int length) throws IOException {
    if (length < 0)
      throw new IllegalArgumentException("Array cannot have length less than 0."); 
    long[] l = new long[length];
    for (int index = 0; index < length; index++)
      l[index] = readLong(); 
    return l;
  }
  
  public int readLongs(long[] l) throws IOException {
    return readLongs(l, 0, l.length);
  }
  
  public int readLongs(long[] l, int offset, int length) throws IOException {
    int readable = this.buffer.remaining();
    if (readable <= 0)
      return -1; 
    if (readable < length * 2)
      length = readable / 2; 
    for (int index = offset; index < offset + length; index++)
      l[index] = readLong(); 
    return length;
  }
  
  public String readString() throws IOException {
    int length = readVarInt();
    byte[] bytes = readBytes(length);
    return new String(bytes, "UTF-8");
  }
  
  public UUID readUUID() throws IOException {
    return new UUID(readLong(), readLong());
  }
  
  public int available() throws IOException {
    return this.buffer.remaining();
  }
}

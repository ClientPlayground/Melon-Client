package com.github.steveice10.packetlib.io.stream;

import com.github.steveice10.packetlib.io.NetInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class StreamNetInput implements NetInput {
  private InputStream in;
  
  public StreamNetInput(InputStream in) {
    this.in = in;
  }
  
  public boolean readBoolean() throws IOException {
    return (readByte() == 1);
  }
  
  public byte readByte() throws IOException {
    return (byte)readUnsignedByte();
  }
  
  public int readUnsignedByte() throws IOException {
    int b = this.in.read();
    if (b < 0)
      throw new EOFException(); 
    return b;
  }
  
  public short readShort() throws IOException {
    return (short)readUnsignedShort();
  }
  
  public int readUnsignedShort() throws IOException {
    int ch1 = readUnsignedByte();
    int ch2 = readUnsignedByte();
    return (ch1 << 8) + (ch2 << 0);
  }
  
  public char readChar() throws IOException {
    int ch1 = readUnsignedByte();
    int ch2 = readUnsignedByte();
    return (char)((ch1 << 8) + (ch2 << 0));
  }
  
  public int readInt() throws IOException {
    int ch1 = readUnsignedByte();
    int ch2 = readUnsignedByte();
    int ch3 = readUnsignedByte();
    int ch4 = readUnsignedByte();
    return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
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
    byte[] read = readBytes(8);
    return (read[0] << 56L) + ((read[1] & 0xFF) << 48L) + ((read[2] & 0xFF) << 40L) + ((read[3] & 0xFF) << 32L) + ((read[4] & 0xFF) << 24L) + ((read[5] & 0xFF) << 16) + ((read[6] & 0xFF) << 8) + ((read[7] & 0xFF) << 0);
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
    return Float.intBitsToFloat(readInt());
  }
  
  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }
  
  public byte[] readBytes(int length) throws IOException {
    if (length < 0)
      throw new IllegalArgumentException("Array cannot have length less than 0."); 
    byte[] b = new byte[length];
    int n = 0;
    while (n < length) {
      int count = this.in.read(b, n, length - n);
      if (count < 0)
        throw new EOFException(); 
      n += count;
    } 
    return b;
  }
  
  public int readBytes(byte[] b) throws IOException {
    return this.in.read(b);
  }
  
  public int readBytes(byte[] b, int offset, int length) throws IOException {
    return this.in.read(b, offset, length);
  }
  
  public short[] readShorts(int length) throws IOException {
    if (length < 0)
      throw new IllegalArgumentException("Array cannot have length less than 0."); 
    short[] s = new short[length];
    int read = readShorts(s);
    if (read < length)
      throw new EOFException(); 
    return s;
  }
  
  public int readShorts(short[] s) throws IOException {
    return readShorts(s, 0, s.length);
  }
  
  public int readShorts(short[] s, int offset, int length) throws IOException {
    for (int index = offset; index < offset + length; index++) {
      try {
        s[index] = readShort();
      } catch (EOFException e) {
        return index - offset;
      } 
    } 
    return length;
  }
  
  public int[] readInts(int length) throws IOException {
    if (length < 0)
      throw new IllegalArgumentException("Array cannot have length less than 0."); 
    int[] i = new int[length];
    int read = readInts(i);
    if (read < length)
      throw new EOFException(); 
    return i;
  }
  
  public int readInts(int[] i) throws IOException {
    return readInts(i, 0, i.length);
  }
  
  public int readInts(int[] i, int offset, int length) throws IOException {
    for (int index = offset; index < offset + length; index++) {
      try {
        i[index] = readInt();
      } catch (EOFException e) {
        return index - offset;
      } 
    } 
    return length;
  }
  
  public long[] readLongs(int length) throws IOException {
    if (length < 0)
      throw new IllegalArgumentException("Array cannot have length less than 0."); 
    long[] l = new long[length];
    int read = readLongs(l);
    if (read < length)
      throw new EOFException(); 
    return l;
  }
  
  public int readLongs(long[] l) throws IOException {
    return readLongs(l, 0, l.length);
  }
  
  public int readLongs(long[] l, int offset, int length) throws IOException {
    for (int index = offset; index < offset + length; index++) {
      try {
        l[index] = readLong();
      } catch (EOFException e) {
        return index - offset;
      } 
    } 
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
    return this.in.available();
  }
}

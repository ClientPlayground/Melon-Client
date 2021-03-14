package com.github.steveice10.packetlib.io.stream;

import com.github.steveice10.packetlib.io.NetOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class StreamNetOutput implements NetOutput {
  private OutputStream out;
  
  public StreamNetOutput(OutputStream out) {
    this.out = out;
  }
  
  public void writeBoolean(boolean b) throws IOException {
    writeByte(b ? 1 : 0);
  }
  
  public void writeByte(int b) throws IOException {
    this.out.write(b);
  }
  
  public void writeShort(int s) throws IOException {
    writeByte((byte)(s >>> 8 & 0xFF));
    writeByte((byte)(s >>> 0 & 0xFF));
  }
  
  public void writeChar(int c) throws IOException {
    writeByte((byte)(c >>> 8 & 0xFF));
    writeByte((byte)(c >>> 0 & 0xFF));
  }
  
  public void writeInt(int i) throws IOException {
    writeByte((byte)(i >>> 24 & 0xFF));
    writeByte((byte)(i >>> 16 & 0xFF));
    writeByte((byte)(i >>> 8 & 0xFF));
    writeByte((byte)(i >>> 0 & 0xFF));
  }
  
  public void writeVarInt(int i) throws IOException {
    while ((i & 0xFFFFFF80) != 0) {
      writeByte(i & 0x7F | 0x80);
      i >>>= 7;
    } 
    writeByte(i);
  }
  
  public void writeLong(long l) throws IOException {
    writeByte((byte)(int)(l >>> 56L));
    writeByte((byte)(int)(l >>> 48L));
    writeByte((byte)(int)(l >>> 40L));
    writeByte((byte)(int)(l >>> 32L));
    writeByte((byte)(int)(l >>> 24L));
    writeByte((byte)(int)(l >>> 16L));
    writeByte((byte)(int)(l >>> 8L));
    writeByte((byte)(int)(l >>> 0L));
  }
  
  public void writeVarLong(long l) throws IOException {
    while ((l & 0xFFFFFFFFFFFFFF80L) != 0L) {
      writeByte((int)(l & 0x7FL) | 0x80);
      l >>>= 7L;
    } 
    writeByte((int)l);
  }
  
  public void writeFloat(float f) throws IOException {
    writeInt(Float.floatToIntBits(f));
  }
  
  public void writeDouble(double d) throws IOException {
    writeLong(Double.doubleToLongBits(d));
  }
  
  public void writeBytes(byte[] b) throws IOException {
    writeBytes(b, b.length);
  }
  
  public void writeBytes(byte[] b, int length) throws IOException {
    this.out.write(b, 0, length);
  }
  
  public void writeShorts(short[] s) throws IOException {
    writeShorts(s, s.length);
  }
  
  public void writeShorts(short[] s, int length) throws IOException {
    for (int index = 0; index < length; index++)
      writeShort(s[index]); 
  }
  
  public void writeInts(int[] i) throws IOException {
    writeInts(i, i.length);
  }
  
  public void writeInts(int[] i, int length) throws IOException {
    for (int index = 0; index < length; index++)
      writeInt(i[index]); 
  }
  
  public void writeLongs(long[] l) throws IOException {
    writeLongs(l, l.length);
  }
  
  public void writeLongs(long[] l, int length) throws IOException {
    for (int index = 0; index < length; index++)
      writeLong(l[index]); 
  }
  
  public void writeString(String s) throws IOException {
    if (s == null)
      throw new IllegalArgumentException("String cannot be null!"); 
    byte[] bytes = s.getBytes("UTF-8");
    if (bytes.length > 32767)
      throw new IOException("String too big (was " + s.length() + " bytes encoded, max " + '翿' + ")"); 
    writeVarInt(bytes.length);
    writeBytes(bytes);
  }
  
  public void writeUUID(UUID uuid) throws IOException {
    writeLong(uuid.getMostSignificantBits());
    writeLong(uuid.getLeastSignificantBits());
  }
  
  public void flush() throws IOException {
    this.out.flush();
  }
}

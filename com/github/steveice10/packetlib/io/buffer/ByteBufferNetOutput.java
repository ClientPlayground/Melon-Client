package com.github.steveice10.packetlib.io.buffer;

import com.github.steveice10.packetlib.io.NetOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class ByteBufferNetOutput implements NetOutput {
  private ByteBuffer buffer;
  
  public ByteBufferNetOutput(ByteBuffer buffer) {
    this.buffer = buffer;
  }
  
  public ByteBuffer getByteBuffer() {
    return this.buffer;
  }
  
  public void writeBoolean(boolean b) throws IOException {
    this.buffer.put(b ? 1 : 0);
  }
  
  public void writeByte(int b) throws IOException {
    this.buffer.put((byte)b);
  }
  
  public void writeShort(int s) throws IOException {
    this.buffer.putShort((short)s);
  }
  
  public void writeChar(int c) throws IOException {
    this.buffer.putChar((char)c);
  }
  
  public void writeInt(int i) throws IOException {
    this.buffer.putInt(i);
  }
  
  public void writeVarInt(int i) throws IOException {
    while ((i & 0xFFFFFF80) != 0) {
      writeByte(i & 0x7F | 0x80);
      i >>>= 7;
    } 
    writeByte(i);
  }
  
  public void writeLong(long l) throws IOException {
    this.buffer.putLong(l);
  }
  
  public void writeVarLong(long l) throws IOException {
    while ((l & 0xFFFFFFFFFFFFFF80L) != 0L) {
      writeByte((int)(l & 0x7FL) | 0x80);
      l >>>= 7L;
    } 
    writeByte((int)l);
  }
  
  public void writeFloat(float f) throws IOException {
    this.buffer.putFloat(f);
  }
  
  public void writeDouble(double d) throws IOException {
    this.buffer.putDouble(d);
  }
  
  public void writeBytes(byte[] b) throws IOException {
    this.buffer.put(b);
  }
  
  public void writeBytes(byte[] b, int length) throws IOException {
    this.buffer.put(b, 0, length);
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
  
  public void flush() throws IOException {}
}

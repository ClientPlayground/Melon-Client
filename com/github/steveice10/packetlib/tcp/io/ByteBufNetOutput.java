package com.github.steveice10.packetlib.tcp.io;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.packetlib.io.NetOutput;
import java.io.IOException;
import java.util.UUID;

public class ByteBufNetOutput implements NetOutput {
  private ByteBuf buf;
  
  public ByteBufNetOutput(ByteBuf buf) {
    this.buf = buf;
  }
  
  public void writeBoolean(boolean b) throws IOException {
    this.buf.writeBoolean(b);
  }
  
  public void writeByte(int b) throws IOException {
    this.buf.writeByte(b);
  }
  
  public void writeShort(int s) throws IOException {
    this.buf.writeShort(s);
  }
  
  public void writeChar(int c) throws IOException {
    this.buf.writeChar(c);
  }
  
  public void writeInt(int i) throws IOException {
    this.buf.writeInt(i);
  }
  
  public void writeVarInt(int i) throws IOException {
    while ((i & 0xFFFFFF80) != 0) {
      writeByte(i & 0x7F | 0x80);
      i >>>= 7;
    } 
    writeByte(i);
  }
  
  public void writeLong(long l) throws IOException {
    this.buf.writeLong(l);
  }
  
  public void writeVarLong(long l) throws IOException {
    while ((l & 0xFFFFFFFFFFFFFF80L) != 0L) {
      writeByte((int)(l & 0x7FL) | 0x80);
      l >>>= 7L;
    } 
    writeByte((int)l);
  }
  
  public void writeFloat(float f) throws IOException {
    this.buf.writeFloat(f);
  }
  
  public void writeDouble(double d) throws IOException {
    this.buf.writeDouble(d);
  }
  
  public void writeBytes(byte[] b) throws IOException {
    this.buf.writeBytes(b);
  }
  
  public void writeBytes(byte[] b, int length) throws IOException {
    this.buf.writeBytes(b, 0, length);
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

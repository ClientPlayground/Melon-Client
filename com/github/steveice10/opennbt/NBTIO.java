package com.github.steveice10.opennbt;

import com.github.steveice10.opennbt.tag.TagCreateException;
import com.github.steveice10.opennbt.tag.TagRegistry;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NBTIO {
  public static CompoundTag readFile(String path) throws IOException {
    return readFile(new File(path));
  }
  
  public static CompoundTag readFile(File file) throws IOException {
    return readFile(file, true, false);
  }
  
  public static CompoundTag readFile(String path, boolean compressed, boolean littleEndian) throws IOException {
    return readFile(new File(path), compressed, littleEndian);
  }
  
  public static CompoundTag readFile(File file, boolean compressed, boolean littleEndian) throws IOException {
    InputStream in = new FileInputStream(file);
    if (compressed)
      in = new GZIPInputStream(in); 
    Tag tag = readTag(in, littleEndian);
    if (!(tag instanceof CompoundTag))
      throw new IOException("Root tag is not a CompoundTag!"); 
    return (CompoundTag)tag;
  }
  
  public static void writeFile(CompoundTag tag, String path) throws IOException {
    writeFile(tag, new File(path));
  }
  
  public static void writeFile(CompoundTag tag, File file) throws IOException {
    writeFile(tag, file, true, false);
  }
  
  public static void writeFile(CompoundTag tag, String path, boolean compressed, boolean littleEndian) throws IOException {
    writeFile(tag, new File(path), compressed, littleEndian);
  }
  
  public static void writeFile(CompoundTag tag, File file, boolean compressed, boolean littleEndian) throws IOException {
    if (!file.exists()) {
      if (file.getParentFile() != null && !file.getParentFile().exists())
        file.getParentFile().mkdirs(); 
      file.createNewFile();
    } 
    OutputStream out = new FileOutputStream(file);
    if (compressed)
      out = new GZIPOutputStream(out); 
    writeTag(out, (Tag)tag, littleEndian);
    out.close();
  }
  
  public static Tag readTag(InputStream in) throws IOException {
    return readTag(in, false);
  }
  
  public static Tag readTag(InputStream in, boolean littleEndian) throws IOException {
    return readTag(littleEndian ? new LittleEndianDataInputStream(in) : new DataInputStream(in));
  }
  
  public static Tag readTag(DataInput in) throws IOException {
    Tag tag;
    int id = in.readUnsignedByte();
    if (id == 0)
      return null; 
    String name = in.readUTF();
    try {
      tag = TagRegistry.createInstance(id, name);
    } catch (TagCreateException e) {
      throw new IOException("Failed to create tag.", e);
    } 
    tag.read(in);
    return tag;
  }
  
  public static void writeTag(OutputStream out, Tag tag) throws IOException {
    writeTag(out, tag, false);
  }
  
  public static void writeTag(OutputStream out, Tag tag, boolean littleEndian) throws IOException {
    writeTag(littleEndian ? new LittleEndianDataOutputStream(out) : new DataOutputStream(out), tag);
  }
  
  public static void writeTag(DataOutput out, Tag tag) throws IOException {
    out.writeByte(TagRegistry.getIdFor(tag.getClass()));
    out.writeUTF(tag.getName());
    tag.write(out);
  }
  
  private static class LittleEndianDataInputStream extends FilterInputStream implements DataInput {
    public LittleEndianDataInputStream(InputStream in) {
      super(in);
    }
    
    public int read(byte[] b) throws IOException {
      return this.in.read(b, 0, b.length);
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
      return this.in.read(b, off, len);
    }
    
    public void readFully(byte[] b) throws IOException {
      readFully(b, 0, b.length);
    }
    
    public void readFully(byte[] b, int off, int len) throws IOException {
      if (len < 0)
        throw new IndexOutOfBoundsException(); 
      int pos;
      for (pos = 0; pos < len; pos += read) {
        int read = this.in.read(b, off + pos, len - pos);
        if (read < 0)
          throw new EOFException(); 
      } 
    }
    
    public int skipBytes(int n) throws IOException {
      int total = 0;
      int skipped = 0;
      while (total < n && (skipped = (int)this.in.skip((n - total))) > 0)
        total += skipped; 
      return total;
    }
    
    public boolean readBoolean() throws IOException {
      int val = this.in.read();
      if (val < 0)
        throw new EOFException(); 
      return (val != 0);
    }
    
    public byte readByte() throws IOException {
      int val = this.in.read();
      if (val < 0)
        throw new EOFException(); 
      return (byte)val;
    }
    
    public int readUnsignedByte() throws IOException {
      int val = this.in.read();
      if (val < 0)
        throw new EOFException(); 
      return val;
    }
    
    public short readShort() throws IOException {
      int b1 = this.in.read();
      int b2 = this.in.read();
      if ((b1 | b2) < 0)
        throw new EOFException(); 
      return (short)(b1 | b2 << 8);
    }
    
    public int readUnsignedShort() throws IOException {
      int b1 = this.in.read();
      int b2 = this.in.read();
      if ((b1 | b2) < 0)
        throw new EOFException(); 
      return b1 | b2 << 8;
    }
    
    public char readChar() throws IOException {
      int b1 = this.in.read();
      int b2 = this.in.read();
      if ((b1 | b2) < 0)
        throw new EOFException(); 
      return (char)(b1 | b2 << 8);
    }
    
    public int readInt() throws IOException {
      int b1 = this.in.read();
      int b2 = this.in.read();
      int b3 = this.in.read();
      int b4 = this.in.read();
      if ((b1 | b2 | b3 | b4) < 0)
        throw new EOFException(); 
      return b1 | b2 << 8 | b3 << 16 | b4 << 24;
    }
    
    public long readLong() throws IOException {
      long b1 = this.in.read();
      long b2 = this.in.read();
      long b3 = this.in.read();
      long b4 = this.in.read();
      long b5 = this.in.read();
      long b6 = this.in.read();
      long b7 = this.in.read();
      long b8 = this.in.read();
      if ((b1 | b2 | b3 | b4 | b5 | b6 | b7 | b8) < 0L)
        throw new EOFException(); 
      return b1 | b2 << 8L | b3 << 16L | b4 << 24L | b5 << 32L | b6 << 40L | b7 << 48L | b8 << 56L;
    }
    
    public float readFloat() throws IOException {
      return Float.intBitsToFloat(readInt());
    }
    
    public double readDouble() throws IOException {
      return Double.longBitsToDouble(readLong());
    }
    
    public String readLine() throws IOException {
      throw new UnsupportedOperationException("Use readUTF.");
    }
    
    public String readUTF() throws IOException {
      byte[] bytes = new byte[readUnsignedShort()];
      readFully(bytes);
      return new String(bytes, "UTF-8");
    }
  }
  
  private static class LittleEndianDataOutputStream extends FilterOutputStream implements DataOutput {
    public LittleEndianDataOutputStream(OutputStream out) {
      super(out);
    }
    
    public synchronized void write(int b) throws IOException {
      this.out.write(b);
    }
    
    public synchronized void write(byte[] b, int off, int len) throws IOException {
      this.out.write(b, off, len);
    }
    
    public void flush() throws IOException {
      this.out.flush();
    }
    
    public void writeBoolean(boolean b) throws IOException {
      this.out.write(b ? 1 : 0);
    }
    
    public void writeByte(int b) throws IOException {
      this.out.write(b);
    }
    
    public void writeShort(int s) throws IOException {
      this.out.write(s & 0xFF);
      this.out.write(s >>> 8 & 0xFF);
    }
    
    public void writeChar(int c) throws IOException {
      this.out.write(c & 0xFF);
      this.out.write(c >>> 8 & 0xFF);
    }
    
    public void writeInt(int i) throws IOException {
      this.out.write(i & 0xFF);
      this.out.write(i >>> 8 & 0xFF);
      this.out.write(i >>> 16 & 0xFF);
      this.out.write(i >>> 24 & 0xFF);
    }
    
    public void writeLong(long l) throws IOException {
      this.out.write((int)(l & 0xFFL));
      this.out.write((int)(l >>> 8L & 0xFFL));
      this.out.write((int)(l >>> 16L & 0xFFL));
      this.out.write((int)(l >>> 24L & 0xFFL));
      this.out.write((int)(l >>> 32L & 0xFFL));
      this.out.write((int)(l >>> 40L & 0xFFL));
      this.out.write((int)(l >>> 48L & 0xFFL));
      this.out.write((int)(l >>> 56L & 0xFFL));
    }
    
    public void writeFloat(float f) throws IOException {
      writeInt(Float.floatToIntBits(f));
    }
    
    public void writeDouble(double d) throws IOException {
      writeLong(Double.doubleToLongBits(d));
    }
    
    public void writeBytes(String s) throws IOException {
      int len = s.length();
      for (int index = 0; index < len; index++)
        this.out.write((byte)s.charAt(index)); 
    }
    
    public void writeChars(String s) throws IOException {
      int len = s.length();
      for (int index = 0; index < len; index++) {
        char c = s.charAt(index);
        this.out.write(c & 0xFF);
        this.out.write(c >>> 8 & 0xFF);
      } 
    }
    
    public void writeUTF(String s) throws IOException {
      byte[] bytes = s.getBytes("UTF-8");
      writeShort(bytes.length);
      write(bytes);
    }
  }
}

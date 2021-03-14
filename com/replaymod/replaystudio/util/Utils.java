package com.replaymod.replaystudio.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
  public static int readInt(InputStream in) throws IOException {
    int b0 = in.read();
    int b1 = in.read();
    int b2 = in.read();
    int b3 = in.read();
    if ((b0 | b1 | b2 | b3) < 0)
      return -1; 
    return b0 << 24 | b1 << 16 | b2 << 8 | b3;
  }
  
  public static void writeInt(OutputStream out, int x) throws IOException {
    out.write(x >>> 24 & 0xFF);
    out.write(x >>> 16 & 0xFF);
    out.write(x >>> 8 & 0xFF);
    out.write(x & 0xFF);
  }
  
  public static boolean containsOnlyNull(Object[] array) {
    for (Object o : array) {
      if (o != null)
        return false; 
    } 
    return true;
  }
  
  public static long within(long i, long min, long max) {
    if (i > max)
      return max; 
    if (i < min)
      return min; 
    return i;
  }
  
  public static InputStream notCloseable(final InputStream source) {
    return new InputStream() {
        boolean closed;
        
        public void close() throws IOException {
          this.closed = true;
        }
        
        public int read() throws IOException {
          if (this.closed)
            return -1; 
          return source.read();
        }
        
        public int read(byte[] b, int off, int len) throws IOException {
          if (this.closed)
            return -1; 
          return source.read(b, off, len);
        }
        
        public int available() throws IOException {
          return source.available();
        }
        
        public long skip(long n) throws IOException {
          if (this.closed)
            return 0L; 
          return source.skip(n);
        }
        
        public synchronized void mark(int readlimit) {
          source.mark(readlimit);
        }
        
        public synchronized void reset() throws IOException {
          source.reset();
        }
        
        public boolean markSupported() {
          return source.markSupported();
        }
        
        public int read(byte[] b) throws IOException {
          if (this.closed)
            return -1; 
          return source.read(b);
        }
      };
  }
  
  public static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;
    while ((read = in.read(buffer)) > -1)
      out.write(buffer, 0, read); 
    in.close();
  }
}

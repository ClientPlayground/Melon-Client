package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.util.ByteProcessor;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

abstract class ByteBufChecksum implements Checksum {
  private static final Method ADLER32_UPDATE_METHOD = updateByteBuffer(new Adler32());
  
  private static final Method CRC32_UPDATE_METHOD = updateByteBuffer(new CRC32());
  
  private final ByteProcessor updateProcessor = new ByteProcessor() {
      public boolean process(byte value) throws Exception {
        ByteBufChecksum.this.update(value);
        return true;
      }
    };
  
  private static Method updateByteBuffer(Checksum checksum) {
    if (PlatformDependent.javaVersion() >= 8)
      try {
        Method method = checksum.getClass().getDeclaredMethod("update", new Class[] { ByteBuffer.class });
        method.invoke(method, new Object[] { ByteBuffer.allocate(1) });
        return method;
      } catch (Throwable ignore) {
        return null;
      }  
    return null;
  }
  
  static ByteBufChecksum wrapChecksum(Checksum checksum) {
    ObjectUtil.checkNotNull(checksum, "checksum");
    if (checksum instanceof Adler32 && ADLER32_UPDATE_METHOD != null)
      return new ReflectiveByteBufChecksum(checksum, ADLER32_UPDATE_METHOD); 
    if (checksum instanceof CRC32 && CRC32_UPDATE_METHOD != null)
      return new ReflectiveByteBufChecksum(checksum, CRC32_UPDATE_METHOD); 
    return new SlowByteBufChecksum(checksum);
  }
  
  public void update(ByteBuf b, int off, int len) {
    if (b.hasArray()) {
      update(b.array(), b.arrayOffset() + off, len);
    } else {
      b.forEachByte(off, len, this.updateProcessor);
    } 
  }
  
  private static final class ReflectiveByteBufChecksum extends SlowByteBufChecksum {
    private final Method method;
    
    ReflectiveByteBufChecksum(Checksum checksum, Method method) {
      super(checksum);
      this.method = method;
    }
    
    public void update(ByteBuf b, int off, int len) {
      if (b.hasArray()) {
        update(b.array(), b.arrayOffset() + off, len);
      } else {
        try {
          this.method.invoke(this.checksum, new Object[] { CompressionUtil.safeNioBuffer(b) });
        } catch (Throwable cause) {
          throw new Error();
        } 
      } 
    }
  }
  
  private static class SlowByteBufChecksum extends ByteBufChecksum {
    protected final Checksum checksum;
    
    SlowByteBufChecksum(Checksum checksum) {
      this.checksum = checksum;
    }
    
    public void update(int b) {
      this.checksum.update(b);
    }
    
    public void update(byte[] b, int off, int len) {
      this.checksum.update(b, off, len);
    }
    
    public long getValue() {
      return this.checksum.getValue();
    }
    
    public void reset() {
      this.checksum.reset();
    }
  }
}

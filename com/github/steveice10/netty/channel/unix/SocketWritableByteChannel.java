package com.github.steveice10.netty.channel.unix;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public abstract class SocketWritableByteChannel implements WritableByteChannel {
  private final FileDescriptor fd;
  
  protected SocketWritableByteChannel(FileDescriptor fd) {
    this.fd = (FileDescriptor)ObjectUtil.checkNotNull(fd, "fd");
  }
  
  public final int write(ByteBuffer src) throws IOException {
    int written, position = src.position();
    int limit = src.limit();
    if (src.isDirect()) {
      written = this.fd.write(src, position, src.limit());
    } else {
      int readableBytes = limit - position;
      ByteBuf buffer = null;
      try {
        if (readableBytes == 0) {
          buffer = Unpooled.EMPTY_BUFFER;
        } else {
          ByteBufAllocator alloc = alloc();
          if (alloc.isDirectBufferPooled()) {
            buffer = alloc.directBuffer(readableBytes);
          } else {
            buffer = ByteBufUtil.threadLocalDirectBuffer();
            if (buffer == null)
              buffer = Unpooled.directBuffer(readableBytes); 
          } 
        } 
        buffer.writeBytes(src.duplicate());
        ByteBuffer nioBuffer = buffer.internalNioBuffer(buffer.readerIndex(), readableBytes);
        written = this.fd.write(nioBuffer, nioBuffer.position(), nioBuffer.limit());
      } finally {
        if (buffer != null)
          buffer.release(); 
      } 
    } 
    if (written > 0)
      src.position(position + written); 
    return written;
  }
  
  public final boolean isOpen() {
    return this.fd.isOpen();
  }
  
  public final void close() throws IOException {
    this.fd.close();
  }
  
  protected abstract ByteBufAllocator alloc();
}

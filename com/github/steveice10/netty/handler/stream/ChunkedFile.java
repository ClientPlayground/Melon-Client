package com.github.steveice10.netty.handler.stream;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ChunkedFile implements ChunkedInput<ByteBuf> {
  private final RandomAccessFile file;
  
  private final long startOffset;
  
  private final long endOffset;
  
  private final int chunkSize;
  
  private long offset;
  
  public ChunkedFile(File file) throws IOException {
    this(file, 8192);
  }
  
  public ChunkedFile(File file, int chunkSize) throws IOException {
    this(new RandomAccessFile(file, "r"), chunkSize);
  }
  
  public ChunkedFile(RandomAccessFile file) throws IOException {
    this(file, 8192);
  }
  
  public ChunkedFile(RandomAccessFile file, int chunkSize) throws IOException {
    this(file, 0L, file.length(), chunkSize);
  }
  
  public ChunkedFile(RandomAccessFile file, long offset, long length, int chunkSize) throws IOException {
    if (file == null)
      throw new NullPointerException("file"); 
    if (offset < 0L)
      throw new IllegalArgumentException("offset: " + offset + " (expected: 0 or greater)"); 
    if (length < 0L)
      throw new IllegalArgumentException("length: " + length + " (expected: 0 or greater)"); 
    if (chunkSize <= 0)
      throw new IllegalArgumentException("chunkSize: " + chunkSize + " (expected: a positive integer)"); 
    this.file = file;
    this.offset = this.startOffset = offset;
    this.endOffset = offset + length;
    this.chunkSize = chunkSize;
    file.seek(offset);
  }
  
  public long startOffset() {
    return this.startOffset;
  }
  
  public long endOffset() {
    return this.endOffset;
  }
  
  public long currentOffset() {
    return this.offset;
  }
  
  public boolean isEndOfInput() throws Exception {
    return (this.offset >= this.endOffset || !this.file.getChannel().isOpen());
  }
  
  public void close() throws Exception {
    this.file.close();
  }
  
  @Deprecated
  public ByteBuf readChunk(ChannelHandlerContext ctx) throws Exception {
    return readChunk(ctx.alloc());
  }
  
  public ByteBuf readChunk(ByteBufAllocator allocator) throws Exception {
    long offset = this.offset;
    if (offset >= this.endOffset)
      return null; 
    int chunkSize = (int)Math.min(this.chunkSize, this.endOffset - offset);
    ByteBuf buf = allocator.heapBuffer(chunkSize);
    boolean release = true;
    try {
      this.file.readFully(buf.array(), buf.arrayOffset(), chunkSize);
      buf.writerIndex(chunkSize);
      this.offset = offset + chunkSize;
      release = false;
      return buf;
    } finally {
      if (release)
        buf.release(); 
    } 
  }
  
  public long length() {
    return this.endOffset - this.startOffset;
  }
  
  public long progress() {
    return this.offset - this.startOffset;
  }
}

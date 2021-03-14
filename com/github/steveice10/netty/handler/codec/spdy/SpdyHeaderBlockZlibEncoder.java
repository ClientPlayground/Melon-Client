package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.Unpooled;
import java.util.zip.Deflater;

class SpdyHeaderBlockZlibEncoder extends SpdyHeaderBlockRawEncoder {
  private final Deflater compressor;
  
  private boolean finished;
  
  SpdyHeaderBlockZlibEncoder(SpdyVersion spdyVersion, int compressionLevel) {
    super(spdyVersion);
    if (compressionLevel < 0 || compressionLevel > 9)
      throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)"); 
    this.compressor = new Deflater(compressionLevel);
    this.compressor.setDictionary(SpdyCodecUtil.SPDY_DICT);
  }
  
  private int setInput(ByteBuf decompressed) {
    int len = decompressed.readableBytes();
    if (decompressed.hasArray()) {
      this.compressor.setInput(decompressed.array(), decompressed.arrayOffset() + decompressed.readerIndex(), len);
    } else {
      byte[] in = new byte[len];
      decompressed.getBytes(decompressed.readerIndex(), in);
      this.compressor.setInput(in, 0, in.length);
    } 
    return len;
  }
  
  private ByteBuf encode(ByteBufAllocator alloc, int len) {
    ByteBuf compressed = alloc.heapBuffer(len);
    boolean release = true;
    try {
      while (compressInto(compressed))
        compressed.ensureWritable(compressed.capacity() << 1); 
      release = false;
      return compressed;
    } finally {
      if (release)
        compressed.release(); 
    } 
  }
  
  private boolean compressInto(ByteBuf compressed) {
    byte[] out = compressed.array();
    int off = compressed.arrayOffset() + compressed.writerIndex();
    int toWrite = compressed.writableBytes();
    int numBytes = this.compressor.deflate(out, off, toWrite, 2);
    compressed.writerIndex(compressed.writerIndex() + numBytes);
    return (numBytes == toWrite);
  }
  
  public ByteBuf encode(ByteBufAllocator alloc, SpdyHeadersFrame frame) throws Exception {
    if (frame == null)
      throw new IllegalArgumentException("frame"); 
    if (this.finished)
      return Unpooled.EMPTY_BUFFER; 
    ByteBuf decompressed = super.encode(alloc, frame);
    try {
      if (!decompressed.isReadable())
        return Unpooled.EMPTY_BUFFER; 
      int len = setInput(decompressed);
      return encode(alloc, len);
    } finally {
      decompressed.release();
    } 
  }
  
  public void end() {
    if (this.finished)
      return; 
    this.finished = true;
    this.compressor.end();
    super.end();
  }
}

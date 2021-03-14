package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.util.internal.PlatformDependent;

abstract class SpdyHeaderBlockEncoder {
  static SpdyHeaderBlockEncoder newInstance(SpdyVersion version, int compressionLevel, int windowBits, int memLevel) {
    if (PlatformDependent.javaVersion() >= 7)
      return new SpdyHeaderBlockZlibEncoder(version, compressionLevel); 
    return new SpdyHeaderBlockJZlibEncoder(version, compressionLevel, windowBits, memLevel);
  }
  
  abstract ByteBuf encode(ByteBufAllocator paramByteBufAllocator, SpdyHeadersFrame paramSpdyHeadersFrame) throws Exception;
  
  abstract void end();
}

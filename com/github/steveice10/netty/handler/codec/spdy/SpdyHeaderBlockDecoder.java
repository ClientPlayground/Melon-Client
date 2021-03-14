package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;

abstract class SpdyHeaderBlockDecoder {
  static SpdyHeaderBlockDecoder newInstance(SpdyVersion spdyVersion, int maxHeaderSize) {
    return new SpdyHeaderBlockZlibDecoder(spdyVersion, maxHeaderSize);
  }
  
  abstract void decode(ByteBufAllocator paramByteBufAllocator, ByteBuf paramByteBuf, SpdyHeadersFrame paramSpdyHeadersFrame) throws Exception;
  
  abstract void endHeaderBlock(SpdyHeadersFrame paramSpdyHeadersFrame) throws Exception;
  
  abstract void end();
}

package com.github.steveice10.netty.handler.stream;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelHandlerContext;

public interface ChunkedInput<B> {
  boolean isEndOfInput() throws Exception;
  
  void close() throws Exception;
  
  @Deprecated
  B readChunk(ChannelHandlerContext paramChannelHandlerContext) throws Exception;
  
  B readChunk(ByteBufAllocator paramByteBufAllocator) throws Exception;
  
  long length();
  
  long progress();
}

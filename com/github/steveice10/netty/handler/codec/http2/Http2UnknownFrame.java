package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;

public interface Http2UnknownFrame extends Http2Frame, ByteBufHolder {
  Http2FrameStream stream();
  
  Http2UnknownFrame stream(Http2FrameStream paramHttp2FrameStream);
  
  byte frameType();
  
  Http2Flags flags();
  
  Http2UnknownFrame copy();
  
  Http2UnknownFrame duplicate();
  
  Http2UnknownFrame retainedDuplicate();
  
  Http2UnknownFrame replace(ByteBuf paramByteBuf);
  
  Http2UnknownFrame retain();
  
  Http2UnknownFrame retain(int paramInt);
  
  Http2UnknownFrame touch();
  
  Http2UnknownFrame touch(Object paramObject);
}

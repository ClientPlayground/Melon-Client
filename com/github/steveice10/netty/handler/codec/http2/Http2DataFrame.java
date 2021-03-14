package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;

public interface Http2DataFrame extends Http2StreamFrame, ByteBufHolder {
  int padding();
  
  ByteBuf content();
  
  int initialFlowControlledBytes();
  
  boolean isEndStream();
  
  Http2DataFrame copy();
  
  Http2DataFrame duplicate();
  
  Http2DataFrame retainedDuplicate();
  
  Http2DataFrame replace(ByteBuf paramByteBuf);
  
  Http2DataFrame retain();
  
  Http2DataFrame retain(int paramInt);
  
  Http2DataFrame touch();
  
  Http2DataFrame touch(Object paramObject);
}

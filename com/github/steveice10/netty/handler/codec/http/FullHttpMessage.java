package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface FullHttpMessage extends HttpMessage, LastHttpContent {
  FullHttpMessage copy();
  
  FullHttpMessage duplicate();
  
  FullHttpMessage retainedDuplicate();
  
  FullHttpMessage replace(ByteBuf paramByteBuf);
  
  FullHttpMessage retain(int paramInt);
  
  FullHttpMessage retain();
  
  FullHttpMessage touch();
  
  FullHttpMessage touch(Object paramObject);
}

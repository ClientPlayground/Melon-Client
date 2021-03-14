package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface FullHttpResponse extends HttpResponse, FullHttpMessage {
  FullHttpResponse copy();
  
  FullHttpResponse duplicate();
  
  FullHttpResponse retainedDuplicate();
  
  FullHttpResponse replace(ByteBuf paramByteBuf);
  
  FullHttpResponse retain(int paramInt);
  
  FullHttpResponse retain();
  
  FullHttpResponse touch();
  
  FullHttpResponse touch(Object paramObject);
  
  FullHttpResponse setProtocolVersion(HttpVersion paramHttpVersion);
  
  FullHttpResponse setStatus(HttpResponseStatus paramHttpResponseStatus);
}

package com.github.steveice10.netty.handler.codec.stomp;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface StompFrame extends StompHeadersSubframe, LastStompContentSubframe {
  StompFrame copy();
  
  StompFrame duplicate();
  
  StompFrame retainedDuplicate();
  
  StompFrame replace(ByteBuf paramByteBuf);
  
  StompFrame retain();
  
  StompFrame retain(int paramInt);
  
  StompFrame touch();
  
  StompFrame touch(Object paramObject);
}

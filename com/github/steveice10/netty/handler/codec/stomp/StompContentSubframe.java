package com.github.steveice10.netty.handler.codec.stomp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;

public interface StompContentSubframe extends ByteBufHolder, StompSubframe {
  StompContentSubframe copy();
  
  StompContentSubframe duplicate();
  
  StompContentSubframe retainedDuplicate();
  
  StompContentSubframe replace(ByteBuf paramByteBuf);
  
  StompContentSubframe retain();
  
  StompContentSubframe retain(int paramInt);
  
  StompContentSubframe touch();
  
  StompContentSubframe touch(Object paramObject);
}

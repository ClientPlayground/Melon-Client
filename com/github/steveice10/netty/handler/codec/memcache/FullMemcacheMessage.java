package com.github.steveice10.netty.handler.codec.memcache;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface FullMemcacheMessage extends MemcacheMessage, LastMemcacheContent {
  FullMemcacheMessage copy();
  
  FullMemcacheMessage duplicate();
  
  FullMemcacheMessage retainedDuplicate();
  
  FullMemcacheMessage replace(ByteBuf paramByteBuf);
  
  FullMemcacheMessage retain(int paramInt);
  
  FullMemcacheMessage retain();
  
  FullMemcacheMessage touch();
  
  FullMemcacheMessage touch(Object paramObject);
}

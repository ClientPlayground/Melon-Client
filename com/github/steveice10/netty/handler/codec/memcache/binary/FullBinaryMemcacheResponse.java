package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.handler.codec.memcache.FullMemcacheMessage;

public interface FullBinaryMemcacheResponse extends BinaryMemcacheResponse, FullMemcacheMessage {
  FullBinaryMemcacheResponse copy();
  
  FullBinaryMemcacheResponse duplicate();
  
  FullBinaryMemcacheResponse retainedDuplicate();
  
  FullBinaryMemcacheResponse replace(ByteBuf paramByteBuf);
  
  FullBinaryMemcacheResponse retain(int paramInt);
  
  FullBinaryMemcacheResponse retain();
  
  FullBinaryMemcacheResponse touch();
  
  FullBinaryMemcacheResponse touch(Object paramObject);
}

package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.handler.codec.memcache.FullMemcacheMessage;

public interface FullBinaryMemcacheRequest extends BinaryMemcacheRequest, FullMemcacheMessage {
  FullBinaryMemcacheRequest copy();
  
  FullBinaryMemcacheRequest duplicate();
  
  FullBinaryMemcacheRequest retainedDuplicate();
  
  FullBinaryMemcacheRequest replace(ByteBuf paramByteBuf);
  
  FullBinaryMemcacheRequest retain(int paramInt);
  
  FullBinaryMemcacheRequest retain();
  
  FullBinaryMemcacheRequest touch();
  
  FullBinaryMemcacheRequest touch(Object paramObject);
}

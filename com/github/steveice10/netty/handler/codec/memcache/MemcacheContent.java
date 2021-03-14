package com.github.steveice10.netty.handler.codec.memcache;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;

public interface MemcacheContent extends MemcacheObject, ByteBufHolder {
  MemcacheContent copy();
  
  MemcacheContent duplicate();
  
  MemcacheContent retainedDuplicate();
  
  MemcacheContent replace(ByteBuf paramByteBuf);
  
  MemcacheContent retain();
  
  MemcacheContent retain(int paramInt);
  
  MemcacheContent touch();
  
  MemcacheContent touch(Object paramObject);
}

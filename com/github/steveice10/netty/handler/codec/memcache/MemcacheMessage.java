package com.github.steveice10.netty.handler.codec.memcache;

import com.github.steveice10.netty.util.ReferenceCounted;

public interface MemcacheMessage extends MemcacheObject, ReferenceCounted {
  MemcacheMessage retain();
  
  MemcacheMessage retain(int paramInt);
  
  MemcacheMessage touch();
  
  MemcacheMessage touch(Object paramObject);
}

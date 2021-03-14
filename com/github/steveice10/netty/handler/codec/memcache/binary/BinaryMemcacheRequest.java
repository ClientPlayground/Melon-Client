package com.github.steveice10.netty.handler.codec.memcache.binary;

public interface BinaryMemcacheRequest extends BinaryMemcacheMessage {
  short reserved();
  
  BinaryMemcacheRequest setReserved(short paramShort);
  
  BinaryMemcacheRequest retain();
  
  BinaryMemcacheRequest retain(int paramInt);
  
  BinaryMemcacheRequest touch();
  
  BinaryMemcacheRequest touch(Object paramObject);
}

package com.github.steveice10.netty.handler.codec.memcache.binary;

public interface BinaryMemcacheResponse extends BinaryMemcacheMessage {
  short status();
  
  BinaryMemcacheResponse setStatus(short paramShort);
  
  BinaryMemcacheResponse retain();
  
  BinaryMemcacheResponse retain(int paramInt);
  
  BinaryMemcacheResponse touch();
  
  BinaryMemcacheResponse touch(Object paramObject);
}

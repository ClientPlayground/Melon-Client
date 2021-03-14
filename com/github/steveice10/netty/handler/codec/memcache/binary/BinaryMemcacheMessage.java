package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.handler.codec.memcache.MemcacheMessage;

public interface BinaryMemcacheMessage extends MemcacheMessage {
  byte magic();
  
  BinaryMemcacheMessage setMagic(byte paramByte);
  
  byte opcode();
  
  BinaryMemcacheMessage setOpcode(byte paramByte);
  
  short keyLength();
  
  byte extrasLength();
  
  byte dataType();
  
  BinaryMemcacheMessage setDataType(byte paramByte);
  
  int totalBodyLength();
  
  BinaryMemcacheMessage setTotalBodyLength(int paramInt);
  
  int opaque();
  
  BinaryMemcacheMessage setOpaque(int paramInt);
  
  long cas();
  
  BinaryMemcacheMessage setCas(long paramLong);
  
  ByteBuf key();
  
  BinaryMemcacheMessage setKey(ByteBuf paramByteBuf);
  
  ByteBuf extras();
  
  BinaryMemcacheMessage setExtras(ByteBuf paramByteBuf);
  
  BinaryMemcacheMessage retain();
  
  BinaryMemcacheMessage retain(int paramInt);
  
  BinaryMemcacheMessage touch();
  
  BinaryMemcacheMessage touch(Object paramObject);
}

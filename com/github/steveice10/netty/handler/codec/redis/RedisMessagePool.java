package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface RedisMessagePool {
  SimpleStringRedisMessage getSimpleString(String paramString);
  
  SimpleStringRedisMessage getSimpleString(ByteBuf paramByteBuf);
  
  ErrorRedisMessage getError(String paramString);
  
  ErrorRedisMessage getError(ByteBuf paramByteBuf);
  
  IntegerRedisMessage getInteger(long paramLong);
  
  IntegerRedisMessage getInteger(ByteBuf paramByteBuf);
  
  byte[] getByteBufOfInteger(long paramLong);
}

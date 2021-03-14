package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;

public interface BulkStringRedisContent extends RedisMessage, ByteBufHolder {
  BulkStringRedisContent copy();
  
  BulkStringRedisContent duplicate();
  
  BulkStringRedisContent retainedDuplicate();
  
  BulkStringRedisContent replace(ByteBuf paramByteBuf);
  
  BulkStringRedisContent retain();
  
  BulkStringRedisContent retain(int paramInt);
  
  BulkStringRedisContent touch();
  
  BulkStringRedisContent touch(Object paramObject);
}

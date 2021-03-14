package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.ReferenceCounted;

public interface LastBulkStringRedisContent extends BulkStringRedisContent {
  public static final LastBulkStringRedisContent EMPTY_LAST_CONTENT = new LastBulkStringRedisContent() {
      public ByteBuf content() {
        return Unpooled.EMPTY_BUFFER;
      }
      
      public LastBulkStringRedisContent copy() {
        return this;
      }
      
      public LastBulkStringRedisContent duplicate() {
        return this;
      }
      
      public LastBulkStringRedisContent retainedDuplicate() {
        return this;
      }
      
      public LastBulkStringRedisContent replace(ByteBuf content) {
        return new DefaultLastBulkStringRedisContent(content);
      }
      
      public LastBulkStringRedisContent retain(int increment) {
        return this;
      }
      
      public LastBulkStringRedisContent retain() {
        return this;
      }
      
      public int refCnt() {
        return 1;
      }
      
      public LastBulkStringRedisContent touch() {
        return this;
      }
      
      public LastBulkStringRedisContent touch(Object hint) {
        return this;
      }
      
      public boolean release() {
        return false;
      }
      
      public boolean release(int decrement) {
        return false;
      }
    };
  
  LastBulkStringRedisContent copy();
  
  LastBulkStringRedisContent duplicate();
  
  LastBulkStringRedisContent retainedDuplicate();
  
  LastBulkStringRedisContent replace(ByteBuf paramByteBuf);
  
  LastBulkStringRedisContent retain();
  
  LastBulkStringRedisContent retain(int paramInt);
  
  LastBulkStringRedisContent touch();
  
  LastBulkStringRedisContent touch(Object paramObject);
}

package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.util.ReferenceCounted;

public final class DefaultLastBulkStringRedisContent extends DefaultBulkStringRedisContent implements LastBulkStringRedisContent {
  public DefaultLastBulkStringRedisContent(ByteBuf content) {
    super(content);
  }
  
  public LastBulkStringRedisContent copy() {
    return (LastBulkStringRedisContent)super.copy();
  }
  
  public LastBulkStringRedisContent duplicate() {
    return (LastBulkStringRedisContent)super.duplicate();
  }
  
  public LastBulkStringRedisContent retainedDuplicate() {
    return (LastBulkStringRedisContent)super.retainedDuplicate();
  }
  
  public LastBulkStringRedisContent replace(ByteBuf content) {
    return new DefaultLastBulkStringRedisContent(content);
  }
  
  public LastBulkStringRedisContent retain() {
    super.retain();
    return this;
  }
  
  public LastBulkStringRedisContent retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public LastBulkStringRedisContent touch() {
    super.touch();
    return this;
  }
  
  public LastBulkStringRedisContent touch(Object hint) {
    super.touch(hint);
    return this;
  }
}

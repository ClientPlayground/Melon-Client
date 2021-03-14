package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.DefaultByteBufHolder;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultBulkStringRedisContent extends DefaultByteBufHolder implements BulkStringRedisContent {
  public DefaultBulkStringRedisContent(ByteBuf content) {
    super(content);
  }
  
  public BulkStringRedisContent copy() {
    return (BulkStringRedisContent)super.copy();
  }
  
  public BulkStringRedisContent duplicate() {
    return (BulkStringRedisContent)super.duplicate();
  }
  
  public BulkStringRedisContent retainedDuplicate() {
    return (BulkStringRedisContent)super.retainedDuplicate();
  }
  
  public BulkStringRedisContent replace(ByteBuf content) {
    return new DefaultBulkStringRedisContent(content);
  }
  
  public BulkStringRedisContent retain() {
    super.retain();
    return this;
  }
  
  public BulkStringRedisContent retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public BulkStringRedisContent touch() {
    super.touch();
    return this;
  }
  
  public BulkStringRedisContent touch(Object hint) {
    super.touch(hint);
    return this;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '[' + 
      "content=" + 
      
      content() + ']';
  }
}

package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.DefaultByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.StringUtil;

public class FullBulkStringRedisMessage extends DefaultByteBufHolder implements LastBulkStringRedisContent {
  private FullBulkStringRedisMessage() {
    this(Unpooled.EMPTY_BUFFER);
  }
  
  public FullBulkStringRedisMessage(ByteBuf content) {
    super(content);
  }
  
  public boolean isNull() {
    return false;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '[' + 
      "content=" + 
      
      content() + ']';
  }
  
  public static final FullBulkStringRedisMessage NULL_INSTANCE = new FullBulkStringRedisMessage() {
      public boolean isNull() {
        return true;
      }
      
      public ByteBuf content() {
        return Unpooled.EMPTY_BUFFER;
      }
      
      public FullBulkStringRedisMessage copy() {
        return this;
      }
      
      public FullBulkStringRedisMessage duplicate() {
        return this;
      }
      
      public FullBulkStringRedisMessage retainedDuplicate() {
        return this;
      }
      
      public int refCnt() {
        return 1;
      }
      
      public FullBulkStringRedisMessage retain() {
        return this;
      }
      
      public FullBulkStringRedisMessage retain(int increment) {
        return this;
      }
      
      public FullBulkStringRedisMessage touch() {
        return this;
      }
      
      public FullBulkStringRedisMessage touch(Object hint) {
        return this;
      }
      
      public boolean release() {
        return false;
      }
      
      public boolean release(int decrement) {
        return false;
      }
    };
  
  public static final FullBulkStringRedisMessage EMPTY_INSTANCE = new FullBulkStringRedisMessage() {
      public ByteBuf content() {
        return Unpooled.EMPTY_BUFFER;
      }
      
      public FullBulkStringRedisMessage copy() {
        return this;
      }
      
      public FullBulkStringRedisMessage duplicate() {
        return this;
      }
      
      public FullBulkStringRedisMessage retainedDuplicate() {
        return this;
      }
      
      public int refCnt() {
        return 1;
      }
      
      public FullBulkStringRedisMessage retain() {
        return this;
      }
      
      public FullBulkStringRedisMessage retain(int increment) {
        return this;
      }
      
      public FullBulkStringRedisMessage touch() {
        return this;
      }
      
      public FullBulkStringRedisMessage touch(Object hint) {
        return this;
      }
      
      public boolean release() {
        return false;
      }
      
      public boolean release(int decrement) {
        return false;
      }
    };
  
  public FullBulkStringRedisMessage copy() {
    return (FullBulkStringRedisMessage)super.copy();
  }
  
  public FullBulkStringRedisMessage duplicate() {
    return (FullBulkStringRedisMessage)super.duplicate();
  }
  
  public FullBulkStringRedisMessage retainedDuplicate() {
    return (FullBulkStringRedisMessage)super.retainedDuplicate();
  }
  
  public FullBulkStringRedisMessage replace(ByteBuf content) {
    return new FullBulkStringRedisMessage(content);
  }
  
  public FullBulkStringRedisMessage retain() {
    super.retain();
    return this;
  }
  
  public FullBulkStringRedisMessage retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public FullBulkStringRedisMessage touch() {
    super.touch();
    return this;
  }
  
  public FullBulkStringRedisMessage touch(Object hint) {
    super.touch(hint);
    return this;
  }
}

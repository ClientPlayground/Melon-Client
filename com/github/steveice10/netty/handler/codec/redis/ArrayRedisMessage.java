package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.util.AbstractReferenceCounted;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.Collections;
import java.util.List;

public class ArrayRedisMessage extends AbstractReferenceCounted implements RedisMessage {
  private final List<RedisMessage> children;
  
  private ArrayRedisMessage() {
    this.children = Collections.emptyList();
  }
  
  public ArrayRedisMessage(List<RedisMessage> children) {
    this.children = (List<RedisMessage>)ObjectUtil.checkNotNull(children, "children");
  }
  
  public final List<RedisMessage> children() {
    return this.children;
  }
  
  public boolean isNull() {
    return false;
  }
  
  protected void deallocate() {
    for (RedisMessage msg : this.children)
      ReferenceCountUtil.release(msg); 
  }
  
  public ArrayRedisMessage touch(Object hint) {
    for (RedisMessage msg : this.children)
      ReferenceCountUtil.touch(msg); 
    return this;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '[' + 
      "children=" + 
      this.children
      .size() + ']';
  }
  
  public static final ArrayRedisMessage NULL_INSTANCE = new ArrayRedisMessage() {
      public boolean isNull() {
        return true;
      }
      
      public ArrayRedisMessage retain() {
        return this;
      }
      
      public ArrayRedisMessage retain(int increment) {
        return this;
      }
      
      public ArrayRedisMessage touch() {
        return this;
      }
      
      public ArrayRedisMessage touch(Object hint) {
        return this;
      }
      
      public boolean release() {
        return false;
      }
      
      public boolean release(int decrement) {
        return false;
      }
      
      public String toString() {
        return "NullArrayRedisMessage";
      }
    };
  
  public static final ArrayRedisMessage EMPTY_INSTANCE = new ArrayRedisMessage() {
      public ArrayRedisMessage retain() {
        return this;
      }
      
      public ArrayRedisMessage retain(int increment) {
        return this;
      }
      
      public ArrayRedisMessage touch() {
        return this;
      }
      
      public ArrayRedisMessage touch(Object hint) {
        return this;
      }
      
      public boolean release() {
        return false;
      }
      
      public boolean release(int decrement) {
        return false;
      }
      
      public String toString() {
        return "EmptyArrayRedisMessage";
      }
    };
}

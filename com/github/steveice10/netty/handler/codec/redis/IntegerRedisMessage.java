package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.util.internal.StringUtil;

public final class IntegerRedisMessage implements RedisMessage {
  private final long value;
  
  public IntegerRedisMessage(long value) {
    this.value = value;
  }
  
  public long value() {
    return this.value;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '[' + 
      "value=" + 
      this.value + 
      ']';
  }
}

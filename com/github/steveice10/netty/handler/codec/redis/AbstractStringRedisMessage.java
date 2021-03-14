package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;

public abstract class AbstractStringRedisMessage implements RedisMessage {
  private final String content;
  
  AbstractStringRedisMessage(String content) {
    this.content = (String)ObjectUtil.checkNotNull(content, "content");
  }
  
  public final String content() {
    return this.content;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '[' + 
      "content=" + 
      this.content + 
      ']';
  }
}

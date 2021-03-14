package com.github.steveice10.netty.handler.codec.redis;

public final class InlineCommandRedisMessage extends AbstractStringRedisMessage {
  public InlineCommandRedisMessage(String content) {
    super(content);
  }
}

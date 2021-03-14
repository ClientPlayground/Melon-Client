package com.github.steveice10.netty.handler.codec.redis;

public final class ErrorRedisMessage extends AbstractStringRedisMessage {
  public ErrorRedisMessage(String content) {
    super(content);
  }
}

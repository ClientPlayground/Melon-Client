package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.handler.codec.CodecException;

public final class RedisCodecException extends CodecException {
  private static final long serialVersionUID = 5570454251549268063L;
  
  public RedisCodecException(String message) {
    super(message);
  }
  
  public RedisCodecException(Throwable cause) {
    super(cause);
  }
}

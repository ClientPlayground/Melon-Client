package com.github.steveice10.netty.handler.codec.serialization;

public interface ClassResolver {
  Class<?> resolve(String paramString) throws ClassNotFoundException;
}

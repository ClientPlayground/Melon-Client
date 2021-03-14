package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;

public final class Delimiters {
  public static ByteBuf[] nulDelimiter() {
    return new ByteBuf[] { Unpooled.wrappedBuffer(new byte[] { 0 }) };
  }
  
  public static ByteBuf[] lineDelimiter() {
    return new ByteBuf[] { Unpooled.wrappedBuffer(new byte[] { 13, 10 }), Unpooled.wrappedBuffer(new byte[] { 10 }) };
  }
}

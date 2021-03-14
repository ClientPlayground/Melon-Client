package com.github.steveice10.netty.handler.codec.redis;

import com.github.steveice10.netty.buffer.ByteBuf;

public enum RedisMessageType {
  INLINE_COMMAND(null, true),
  SIMPLE_STRING(Byte.valueOf((byte)43), true),
  ERROR(Byte.valueOf((byte)45), true),
  INTEGER(Byte.valueOf((byte)58), true),
  BULK_STRING(Byte.valueOf((byte)36), false),
  ARRAY_HEADER(Byte.valueOf((byte)42), false);
  
  private final Byte value;
  
  private final boolean inline;
  
  RedisMessageType(Byte value, boolean inline) {
    this.value = value;
    this.inline = inline;
  }
  
  public int length() {
    return (this.value != null) ? 1 : 0;
  }
  
  public boolean isInline() {
    return this.inline;
  }
  
  public static RedisMessageType readFrom(ByteBuf in, boolean decodeInlineCommands) {
    int initialIndex = in.readerIndex();
    RedisMessageType type = valueOf(in.readByte());
    if (type == INLINE_COMMAND) {
      if (!decodeInlineCommands)
        throw new RedisCodecException("Decoding of inline commands is disabled"); 
      in.readerIndex(initialIndex);
    } 
    return type;
  }
  
  public void writeTo(ByteBuf out) {
    if (this.value == null)
      return; 
    out.writeByte(this.value.byteValue());
  }
}

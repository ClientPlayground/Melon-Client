package com.github.steveice10.netty.handler.codec.haproxy;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.DefaultByteBufHolder;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public class HAProxyTLV extends DefaultByteBufHolder {
  private final Type type;
  
  private final byte typeByteValue;
  
  public enum Type {
    PP2_TYPE_ALPN, PP2_TYPE_AUTHORITY, PP2_TYPE_SSL, PP2_TYPE_SSL_VERSION, PP2_TYPE_SSL_CN, PP2_TYPE_NETNS, OTHER;
    
    public static Type typeForByteValue(byte byteValue) {
      switch (byteValue) {
        case 1:
          return PP2_TYPE_ALPN;
        case 2:
          return PP2_TYPE_AUTHORITY;
        case 32:
          return PP2_TYPE_SSL;
        case 33:
          return PP2_TYPE_SSL_VERSION;
        case 34:
          return PP2_TYPE_SSL_CN;
        case 48:
          return PP2_TYPE_NETNS;
      } 
      return OTHER;
    }
  }
  
  HAProxyTLV(Type type, byte typeByteValue, ByteBuf content) {
    super(content);
    ObjectUtil.checkNotNull(type, "type");
    this.type = type;
    this.typeByteValue = typeByteValue;
  }
  
  public Type type() {
    return this.type;
  }
  
  public byte typeByteValue() {
    return this.typeByteValue;
  }
  
  public HAProxyTLV copy() {
    return replace(content().copy());
  }
  
  public HAProxyTLV duplicate() {
    return replace(content().duplicate());
  }
  
  public HAProxyTLV retainedDuplicate() {
    return replace(content().retainedDuplicate());
  }
  
  public HAProxyTLV replace(ByteBuf content) {
    return new HAProxyTLV(this.type, this.typeByteValue, content);
  }
  
  public HAProxyTLV retain() {
    super.retain();
    return this;
  }
  
  public HAProxyTLV retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public HAProxyTLV touch() {
    super.touch();
    return this;
  }
  
  public HAProxyTLV touch(Object hint) {
    super.touch(hint);
    return this;
  }
}

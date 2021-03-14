package com.github.steveice10.netty.handler.codec.socks;

public enum SocksAddressType {
  IPv4((byte)1),
  DOMAIN((byte)3),
  IPv6((byte)4),
  UNKNOWN((byte)-1);
  
  private final byte b;
  
  SocksAddressType(byte b) {
    this.b = b;
  }
  
  @Deprecated
  public static SocksAddressType fromByte(byte b) {
    return valueOf(b);
  }
  
  public byte byteValue() {
    return this.b;
  }
}

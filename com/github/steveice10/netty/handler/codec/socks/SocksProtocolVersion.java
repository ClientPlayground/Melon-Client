package com.github.steveice10.netty.handler.codec.socks;

public enum SocksProtocolVersion {
  SOCKS4a((byte)4),
  SOCKS5((byte)5),
  UNKNOWN((byte)-1);
  
  private final byte b;
  
  SocksProtocolVersion(byte b) {
    this.b = b;
  }
  
  @Deprecated
  public static SocksProtocolVersion fromByte(byte b) {
    return valueOf(b);
  }
  
  public byte byteValue() {
    return this.b;
  }
}

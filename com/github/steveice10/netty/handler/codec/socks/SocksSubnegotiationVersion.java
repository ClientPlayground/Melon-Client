package com.github.steveice10.netty.handler.codec.socks;

public enum SocksSubnegotiationVersion {
  AUTH_PASSWORD((byte)1),
  UNKNOWN((byte)-1);
  
  private final byte b;
  
  SocksSubnegotiationVersion(byte b) {
    this.b = b;
  }
  
  @Deprecated
  public static SocksSubnegotiationVersion fromByte(byte b) {
    return valueOf(b);
  }
  
  public byte byteValue() {
    return this.b;
  }
}

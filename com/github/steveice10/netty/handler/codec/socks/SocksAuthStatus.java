package com.github.steveice10.netty.handler.codec.socks;

public enum SocksAuthStatus {
  SUCCESS((byte)0),
  FAILURE((byte)-1);
  
  private final byte b;
  
  SocksAuthStatus(byte b) {
    this.b = b;
  }
  
  @Deprecated
  public static SocksAuthStatus fromByte(byte b) {
    return valueOf(b);
  }
  
  public byte byteValue() {
    return this.b;
  }
}

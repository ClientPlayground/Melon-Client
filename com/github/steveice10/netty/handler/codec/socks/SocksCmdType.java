package com.github.steveice10.netty.handler.codec.socks;

public enum SocksCmdType {
  CONNECT((byte)1),
  BIND((byte)2),
  UDP((byte)3),
  UNKNOWN((byte)-1);
  
  private final byte b;
  
  SocksCmdType(byte b) {
    this.b = b;
  }
  
  @Deprecated
  public static SocksCmdType fromByte(byte b) {
    return valueOf(b);
  }
  
  public byte byteValue() {
    return this.b;
  }
}

package com.github.steveice10.netty.handler.codec.socksx;

public enum SocksVersion {
  SOCKS4a((byte)4),
  SOCKS5((byte)5),
  UNKNOWN((byte)-1);
  
  private final byte b;
  
  SocksVersion(byte b) {
    this.b = b;
  }
  
  public byte byteValue() {
    return this.b;
  }
}

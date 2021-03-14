package com.github.steveice10.netty.handler.codec.haproxy;

public enum HAProxyProtocolVersion {
  V1((byte)16),
  V2((byte)32);
  
  private static final byte VERSION_MASK = -16;
  
  private final byte byteValue;
  
  HAProxyProtocolVersion(byte byteValue) {
    this.byteValue = byteValue;
  }
  
  public byte byteValue() {
    return this.byteValue;
  }
}

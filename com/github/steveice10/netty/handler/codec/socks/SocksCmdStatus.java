package com.github.steveice10.netty.handler.codec.socks;

public enum SocksCmdStatus {
  SUCCESS((byte)0),
  FAILURE((byte)1),
  FORBIDDEN((byte)2),
  NETWORK_UNREACHABLE((byte)3),
  HOST_UNREACHABLE((byte)4),
  REFUSED((byte)5),
  TTL_EXPIRED((byte)6),
  COMMAND_NOT_SUPPORTED((byte)7),
  ADDRESS_NOT_SUPPORTED((byte)8),
  UNASSIGNED((byte)-1);
  
  private final byte b;
  
  SocksCmdStatus(byte b) {
    this.b = b;
  }
  
  @Deprecated
  public static SocksCmdStatus fromByte(byte b) {
    return valueOf(b);
  }
  
  public byte byteValue() {
    return this.b;
  }
}

package com.github.steveice10.netty.handler.codec.haproxy;

public enum HAProxyCommand {
  LOCAL((byte)0),
  PROXY((byte)1);
  
  private static final byte COMMAND_MASK = 15;
  
  private final byte byteValue;
  
  HAProxyCommand(byte byteValue) {
    this.byteValue = byteValue;
  }
  
  public byte byteValue() {
    return this.byteValue;
  }
}

package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;

public abstract class SocksMessage {
  private final SocksMessageType type;
  
  private final SocksProtocolVersion protocolVersion = SocksProtocolVersion.SOCKS5;
  
  protected SocksMessage(SocksMessageType type) {
    if (type == null)
      throw new NullPointerException("type"); 
    this.type = type;
  }
  
  public SocksMessageType type() {
    return this.type;
  }
  
  public SocksProtocolVersion protocolVersion() {
    return this.protocolVersion;
  }
  
  @Deprecated
  public abstract void encodeAsByteBuf(ByteBuf paramByteBuf);
}

package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;

public final class SocksInitResponse extends SocksResponse {
  private final SocksAuthScheme authScheme;
  
  public SocksInitResponse(SocksAuthScheme authScheme) {
    super(SocksResponseType.INIT);
    if (authScheme == null)
      throw new NullPointerException("authScheme"); 
    this.authScheme = authScheme;
  }
  
  public SocksAuthScheme authScheme() {
    return this.authScheme;
  }
  
  public void encodeAsByteBuf(ByteBuf byteBuf) {
    byteBuf.writeByte(protocolVersion().byteValue());
    byteBuf.writeByte(this.authScheme.byteValue());
  }
}

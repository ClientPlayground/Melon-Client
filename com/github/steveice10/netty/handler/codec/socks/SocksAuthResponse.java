package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;

public final class SocksAuthResponse extends SocksResponse {
  private static final SocksSubnegotiationVersion SUBNEGOTIATION_VERSION = SocksSubnegotiationVersion.AUTH_PASSWORD;
  
  private final SocksAuthStatus authStatus;
  
  public SocksAuthResponse(SocksAuthStatus authStatus) {
    super(SocksResponseType.AUTH);
    if (authStatus == null)
      throw new NullPointerException("authStatus"); 
    this.authStatus = authStatus;
  }
  
  public SocksAuthStatus authStatus() {
    return this.authStatus;
  }
  
  public void encodeAsByteBuf(ByteBuf byteBuf) {
    byteBuf.writeByte(SUBNEGOTIATION_VERSION.byteValue());
    byteBuf.writeByte(this.authStatus.byteValue());
  }
}

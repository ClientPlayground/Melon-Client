package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;

public final class UnknownSocksResponse extends SocksResponse {
  public UnknownSocksResponse() {
    super(SocksResponseType.UNKNOWN);
  }
  
  public void encodeAsByteBuf(ByteBuf byteBuf) {}
}

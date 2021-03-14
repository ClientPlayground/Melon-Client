package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;

public final class UnknownSocksRequest extends SocksRequest {
  public UnknownSocksRequest() {
    super(SocksRequestType.UNKNOWN);
  }
  
  public void encodeAsByteBuf(ByteBuf byteBuf) {}
}

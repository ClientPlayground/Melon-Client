package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import java.util.List;

public class SocksInitResponseDecoder extends ReplayingDecoder<SocksInitResponseDecoder.State> {
  public SocksInitResponseDecoder() {
    super(State.CHECK_PROTOCOL_VERSION);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
    SocksAuthScheme authScheme;
    switch ((State)state()) {
      case CHECK_PROTOCOL_VERSION:
        if (byteBuf.readByte() != SocksProtocolVersion.SOCKS5.byteValue()) {
          out.add(SocksCommonUtils.UNKNOWN_SOCKS_RESPONSE);
          break;
        } 
        checkpoint(State.READ_PREFERRED_AUTH_TYPE);
      case READ_PREFERRED_AUTH_TYPE:
        authScheme = SocksAuthScheme.valueOf(byteBuf.readByte());
        out.add(new SocksInitResponse(authScheme));
        break;
      default:
        throw new Error();
    } 
    ctx.pipeline().remove((ChannelHandler)this);
  }
  
  enum State {
    CHECK_PROTOCOL_VERSION, READ_PREFERRED_AUTH_TYPE;
  }
}

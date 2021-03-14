package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import java.util.List;

public class SocksAuthResponseDecoder extends ReplayingDecoder<SocksAuthResponseDecoder.State> {
  public SocksAuthResponseDecoder() {
    super(State.CHECK_PROTOCOL_VERSION);
  }
  
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
    SocksAuthStatus authStatus;
    switch ((State)state()) {
      case CHECK_PROTOCOL_VERSION:
        if (byteBuf.readByte() != SocksSubnegotiationVersion.AUTH_PASSWORD.byteValue()) {
          out.add(SocksCommonUtils.UNKNOWN_SOCKS_RESPONSE);
          break;
        } 
        checkpoint(State.READ_AUTH_RESPONSE);
      case READ_AUTH_RESPONSE:
        authStatus = SocksAuthStatus.valueOf(byteBuf.readByte());
        out.add(new SocksAuthResponse(authStatus));
        break;
      default:
        throw new Error();
    } 
    channelHandlerContext.pipeline().remove((ChannelHandler)this);
  }
  
  enum State {
    CHECK_PROTOCOL_VERSION, READ_AUTH_RESPONSE;
  }
}

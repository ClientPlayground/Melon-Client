package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SocksInitRequestDecoder extends ReplayingDecoder<SocksInitRequestDecoder.State> {
  public SocksInitRequestDecoder() {
    super(State.CHECK_PROTOCOL_VERSION);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
    byte authSchemeNum;
    List<SocksAuthScheme> authSchemes;
    switch ((State)state()) {
      case CHECK_PROTOCOL_VERSION:
        if (byteBuf.readByte() != SocksProtocolVersion.SOCKS5.byteValue()) {
          out.add(SocksCommonUtils.UNKNOWN_SOCKS_REQUEST);
          break;
        } 
        checkpoint(State.READ_AUTH_SCHEMES);
      case READ_AUTH_SCHEMES:
        authSchemeNum = byteBuf.readByte();
        if (authSchemeNum > 0) {
          authSchemes = new ArrayList<SocksAuthScheme>(authSchemeNum);
          for (int i = 0; i < authSchemeNum; i++)
            authSchemes.add(SocksAuthScheme.valueOf(byteBuf.readByte())); 
        } else {
          authSchemes = Collections.emptyList();
        } 
        out.add(new SocksInitRequest(authSchemes));
        break;
      default:
        throw new Error();
    } 
    ctx.pipeline().remove((ChannelHandler)this);
  }
  
  enum State {
    CHECK_PROTOCOL_VERSION, READ_AUTH_SCHEMES;
  }
}

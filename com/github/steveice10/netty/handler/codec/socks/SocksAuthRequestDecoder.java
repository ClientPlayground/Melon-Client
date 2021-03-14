package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import java.util.List;

public class SocksAuthRequestDecoder extends ReplayingDecoder<SocksAuthRequestDecoder.State> {
  private String username;
  
  public SocksAuthRequestDecoder() {
    super(State.CHECK_PROTOCOL_VERSION);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
    int fieldLength;
    String password;
    switch ((State)state()) {
      case CHECK_PROTOCOL_VERSION:
        if (byteBuf.readByte() != SocksSubnegotiationVersion.AUTH_PASSWORD.byteValue()) {
          out.add(SocksCommonUtils.UNKNOWN_SOCKS_REQUEST);
          break;
        } 
        checkpoint(State.READ_USERNAME);
      case READ_USERNAME:
        fieldLength = byteBuf.readByte();
        this.username = SocksCommonUtils.readUsAscii(byteBuf, fieldLength);
        checkpoint(State.READ_PASSWORD);
      case READ_PASSWORD:
        fieldLength = byteBuf.readByte();
        password = SocksCommonUtils.readUsAscii(byteBuf, fieldLength);
        out.add(new SocksAuthRequest(this.username, password));
        break;
      default:
        throw new Error();
    } 
    ctx.pipeline().remove((ChannelHandler)this);
  }
  
  enum State {
    CHECK_PROTOCOL_VERSION, READ_USERNAME, READ_PASSWORD;
  }
}

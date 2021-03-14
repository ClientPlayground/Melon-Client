package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class SocksMessageEncoder extends MessageToByteEncoder<SocksMessage> {
  protected void encode(ChannelHandlerContext ctx, SocksMessage msg, ByteBuf out) throws Exception {
    msg.encodeAsByteBuf(out);
  }
}

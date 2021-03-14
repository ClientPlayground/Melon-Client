package com.github.steveice10.netty.handler.codec.socksx.v4;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;
import com.github.steveice10.netty.util.NetUtil;

@Sharable
public final class Socks4ServerEncoder extends MessageToByteEncoder<Socks4CommandResponse> {
  public static final Socks4ServerEncoder INSTANCE = new Socks4ServerEncoder();
  
  private static final byte[] IPv4_HOSTNAME_ZEROED = new byte[] { 0, 0, 0, 0 };
  
  protected void encode(ChannelHandlerContext ctx, Socks4CommandResponse msg, ByteBuf out) throws Exception {
    out.writeByte(0);
    out.writeByte(msg.status().byteValue());
    out.writeShort(msg.dstPort());
    out.writeBytes((msg.dstAddr() == null) ? IPv4_HOSTNAME_ZEROED : 
        NetUtil.createByteArrayFromIpAddressString(msg.dstAddr()));
  }
}

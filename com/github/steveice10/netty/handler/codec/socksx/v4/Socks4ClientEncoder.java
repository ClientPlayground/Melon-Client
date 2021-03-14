package com.github.steveice10.netty.handler.codec.socksx.v4;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;
import com.github.steveice10.netty.util.NetUtil;

@Sharable
public final class Socks4ClientEncoder extends MessageToByteEncoder<Socks4CommandRequest> {
  public static final Socks4ClientEncoder INSTANCE = new Socks4ClientEncoder();
  
  private static final byte[] IPv4_DOMAIN_MARKER = new byte[] { 0, 0, 0, 1 };
  
  protected void encode(ChannelHandlerContext ctx, Socks4CommandRequest msg, ByteBuf out) throws Exception {
    out.writeByte(msg.version().byteValue());
    out.writeByte(msg.type().byteValue());
    out.writeShort(msg.dstPort());
    if (NetUtil.isValidIpV4Address(msg.dstAddr())) {
      out.writeBytes(NetUtil.createByteArrayFromIpAddressString(msg.dstAddr()));
      ByteBufUtil.writeAscii(out, msg.userId());
      out.writeByte(0);
    } else {
      out.writeBytes(IPv4_DOMAIN_MARKER);
      ByteBufUtil.writeAscii(out, msg.userId());
      out.writeByte(0);
      ByteBufUtil.writeAscii(out, msg.dstAddr());
      out.writeByte(0);
    } 
  }
}

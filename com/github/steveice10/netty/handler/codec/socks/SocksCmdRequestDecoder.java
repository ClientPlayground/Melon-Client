package com.github.steveice10.netty.handler.codec.socks;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ReplayingDecoder;
import com.github.steveice10.netty.util.NetUtil;
import java.util.List;

public class SocksCmdRequestDecoder extends ReplayingDecoder<SocksCmdRequestDecoder.State> {
  private SocksCmdType cmdType;
  
  private SocksAddressType addressType;
  
  public SocksCmdRequestDecoder() {
    super(State.CHECK_PROTOCOL_VERSION);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
    String host;
    int fieldLength;
    byte[] bytes;
    int port;
    String str1;
    int i;
    switch ((State)state()) {
      case CHECK_PROTOCOL_VERSION:
        if (byteBuf.readByte() != SocksProtocolVersion.SOCKS5.byteValue()) {
          out.add(SocksCommonUtils.UNKNOWN_SOCKS_REQUEST);
          break;
        } 
        checkpoint(State.READ_CMD_HEADER);
      case READ_CMD_HEADER:
        this.cmdType = SocksCmdType.valueOf(byteBuf.readByte());
        byteBuf.skipBytes(1);
        this.addressType = SocksAddressType.valueOf(byteBuf.readByte());
        checkpoint(State.READ_CMD_ADDRESS);
      case READ_CMD_ADDRESS:
        switch (this.addressType) {
          case CHECK_PROTOCOL_VERSION:
            host = NetUtil.intToIpAddress(byteBuf.readInt());
            port = byteBuf.readUnsignedShort();
            out.add(new SocksCmdRequest(this.cmdType, this.addressType, host, port));
            break;
          case READ_CMD_HEADER:
            fieldLength = byteBuf.readByte();
            str1 = SocksCommonUtils.readUsAscii(byteBuf, fieldLength);
            i = byteBuf.readUnsignedShort();
            out.add(new SocksCmdRequest(this.cmdType, this.addressType, str1, i));
            break;
          case READ_CMD_ADDRESS:
            bytes = new byte[16];
            byteBuf.readBytes(bytes);
            str1 = SocksCommonUtils.ipv6toStr(bytes);
            i = byteBuf.readUnsignedShort();
            out.add(new SocksCmdRequest(this.cmdType, this.addressType, str1, i));
            break;
          case null:
            out.add(SocksCommonUtils.UNKNOWN_SOCKS_REQUEST);
            break;
        } 
        throw new Error();
      default:
        throw new Error();
    } 
    ctx.pipeline().remove((ChannelHandler)this);
  }
  
  enum State {
    CHECK_PROTOCOL_VERSION, READ_CMD_HEADER, READ_CMD_ADDRESS;
  }
}

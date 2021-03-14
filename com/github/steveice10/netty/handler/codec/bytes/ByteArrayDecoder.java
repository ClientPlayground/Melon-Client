package com.github.steveice10.netty.handler.codec.bytes;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

public class ByteArrayDecoder extends MessageToMessageDecoder<ByteBuf> {
  protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
    byte[] array = new byte[msg.readableBytes()];
    msg.getBytes(0, array);
    out.add(array);
  }
}

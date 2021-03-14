package com.github.steveice10.netty.handler.codec.bytes;

import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

@Sharable
public class ByteArrayEncoder extends MessageToMessageEncoder<byte[]> {
  protected void encode(ChannelHandlerContext ctx, byte[] msg, List<Object> out) throws Exception {
    out.add(Unpooled.wrappedBuffer(msg));
  }
}

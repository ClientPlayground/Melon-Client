package com.github.steveice10.netty.handler.codec.protobuf;

import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import java.util.List;

@Sharable
public class ProtobufEncoder extends MessageToMessageEncoder<MessageLiteOrBuilder> {
  protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out) throws Exception {
    if (msg instanceof MessageLite) {
      out.add(Unpooled.wrappedBuffer(((MessageLite)msg).toByteArray()));
      return;
    } 
    if (msg instanceof MessageLite.Builder)
      out.add(Unpooled.wrappedBuffer(((MessageLite.Builder)msg).build().toByteArray())); 
  }
}

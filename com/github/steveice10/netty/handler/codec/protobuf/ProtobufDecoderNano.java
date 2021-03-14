package com.github.steveice10.netty.handler.codec.protobuf;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.google.protobuf.nano.MessageNano;
import java.util.List;

@Sharable
public class ProtobufDecoderNano extends MessageToMessageDecoder<ByteBuf> {
  private final Class<? extends MessageNano> clazz;
  
  public ProtobufDecoderNano(Class<? extends MessageNano> clazz) {
    this.clazz = (Class<? extends MessageNano>)ObjectUtil.checkNotNull(clazz, "You must provide a Class");
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
    byte[] array;
    int offset, length = msg.readableBytes();
    if (msg.hasArray()) {
      array = msg.array();
      offset = msg.arrayOffset() + msg.readerIndex();
    } else {
      array = new byte[length];
      msg.getBytes(msg.readerIndex(), array, 0, length);
      offset = 0;
    } 
    MessageNano prototype = this.clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
    out.add(MessageNano.mergeFrom(prototype, array, offset, length));
  }
}

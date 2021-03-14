package com.github.steveice10.netty.handler.codec.protobuf;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;
import java.util.List;

@Sharable
public class ProtobufEncoderNano extends MessageToMessageEncoder<MessageNano> {
  protected void encode(ChannelHandlerContext ctx, MessageNano msg, List<Object> out) throws Exception {
    int size = msg.getSerializedSize();
    ByteBuf buffer = ctx.alloc().heapBuffer(size, size);
    byte[] array = buffer.array();
    CodedOutputByteBufferNano cobbn = CodedOutputByteBufferNano.newInstance(array, buffer
        .arrayOffset(), buffer.capacity());
    msg.writeTo(cobbn);
    buffer.writerIndex(size);
    out.add(buffer);
  }
}

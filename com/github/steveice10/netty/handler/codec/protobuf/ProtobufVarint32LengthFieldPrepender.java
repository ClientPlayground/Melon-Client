package com.github.steveice10.netty.handler.codec.protobuf;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class ProtobufVarint32LengthFieldPrepender extends MessageToByteEncoder<ByteBuf> {
  protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
    int bodyLen = msg.readableBytes();
    int headerLen = computeRawVarint32Size(bodyLen);
    out.ensureWritable(headerLen + bodyLen);
    writeRawVarint32(out, bodyLen);
    out.writeBytes(msg, msg.readerIndex(), bodyLen);
  }
  
  static void writeRawVarint32(ByteBuf out, int value) {
    while (true) {
      if ((value & 0xFFFFFF80) == 0) {
        out.writeByte(value);
        return;
      } 
      out.writeByte(value & 0x7F | 0x80);
      value >>>= 7;
    } 
  }
  
  static int computeRawVarint32Size(int value) {
    if ((value & 0xFFFFFF80) == 0)
      return 1; 
    if ((value & 0xFFFFC000) == 0)
      return 2; 
    if ((value & 0xFFE00000) == 0)
      return 3; 
    if ((value & 0xF0000000) == 0)
      return 4; 
    return 5;
  }
}

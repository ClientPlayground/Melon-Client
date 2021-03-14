package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ByteToMessageCodec;
import com.github.steveice10.netty.handler.codec.CorruptedFrameException;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetInput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;
import java.util.List;

public class TcpPacketSizer extends ByteToMessageCodec<ByteBuf> {
  private Session session;
  
  public TcpPacketSizer(Session session) {
    this.session = session;
  }
  
  public void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
    int length = in.readableBytes();
    out.ensureWritable(this.session.getPacketProtocol().getPacketHeader().getLengthSize(length) + length);
    this.session.getPacketProtocol().getPacketHeader().writeLength((NetOutput)new ByteBufNetOutput(out), length);
    out.writeBytes(in);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
    int size = this.session.getPacketProtocol().getPacketHeader().getLengthSize();
    if (size > 0) {
      buf.markReaderIndex();
      byte[] lengthBytes = new byte[size];
      for (int index = 0; index < lengthBytes.length; index++) {
        if (!buf.isReadable()) {
          buf.resetReaderIndex();
          return;
        } 
        lengthBytes[index] = buf.readByte();
        if ((this.session.getPacketProtocol().getPacketHeader().isLengthVariable() && lengthBytes[index] >= 0) || index == size - 1) {
          int length = this.session.getPacketProtocol().getPacketHeader().readLength((NetInput)new ByteBufNetInput(Unpooled.wrappedBuffer(lengthBytes)), buf.readableBytes());
          if (buf.readableBytes() < length) {
            buf.resetReaderIndex();
            return;
          } 
          out.add(buf.readBytes(length));
          return;
        } 
      } 
      throw new CorruptedFrameException("Length is too long.");
    } 
    out.add(buf.readBytes(buf.readableBytes()));
  }
}

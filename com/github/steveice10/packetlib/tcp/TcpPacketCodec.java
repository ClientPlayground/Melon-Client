package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ByteToMessageCodec;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionEvent;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetInput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;
import java.util.List;

public class TcpPacketCodec extends ByteToMessageCodec<Packet> {
  private Session session;
  
  public TcpPacketCodec(Session session) {
    this.session = session;
  }
  
  public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf buf) throws Exception {
    ByteBufNetOutput byteBufNetOutput = new ByteBufNetOutput(buf);
    this.session.getPacketProtocol().getPacketHeader().writePacketId((NetOutput)byteBufNetOutput, this.session.getPacketProtocol().getOutgoingId(packet.getClass()));
    packet.write((NetOutput)byteBufNetOutput);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
    int initial = buf.readerIndex();
    ByteBufNetInput byteBufNetInput = new ByteBufNetInput(buf);
    int id = this.session.getPacketProtocol().getPacketHeader().readPacketId((NetInput)byteBufNetInput);
    if (id == -1) {
      buf.readerIndex(initial);
      return;
    } 
    Packet packet = this.session.getPacketProtocol().createIncomingPacket(id);
    packet.read((NetInput)byteBufNetInput);
    if (buf.readableBytes() > 0)
      throw new IllegalStateException("Packet \"" + packet.getClass().getSimpleName() + "\" not fully read."); 
    if (packet.isPriority())
      this.session.callEvent((SessionEvent)new PacketReceivedEvent(this.session, packet)); 
    out.add(packet);
  }
}

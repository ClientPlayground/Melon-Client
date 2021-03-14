package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.packet.PacketProtocol;
import java.util.Map;

public class TcpServerSession extends TcpSession {
  private Server server;
  
  public TcpServerSession(String host, int port, PacketProtocol protocol, Server server) {
    super(host, port, protocol);
    this.server = server;
  }
  
  public Map<String, Object> getFlags() {
    Map<String, Object> ret = super.getFlags();
    ret.putAll(this.server.getGlobalFlags());
    return ret;
  }
  
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    this.server.addSession(this);
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    this.server.removeSession(this);
  }
}

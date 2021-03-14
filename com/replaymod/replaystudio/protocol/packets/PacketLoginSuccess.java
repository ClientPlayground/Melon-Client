package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import java.io.IOException;

public class PacketLoginSuccess {
  private final String id;
  
  private final String name;
  
  public PacketLoginSuccess(String id, String name) {
    this.id = id;
    this.name = name;
  }
  
  public static PacketLoginSuccess read(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      return new PacketLoginSuccess(in.readString(), in.readString());
    } 
  }
  
  public Packet write(PacketTypeRegistry registry) throws IOException {
    Packet packet = new Packet(registry, PacketType.LoginSuccess);
    try (Packet.Writer out = packet.overwrite()) {
      out.writeString(this.id);
      out.writeString(this.name);
    } 
    return packet;
  }
}

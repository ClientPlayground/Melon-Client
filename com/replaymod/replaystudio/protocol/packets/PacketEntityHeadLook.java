package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import java.io.IOException;

public class PacketEntityHeadLook {
  public static float getYaw(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        in.readVarInt();
      } else {
        in.readInt();
      } 
      return in.readByte() / 256.0F * 360.0F;
    } 
  }
  
  public static Packet write(PacketTypeRegistry registry, int entityId, float yaw) throws IOException {
    Packet packet = new Packet(registry, PacketType.EntityHeadLook);
    try (Packet.Writer out = packet.overwrite()) {
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        out.writeVarInt(entityId);
      } else {
        out.writeInt(entityId);
      } 
      out.writeByte((int)(yaw / 360.0F * 256.0F));
    } 
    return packet;
  }
}

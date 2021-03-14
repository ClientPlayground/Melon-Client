package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.util.Location;
import java.io.IOException;

public class PacketEntityTeleport {
  public static Location getLocation(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        in.readVarInt();
      } else {
        in.readInt();
      } 
      return SpawnEntity.readXYZYaPi(packet, in);
    } 
  }
  
  public static Packet write(PacketTypeRegistry registry, int entityId, Location location, boolean onGround) throws IOException {
    Packet packet = new Packet(registry, PacketType.EntityTeleport);
    try (Packet.Writer out = packet.overwrite()) {
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        out.writeVarInt(entityId);
      } else {
        out.writeInt(entityId);
      } 
      SpawnEntity.writeXYZYaPi(packet, out, location);
      if (packet.atLeast(ProtocolVersion.v1_8))
        out.writeBoolean(onGround); 
    } 
    return packet;
  }
}

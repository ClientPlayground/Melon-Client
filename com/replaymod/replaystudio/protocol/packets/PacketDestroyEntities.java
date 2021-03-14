package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketDestroyEntities {
  public static List<Integer> getEntityIds(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      int len = packet.atLeast(ProtocolVersion.v1_8) ? in.readVarInt() : in.readByte();
      List<Integer> result = new ArrayList<>(len);
      for (int i = 0; i < len; i++)
        result.add(Integer.valueOf(packet.atLeast(ProtocolVersion.v1_8) ? in.readVarInt() : in.readInt())); 
      return result;
    } 
  }
  
  public static Packet write(PacketTypeRegistry registry, int... entityIds) throws IOException {
    Packet packet = new Packet(registry, PacketType.DestroyEntities);
    try (Packet.Writer out = packet.overwrite()) {
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        out.writeVarInt(entityIds.length);
      } else {
        out.writeByte(entityIds.length);
      } 
      for (int entityId : entityIds) {
        if (packet.atLeast(ProtocolVersion.v1_8)) {
          out.writeVarInt(entityId);
        } else {
          out.writeInt(entityId);
        } 
      } 
    } 
    return packet;
  }
}

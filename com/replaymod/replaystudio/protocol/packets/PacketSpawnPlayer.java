package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import java.io.IOException;

public class PacketSpawnPlayer extends SpawnEntity {
  public static String getPlayerListEntryId(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      in.readVarInt();
      if (packet.atLeast(ProtocolVersion.v1_8))
        return in.readUUID().toString(); 
      in.readString();
      return in.readString();
    } 
  }
}

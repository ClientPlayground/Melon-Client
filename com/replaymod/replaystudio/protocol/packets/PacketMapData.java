package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import java.io.IOException;

public class PacketMapData {
  public static int getMapId(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      return in.readVarInt();
    } 
  }
}

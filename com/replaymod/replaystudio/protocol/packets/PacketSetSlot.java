package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import java.io.IOException;

public class PacketSetSlot {
  public static int getWindowId(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      return in.readUnsignedByte();
    } 
  }
  
  public static int getSlot(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      in.readUnsignedByte();
      return in.readShort();
    } 
  }
}

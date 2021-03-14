package me.kaimson.melonclient.ingames.utils.ReplayMod.core;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

public class Restrictions {
  public static final String PLUGIN_CHANNEL = "Replay|Restrict";
  
  public String handle(S3FPacketCustomPayload packet) {
    PacketBuffer buffer = packet.getBufferData();
    if (buffer.isReadable()) {
      String name = buffer.readStringFromBuffer(64);
      boolean active = buffer.readBoolean();
      return name;
    } 
    return null;
  }
}

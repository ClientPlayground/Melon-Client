package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import java.io.IOException;

public class PacketNotifyClient {
  public enum Action {
    INVALID_BED, START_RAIN, STOP_RAIN, CHANGE_GAMEMODE, ENTER_CREDITS, DEMO_MESSAGE, ARROW_HIT_PLAYER, RAIN_STRENGTH, THUNDER_STRENGTH, AFFECTED_BY_PUFFERFISH, AFFECTED_BY_ELDER_GUARDIAN, ENABLE_RESPAWN_SCREEN;
  }
  
  public static Action getAction(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      return Action.values()[in.readUnsignedByte()];
    } 
  }
  
  public static float getValue(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      in.readUnsignedByte();
      return in.readFloat();
    } 
  }
  
  public static Packet write(PacketTypeRegistry registry, Action action, float value) throws IOException {
    Packet packet = new Packet(registry, PacketType.PlayerListEntry);
    try (Packet.Writer out = packet.overwrite()) {
      out.writeByte(action.ordinal());
      out.writeFloat(value);
    } 
    return packet;
  }
}

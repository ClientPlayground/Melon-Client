package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PacketTeam {
  public enum Action {
    CREATE, REMOVE, UPDATE, ADD_PLAYER, REMOVE_PLAYER;
  }
  
  public static String getName(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      return in.readString();
    } 
  }
  
  public static Action getAction(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      in.readString();
      return Action.values()[in.readByte()];
    } 
  }
  
  public static List<String> getPlayers(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      int count;
      in.readString();
      Action action = Action.values()[in.readByte()];
      if (action != Action.CREATE && action != Action.ADD_PLAYER && action != Action.REMOVE_PLAYER)
        return (List)Collections.emptyList(); 
      if (action == Action.CREATE) {
        in.readString();
        if (!packet.atLeast(ProtocolVersion.v1_13)) {
          in.readString();
          in.readString();
        } 
        in.readByte();
        if (packet.atLeast(ProtocolVersion.v1_8)) {
          in.readString();
          if (packet.atLeast(ProtocolVersion.v1_9))
            in.readString(); 
          if (packet.atLeast(ProtocolVersion.v1_13)) {
            in.readVarInt();
            in.readString();
            in.readString();
          } else {
            in.readByte();
          } 
        } 
      } 
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        count = in.readVarInt();
      } else {
        count = in.readShort();
      } 
      List<String> result = new ArrayList<>(count);
      for (int i = 0; i < count; i++)
        result.add(in.readString()); 
      return result;
    } 
  }
  
  public static Packet addPlayers(PacketTypeRegistry registry, String name, Collection<String> players) throws IOException {
    return addOrRemovePlayers(registry, name, Action.ADD_PLAYER, players);
  }
  
  public static Packet removePlayers(PacketTypeRegistry registry, String name, Collection<String> players) throws IOException {
    return addOrRemovePlayers(registry, name, Action.REMOVE_PLAYER, players);
  }
  
  private static Packet addOrRemovePlayers(PacketTypeRegistry registry, String name, Action action, Collection<String> players) throws IOException {
    Packet packet = new Packet(registry, PacketType.Team);
    try (Packet.Writer out = packet.overwrite()) {
      out.writeString(name);
      out.writeByte(action.ordinal());
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        out.writeVarInt(players.size());
      } else {
        out.writeShort(players.size());
      } 
      for (String player : players)
        out.writeString(player); 
    } 
    return packet;
  }
}

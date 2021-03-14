package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Triple;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PacketPlayerListEntry {
  private UUID uuid;
  
  private String name;
  
  private List<Triple<String, String, String>> properties;
  
  private String displayName;
  
  private int gamemode;
  
  private int latency;
  
  public enum Action {
    ADD, GAMEMODE, LATENCY, DISPLAY_NAME, REMOVE;
  }
  
  public static PacketPlayerListEntry updateGamemode(PacketPlayerListEntry entry, int gamemode) {
    entry = new PacketPlayerListEntry(entry);
    entry.gamemode = gamemode;
    return entry;
  }
  
  public static PacketPlayerListEntry updateLatency(PacketPlayerListEntry entry, int latency) {
    entry = new PacketPlayerListEntry(entry);
    entry.latency = latency;
    return entry;
  }
  
  public static PacketPlayerListEntry updateDisplayName(PacketPlayerListEntry entry, String displayName) {
    entry = new PacketPlayerListEntry(entry);
    entry.displayName = displayName;
    return entry;
  }
  
  public static Action getAction(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      if (packet.atLeast(ProtocolVersion.v1_8))
        return Action.values()[in.readVarInt()]; 
      in.readString();
      if (in.readBoolean())
        return Action.ADD; 
      return Action.REMOVE;
    } 
  }
  
  public static List<PacketPlayerListEntry> read(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        Action action = Action.values()[in.readVarInt()];
        int count = in.readVarInt();
        List<PacketPlayerListEntry> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
          int properties, j;
          PacketPlayerListEntry packetPlayerListEntry = new PacketPlayerListEntry();
          packetPlayerListEntry.uuid = in.readUUID();
          switch (action) {
            case ADD:
              packetPlayerListEntry.name = in.readString();
              properties = in.readVarInt();
              packetPlayerListEntry.properties = new ArrayList<>(properties);
              for (j = 0; j < properties; j++) {
                String property = in.readString();
                String value = in.readString();
                String signature = null;
                if (in.readBoolean())
                  signature = in.readString(); 
                packetPlayerListEntry.properties.add(new Triple(property, value, signature));
              } 
              packetPlayerListEntry.gamemode = in.readVarInt();
              packetPlayerListEntry.latency = in.readVarInt();
              if (in.readBoolean())
                packetPlayerListEntry.displayName = in.readString(); 
              break;
            case GAMEMODE:
              packetPlayerListEntry.gamemode = in.readVarInt();
              break;
            case LATENCY:
              packetPlayerListEntry.latency = in.readVarInt();
              break;
            case DISPLAY_NAME:
              if (in.readBoolean())
                packetPlayerListEntry.displayName = in.readString(); 
              break;
          } 
          result.add(packetPlayerListEntry);
        } 
        return result;
      } 
      PacketPlayerListEntry entry = new PacketPlayerListEntry();
      entry.name = in.readString();
      in.readBoolean();
      entry.latency = in.readShort();
      return Collections.singletonList(entry);
    } 
  }
  
  public static List<Packet> write(PacketTypeRegistry registry, Action action, List<PacketPlayerListEntry> entries) throws IOException {
    if (registry.atLeast(ProtocolVersion.v1_8))
      return Collections.singletonList(write_1_8(registry, action, entries)); 
    List<Packet> packets = new ArrayList<>(entries.size());
    for (PacketPlayerListEntry it : entries)
      packets.add(write_1_7(registry, action, it)); 
    return packets;
  }
  
  private static Packet write_1_8(PacketTypeRegistry registry, Action action, List<PacketPlayerListEntry> entries) throws IOException {
    Packet packet = new Packet(registry, PacketType.PlayerListEntry);
    try (Packet.Writer out = packet.overwrite()) {
      out.writeVarInt(action.ordinal());
      out.writeVarInt(entries.size());
      for (PacketPlayerListEntry entry : entries) {
        out.writeUUID(entry.uuid);
        switch (action) {
          case ADD:
            out.writeString(entry.name);
            out.writeVarInt(entry.properties.size());
            for (Triple<String, String, String> property : entry.properties) {
              out.writeString((String)property.getFirst());
              out.writeString((String)property.getSecond());
              if (property.getThird() != null) {
                out.writeBoolean(true);
                out.writeString((String)property.getThird());
                continue;
              } 
              out.writeBoolean(false);
            } 
            out.writeVarInt(entry.gamemode);
            out.writeVarInt(entry.latency);
            if (entry.displayName != null) {
              out.writeBoolean(true);
              out.writeString(entry.displayName);
              continue;
            } 
            out.writeBoolean(false);
          case GAMEMODE:
            out.writeVarInt(entry.gamemode);
          case LATENCY:
            out.writeVarInt(entry.latency);
          case DISPLAY_NAME:
            if (entry.displayName != null) {
              out.writeBoolean(true);
              out.writeString(entry.displayName);
              continue;
            } 
            out.writeBoolean(false);
        } 
      } 
    } 
    return packet;
  }
  
  private static Packet write_1_7(PacketTypeRegistry registry, Action action, PacketPlayerListEntry entry) throws IOException {
    Packet packet = new Packet(registry, PacketType.PlayerListEntry);
    try (Packet.Writer out = packet.overwrite()) {
      out.writeString(entry.name);
      if (action == Action.ADD) {
        out.writeBoolean(true);
      } else if (action == Action.REMOVE) {
        out.writeBoolean(false);
      } else {
        throw new IllegalStateException("1.7 only supports ADD or REMOVE");
      } 
      out.writeShort(entry.latency);
    } 
    return packet;
  }
  
  private PacketPlayerListEntry() {}
  
  private PacketPlayerListEntry(PacketPlayerListEntry from) {
    this.uuid = from.uuid;
    this.name = from.name;
    this.properties = from.properties;
    this.displayName = from.displayName;
    this.gamemode = from.gamemode;
    this.latency = from.latency;
  }
  
  public UUID getUuid() {
    return this.uuid;
  }
  
  public String getName() {
    return this.name;
  }
  
  public List<Triple<String, String, String>> getProperties() {
    return this.properties;
  }
  
  public String getDisplayName() {
    return this.displayName;
  }
  
  public int getGamemode() {
    return this.gamemode;
  }
  
  public int getLatency() {
    return this.latency;
  }
  
  public String getId() {
    return (this.uuid != null) ? this.uuid.toString() : this.name;
  }
}

package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.util.IPosition;
import com.replaymod.replaystudio.util.Location;
import java.io.IOException;

public class SpawnEntity {
  public static Location getLocation(Packet packet) throws IOException {
    PacketType type = packet.getType();
    switch (type) {
      case SpawnExpOrb:
        try (Packet.Reader in = packet.reader()) {
          in.readVarInt();
          if (packet.atLeast(ProtocolVersion.v1_9))
            return new Location(in.readDouble(), in.readDouble(), in.readDouble(), 0.0F, 0.0F); 
          return new Location(in.readInt() / 32.0D, in.readInt() / 32.0D, in.readInt() / 32.0D, 0.0F, 0.0F);
        } 
      case SpawnObject:
      case SpawnMob:
        try (Packet.Reader in = packet.reader()) {
          in.readVarInt();
          if (packet.atLeast(ProtocolVersion.v1_9))
            in.readUUID(); 
          if (packet.atLeast(ProtocolVersion.v1_11)) {
            in.readVarInt();
          } else {
            in.readUnsignedByte();
          } 
          return readXYZYaPi(packet, in);
        } 
      case SpawnPlayer:
        try (Packet.Reader in = packet.reader()) {
          in.readVarInt();
          if (packet.atLeast(ProtocolVersion.v1_8)) {
            in.readUUID();
          } else {
            in.readString();
            in.readString();
            int properties = in.readVarInt();
            for (int i = 0; i < properties; i++) {
              in.readString();
              in.readString();
              in.readString();
            } 
          } 
          return readXYZYaPi(packet, in);
        } 
      case SpawnPainting:
        try (Packet.Reader in = packet.reader()) {
          in.readVarInt();
          if (packet.atLeast(ProtocolVersion.v1_9))
            in.readUUID(); 
          if (packet.atLeast(ProtocolVersion.v1_13)) {
            in.readVarInt();
          } else {
            in.readString();
          } 
          if (packet.atLeast(ProtocolVersion.v1_8)) {
            IPosition pos = in.readPosition();
            return new Location(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
          } 
          return new Location(in.readInt(), in.readInt(), in.readInt(), 0.0F, 0.0F);
        } 
    } 
    return null;
  }
  
  static Location readXYZYaPi(Packet packet, Packet.Reader in) throws IOException {
    if (packet.atLeast(ProtocolVersion.v1_9))
      return new Location(in.readDouble(), in.readDouble(), in.readDouble(), in
          .readByte() / 256.0F * 360.0F, in.readByte() / 256.0F * 360.0F); 
    return new Location(in.readInt() / 32.0D, in.readInt() / 32.0D, in.readInt() / 32.0D, in
        .readByte() / 256.0F * 360.0F, in.readByte() / 256.0F * 360.0F);
  }
  
  static void writeXYZYaPi(Packet packet, Packet.Writer out, Location loc) throws IOException {
    if (packet.atLeast(ProtocolVersion.v1_9)) {
      out.writeDouble(loc.getX());
      out.writeDouble(loc.getY());
      out.writeDouble(loc.getZ());
    } else {
      out.writeInt((int)(loc.getX() * 32.0D));
      out.writeInt((int)(loc.getY() * 32.0D));
      out.writeInt((int)(loc.getZ() * 32.0D));
    } 
    out.writeByte((int)(loc.getYaw() / 360.0F * 256.0F));
    out.writeByte((int)(loc.getPitch() / 360.0F * 256.0F));
  }
}

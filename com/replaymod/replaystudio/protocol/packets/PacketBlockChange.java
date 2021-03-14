package com.replaymod.replaystudio.protocol.packets;

import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.protocol.ProtocolVersion;
import com.replaymod.replaystudio.util.IPosition;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PacketBlockChange {
  private IPosition pos;
  
  private int id;
  
  private PacketBlockChange() {}
  
  public PacketBlockChange(IPosition pos, int id) {
    this.pos = pos;
    this.id = id;
  }
  
  public static PacketBlockChange read(Packet packet) throws IOException {
    PacketBlockChange p = new PacketBlockChange();
    try (Packet.Reader in = packet.reader()) {
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        p.pos = in.readPosition();
        p.id = in.readVarInt();
      } else {
        int x = in.readInt();
        int y = in.readUnsignedByte();
        int z = in.readInt();
        p.pos = new IPosition(x, y, z);
        p.id = in.readVarInt() << 4 | in.readUnsignedByte() & 0xF;
      } 
    } 
    return p;
  }
  
  public static Packet write(PacketTypeRegistry registry, IPosition pos, int id) throws IOException {
    return (new PacketBlockChange(pos, id)).write(registry);
  }
  
  public Packet write(PacketTypeRegistry registry) throws IOException {
    Packet packet = new Packet(registry, PacketType.BlockChange);
    try (Packet.Writer out = packet.overwrite()) {
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        out.writePosition(this.pos);
        out.writeVarInt(this.id);
      } else {
        out.writeInt(this.pos.getX());
        out.writeByte(this.pos.getY());
        out.writeInt(this.pos.getZ());
        out.writeVarInt(this.id >> 4);
        out.writeByte(this.id & 0xF);
      } 
    } 
    return packet;
  }
  
  public static List<PacketBlockChange> readBulk(Packet packet) throws IOException {
    try (Packet.Reader in = packet.reader()) {
      PacketBlockChange[] result;
      int chunkX = in.readInt();
      int chunkZ = in.readInt();
      if (packet.atLeast(ProtocolVersion.v1_8)) {
        result = new PacketBlockChange[in.readVarInt()];
      } else {
        result = new PacketBlockChange[in.readShort()];
        in.readInt();
      } 
      for (int index = 0; index < result.length; index++) {
        PacketBlockChange p = new PacketBlockChange();
        short coords = in.readShort();
        int x = (chunkX << 4) + (coords >> 12 & 0xF);
        int y = coords & 0xFF;
        int z = (chunkZ << 4) + (coords >> 8 & 0xF);
        p.pos = new IPosition(x, y, z);
        if (packet.atLeast(ProtocolVersion.v1_8)) {
          p.id = in.readVarInt();
        } else {
          p.id = in.readShort();
        } 
        result[index] = p;
      } 
      return Arrays.asList(result);
    } 
  }
  
  public static List<PacketBlockChange> readSingleOrBulk(Packet packet) throws IOException {
    if (packet.getType() == PacketType.BlockChange)
      return Collections.singletonList(read(packet)); 
    return readBulk(packet);
  }
  
  public IPosition getPosition() {
    return this.pos;
  }
  
  public int getId() {
    return this.id;
  }
}

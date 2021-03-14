package com.replaymod.replaystudio.protocol.packets;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketUpdateLight {
  private static final byte[] EMPTY = new byte[2048];
  
  private int x;
  
  private int z;
  
  private List<byte[]> skyLight;
  
  private List<byte[]> blockLight;
  
  public static PacketUpdateLight read(Packet packet) throws IOException {
    if (packet.getType() != PacketType.UpdateLight)
      throw new IllegalArgumentException("Can only read packets of type UpdateLight."); 
    PacketUpdateLight updateLight = new PacketUpdateLight();
    try (Packet.Reader reader = packet.reader()) {
      updateLight.read((NetInput)reader);
    } 
    return updateLight;
  }
  
  public Packet write(PacketTypeRegistry registry) throws IOException {
    Packet packet = new Packet(registry, PacketType.UpdateLight);
    try (Packet.Writer writer = packet.overwrite()) {
      write((NetOutput)writer);
    } 
    return packet;
  }
  
  private PacketUpdateLight() {}
  
  public PacketUpdateLight(int x, int z, List<byte[]> skyLight, List<byte[]> blockLight) {
    if (skyLight.size() != 18)
      throw new IllegalArgumentException("skyLight must have exactly 18 entries (null entries are permitted)"); 
    if (blockLight.size() != 18)
      throw new IllegalArgumentException("blockLight must have exactly 18 entries (null entries are permitted)"); 
    this.x = x;
    this.z = z;
    this.skyLight = skyLight;
    this.blockLight = blockLight;
  }
  
  public int getX() {
    return this.x;
  }
  
  public int getZ() {
    return this.z;
  }
  
  public List<byte[]> getSkyLight() {
    return this.skyLight;
  }
  
  public List<byte[]> getBlockLight() {
    return this.blockLight;
  }
  
  private void read(NetInput in) throws IOException {
    this.x = in.readVarInt();
    this.z = in.readVarInt();
    int skyLightMask = in.readVarInt();
    int blockLightMask = in.readVarInt();
    int emptySkyLightMask = in.readVarInt();
    int emptyBlockLightMask = in.readVarInt();
    this.skyLight = (List)new ArrayList<>(18);
    int i;
    for (i = 0; i < 18; i++) {
      if ((skyLightMask & 1 << i) != 0) {
        if (in.readVarInt() != 2048)
          throw new IOException("Expected sky light byte array to be of length 2048"); 
        this.skyLight.add(in.readBytes(2048));
      } else if ((emptySkyLightMask & 1 << i) != 0) {
        this.skyLight.add(new byte[2048]);
      } else {
        this.skyLight.add(null);
      } 
    } 
    this.blockLight = (List)new ArrayList<>(18);
    for (i = 0; i < 18; i++) {
      if ((blockLightMask & 1 << i) != 0) {
        if (in.readVarInt() != 2048)
          throw new IOException("Expected block light byte array to be of length 2048"); 
        this.blockLight.add(in.readBytes(2048));
      } else if ((emptyBlockLightMask & 1 << i) != 0) {
        this.blockLight.add(new byte[2048]);
      } else {
        this.blockLight.add(null);
      } 
    } 
  }
  
  private void write(NetOutput out) throws IOException {
    out.writeVarInt(this.x);
    out.writeVarInt(this.z);
    int skyLightMask = 0;
    int blockLightMask = 0;
    int emptySkyLightMask = 0;
    int emptyBlockLightMask = 0;
    int i;
    for (i = 0; i < 18; i++) {
      byte[] skyLight = this.skyLight.get(i);
      if (skyLight != null)
        if (Arrays.equals(EMPTY, skyLight)) {
          emptySkyLightMask |= 1 << i;
        } else {
          skyLightMask |= 1 << i;
        }  
      byte[] blockLight = this.blockLight.get(i);
      if (blockLight != null)
        if (Arrays.equals(EMPTY, blockLight)) {
          emptyBlockLightMask |= 1 << i;
        } else {
          blockLightMask |= 1 << i;
        }  
    } 
    out.writeVarInt(skyLightMask);
    out.writeVarInt(blockLightMask);
    out.writeVarInt(emptySkyLightMask);
    out.writeVarInt(emptyBlockLightMask);
    for (i = 0; i < 18; i++) {
      if ((skyLightMask & 1 << i) != 0) {
        out.writeVarInt(2048);
        out.writeBytes(this.skyLight.get(i));
      } 
    } 
    for (i = 0; i < 18; i++) {
      if ((blockLightMask & 1 << i) != 0) {
        out.writeVarInt(2048);
        out.writeBytes(this.blockLight.get(i));
      } 
    } 
  }
}

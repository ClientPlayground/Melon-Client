package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S1FPacketSetExperience implements Packet<INetHandlerPlayClient> {
  private float field_149401_a;
  
  private int totalExperience;
  
  private int level;
  
  public S1FPacketSetExperience() {}
  
  public S1FPacketSetExperience(float p_i45222_1_, int totalExperienceIn, int levelIn) {
    this.field_149401_a = p_i45222_1_;
    this.totalExperience = totalExperienceIn;
    this.level = levelIn;
  }
  
  public void readPacketData(PacketBuffer buf) throws IOException {
    this.field_149401_a = buf.readFloat();
    this.level = buf.readVarIntFromBuffer();
    this.totalExperience = buf.readVarIntFromBuffer();
  }
  
  public void writePacketData(PacketBuffer buf) throws IOException {
    buf.writeFloat(this.field_149401_a);
    buf.writeVarIntToBuffer(this.level);
    buf.writeVarIntToBuffer(this.totalExperience);
  }
  
  public void processPacket(INetHandlerPlayClient handler) {
    handler.handleSetExperience(this);
  }
  
  public float func_149397_c() {
    return this.field_149401_a;
  }
  
  public int getTotalExperience() {
    return this.totalExperience;
  }
  
  public int getLevel() {
    return this.level;
  }
}

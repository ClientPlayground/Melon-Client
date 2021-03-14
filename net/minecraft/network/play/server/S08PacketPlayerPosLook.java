package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S08PacketPlayerPosLook implements Packet<INetHandlerPlayClient> {
  private double x;
  
  private double y;
  
  private double z;
  
  private float yaw;
  
  private float pitch;
  
  private Set<EnumFlags> field_179835_f;
  
  public S08PacketPlayerPosLook() {}
  
  public S08PacketPlayerPosLook(double xIn, double yIn, double zIn, float yawIn, float pitchIn, Set<EnumFlags> p_i45993_9_) {
    this.x = xIn;
    this.y = yIn;
    this.z = zIn;
    this.yaw = yawIn;
    this.pitch = pitchIn;
    this.field_179835_f = p_i45993_9_;
  }
  
  public void readPacketData(PacketBuffer buf) throws IOException {
    this.x = buf.readDouble();
    this.y = buf.readDouble();
    this.z = buf.readDouble();
    this.yaw = buf.readFloat();
    this.pitch = buf.readFloat();
    this.field_179835_f = EnumFlags.func_180053_a(buf.readUnsignedByte());
  }
  
  public void writePacketData(PacketBuffer buf) throws IOException {
    buf.writeDouble(this.x);
    buf.writeDouble(this.y);
    buf.writeDouble(this.z);
    buf.writeFloat(this.yaw);
    buf.writeFloat(this.pitch);
    buf.writeByte(EnumFlags.func_180056_a(this.field_179835_f));
  }
  
  public void processPacket(INetHandlerPlayClient handler) {
    handler.handlePlayerPosLook(this);
  }
  
  public double getX() {
    return this.x;
  }
  
  public double getY() {
    return this.y;
  }
  
  public double getZ() {
    return this.z;
  }
  
  public float getYaw() {
    return this.yaw;
  }
  
  public float getPitch() {
    return this.pitch;
  }
  
  public Set<EnumFlags> func_179834_f() {
    return this.field_179835_f;
  }
  
  public enum EnumFlags {
    X(0),
    Y(1),
    Z(2),
    Y_ROT(3),
    X_ROT(4);
    
    private int field_180058_f;
    
    EnumFlags(int p_i45992_3_) {
      this.field_180058_f = p_i45992_3_;
    }
    
    private int func_180055_a() {
      return 1 << this.field_180058_f;
    }
    
    private boolean func_180054_b(int p_180054_1_) {
      return ((p_180054_1_ & func_180055_a()) == func_180055_a());
    }
    
    public static Set<EnumFlags> func_180053_a(int p_180053_0_) {
      Set<EnumFlags> set = EnumSet.noneOf(EnumFlags.class);
      for (EnumFlags s08packetplayerposlook$enumflags : values()) {
        if (s08packetplayerposlook$enumflags.func_180054_b(p_180053_0_))
          set.add(s08packetplayerposlook$enumflags); 
      } 
      return set;
    }
    
    public static int func_180056_a(Set<EnumFlags> p_180056_0_) {
      int i = 0;
      for (EnumFlags s08packetplayerposlook$enumflags : p_180056_0_)
        i |= s08packetplayerposlook$enumflags.func_180055_a(); 
      return i;
    }
  }
}

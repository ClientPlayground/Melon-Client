package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Vector;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class VectorType extends Type<Vector> {
  public VectorType() {
    super(Vector.class);
  }
  
  public Vector read(ByteBuf buffer) throws Exception {
    int x = ((Integer)Type.INT.read(buffer)).intValue();
    int y = ((Integer)Type.INT.read(buffer)).intValue();
    int z = ((Integer)Type.INT.read(buffer)).intValue();
    return new Vector(x, y, z);
  }
  
  public void write(ByteBuf buffer, Vector object) throws Exception {
    Type.INT.write(buffer, Integer.valueOf(object.getBlockX()));
    Type.INT.write(buffer, Integer.valueOf(object.getBlockY()));
    Type.INT.write(buffer, Integer.valueOf(object.getBlockZ()));
  }
}

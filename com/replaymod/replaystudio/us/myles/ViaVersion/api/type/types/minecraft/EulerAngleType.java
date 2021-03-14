package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.EulerAngle;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class EulerAngleType extends Type<EulerAngle> {
  public EulerAngleType() {
    super(EulerAngle.class);
  }
  
  public EulerAngle read(ByteBuf buffer) throws Exception {
    float x = ((Float)Type.FLOAT.read(buffer)).floatValue();
    float y = ((Float)Type.FLOAT.read(buffer)).floatValue();
    float z = ((Float)Type.FLOAT.read(buffer)).floatValue();
    return new EulerAngle(x, y, z);
  }
  
  public void write(ByteBuf buffer, EulerAngle object) throws Exception {
    Type.FLOAT.write(buffer, Float.valueOf(object.getX()));
    Type.FLOAT.write(buffer, Float.valueOf(object.getY()));
    Type.FLOAT.write(buffer, Float.valueOf(object.getZ()));
  }
}

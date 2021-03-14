package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class OptionalVarIntType extends Type<Integer> {
  public OptionalVarIntType() {
    super(Integer.class);
  }
  
  public Integer read(ByteBuf buffer) throws Exception {
    int read = ((Integer)Type.VAR_INT.read(buffer)).intValue();
    if (read == 0)
      return null; 
    return Integer.valueOf(read - 1);
  }
  
  public void write(ByteBuf buffer, Integer object) throws Exception {
    if (object == null) {
      Type.VAR_INT.write(buffer, Integer.valueOf(0));
    } else {
      Type.VAR_INT.write(buffer, Integer.valueOf(object.intValue() + 1));
    } 
  }
}

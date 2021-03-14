package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.VillagerData;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class VillagerDataType extends Type<VillagerData> {
  public VillagerDataType() {
    super(VillagerData.class);
  }
  
  public VillagerData read(ByteBuf buffer) throws Exception {
    return new VillagerData(((Integer)Type.VAR_INT.read(buffer)).intValue(), ((Integer)Type.VAR_INT.read(buffer)).intValue(), ((Integer)Type.VAR_INT.read(buffer)).intValue());
  }
  
  public void write(ByteBuf buffer, VillagerData object) throws Exception {
    Type.VAR_INT.write(buffer, Integer.valueOf(object.getType()));
    Type.VAR_INT.write(buffer, Integer.valueOf(object.getProfession()));
    Type.VAR_INT.write(buffer, Integer.valueOf(object.getLevel()));
  }
}

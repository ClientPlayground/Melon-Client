package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class Position1_14Type extends Type<Position> {
  public Position1_14Type() {
    super(Position.class);
  }
  
  public Position read(ByteBuf buffer) {
    long val = buffer.readLong();
    long x = val >> 38L;
    long y = val << 52L >> 52L;
    long z = val << 26L >> 38L;
    return new Position(Long.valueOf(x), Long.valueOf(y), Long.valueOf(z));
  }
  
  public void write(ByteBuf buffer, Position object) {
    buffer.writeLong((object.getX().longValue() & 0x3FFFFFFL) << 38L | object.getY().longValue() & 0xFFFL | (object.getZ().longValue() & 0x3FFFFFFL) << 12L);
  }
}

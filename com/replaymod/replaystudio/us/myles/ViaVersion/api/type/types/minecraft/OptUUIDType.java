package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import java.util.UUID;

public class OptUUIDType extends Type<UUID> {
  public OptUUIDType() {
    super(UUID.class);
  }
  
  public UUID read(ByteBuf buffer) {
    boolean present = buffer.readBoolean();
    if (!present)
      return null; 
    return new UUID(buffer.readLong(), buffer.readLong());
  }
  
  public void write(ByteBuf buffer, UUID object) {
    if (object == null) {
      buffer.writeBoolean(false);
    } else {
      buffer.writeBoolean(true);
      buffer.writeLong(object.getMostSignificantBits());
      buffer.writeLong(object.getLeastSignificantBits());
    } 
  }
}

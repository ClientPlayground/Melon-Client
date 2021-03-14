package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import java.util.UUID;

public class UUIDType extends Type<UUID> {
  public UUIDType() {
    super(UUID.class);
  }
  
  public UUID read(ByteBuf buffer) {
    return new UUID(buffer.readLong(), buffer.readLong());
  }
  
  public void write(ByteBuf buffer, UUID object) {
    buffer.writeLong(object.getMostSignificantBits());
    buffer.writeLong(object.getLeastSignificantBits());
  }
}

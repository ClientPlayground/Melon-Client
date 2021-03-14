package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.TypeConverter;

public class VoidType extends Type<Void> implements TypeConverter<Void> {
  public VoidType() {
    super(Void.class);
  }
  
  public Void read(ByteBuf buffer) {
    return null;
  }
  
  public void write(ByteBuf buffer, Void object) {}
  
  public Void from(Object o) {
    return null;
  }
}

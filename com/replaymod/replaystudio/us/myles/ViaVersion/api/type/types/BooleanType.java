package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.TypeConverter;

public class BooleanType extends Type<Boolean> implements TypeConverter<Boolean> {
  public BooleanType() {
    super(Boolean.class);
  }
  
  public Boolean read(ByteBuf buffer) {
    return Boolean.valueOf(buffer.readBoolean());
  }
  
  public void write(ByteBuf buffer, Boolean object) {
    buffer.writeBoolean(object.booleanValue());
  }
  
  public Boolean from(Object o) {
    if (o instanceof Number)
      return Boolean.valueOf((((Number)o).intValue() == 1)); 
    return (Boolean)o;
  }
}

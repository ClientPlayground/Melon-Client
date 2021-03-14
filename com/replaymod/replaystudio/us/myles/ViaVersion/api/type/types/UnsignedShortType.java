package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.TypeConverter;

public class UnsignedShortType extends Type<Integer> implements TypeConverter<Integer> {
  public UnsignedShortType() {
    super(Integer.class);
  }
  
  public Integer read(ByteBuf buffer) {
    return Integer.valueOf(buffer.readUnsignedShort());
  }
  
  public void write(ByteBuf buffer, Integer object) {
    buffer.writeShort(object.intValue());
  }
  
  public Integer from(Object o) {
    if (o instanceof Number)
      return Integer.valueOf(((Number)o).intValue()); 
    if (o instanceof Boolean)
      return Integer.valueOf(((Boolean)o).booleanValue() ? 1 : 0); 
    return (Integer)o;
  }
}

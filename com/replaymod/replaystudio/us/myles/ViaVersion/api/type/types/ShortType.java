package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.TypeConverter;

public class ShortType extends Type<Short> implements TypeConverter<Short> {
  public ShortType() {
    super(Short.class);
  }
  
  public Short read(ByteBuf buffer) {
    return Short.valueOf(buffer.readShort());
  }
  
  public void write(ByteBuf buffer, Short object) {
    buffer.writeShort(object.shortValue());
  }
  
  public Short from(Object o) {
    if (o instanceof Number)
      return Short.valueOf(((Number)o).shortValue()); 
    if (o instanceof Boolean)
      return Short.valueOf(((Boolean)o).booleanValue() ? 1 : 0); 
    return Short.valueOf(((Short)o).shortValue());
  }
}

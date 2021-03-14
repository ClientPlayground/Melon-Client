package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.TypeConverter;

public class FloatType extends Type<Float> implements TypeConverter<Float> {
  public FloatType() {
    super(Float.class);
  }
  
  public Float read(ByteBuf buffer) {
    return Float.valueOf(buffer.readFloat());
  }
  
  public void write(ByteBuf buffer, Float object) {
    buffer.writeFloat(object.floatValue());
  }
  
  public Float from(Object o) {
    if (o instanceof Number)
      return Float.valueOf(((Number)o).floatValue()); 
    if (o instanceof Boolean)
      return Float.valueOf(((Boolean)o).booleanValue() ? 1.0F : 0.0F); 
    return (Float)o;
  }
}

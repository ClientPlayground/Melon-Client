package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.TypeConverter;

public class DoubleType extends Type<Double> implements TypeConverter<Double> {
  public DoubleType() {
    super(Double.class);
  }
  
  public Double read(ByteBuf buffer) {
    return Double.valueOf(buffer.readDouble());
  }
  
  public void write(ByteBuf buffer, Double object) {
    buffer.writeDouble(object.doubleValue());
  }
  
  public Double from(Object o) {
    if (o instanceof Number)
      return Double.valueOf(((Number)o).doubleValue()); 
    if (o instanceof Boolean)
      return Double.valueOf(((Boolean)o).booleanValue() ? 1.0D : 0.0D); 
    return (Double)o;
  }
}

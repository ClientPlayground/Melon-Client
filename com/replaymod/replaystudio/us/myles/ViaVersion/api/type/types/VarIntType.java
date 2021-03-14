package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.TypeConverter;

public class VarIntType extends Type<Integer> implements TypeConverter<Integer> {
  public VarIntType() {
    super("VarInt", Integer.class);
  }
  
  public void write(ByteBuf buffer, Integer object) {
    do {
      int part = object.intValue() & 0x7F;
      object = Integer.valueOf(object.intValue() >>> 7);
      if (object.intValue() != 0)
        part |= 0x80; 
      buffer.writeByte(part);
    } while (object.intValue() != 0);
  }
  
  public Integer read(ByteBuf buffer) {
    byte in;
    int out = 0;
    int bytes = 0;
    do {
      in = buffer.readByte();
      out |= (in & Byte.MAX_VALUE) << bytes++ * 7;
      if (bytes > 5)
        throw new RuntimeException("VarInt too big"); 
    } while ((in & 0x80) == 128);
    return Integer.valueOf(out);
  }
  
  public Integer from(Object o) {
    if (o instanceof Number)
      return Integer.valueOf(((Number)o).intValue()); 
    if (o instanceof Boolean)
      return Integer.valueOf(((Boolean)o).booleanValue() ? 1 : 0); 
    return (Integer)o;
  }
}

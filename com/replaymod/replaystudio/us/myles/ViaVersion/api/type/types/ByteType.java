package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.TypeConverter;

public class ByteType extends Type<Byte> implements TypeConverter<Byte> {
  public ByteType() {
    super(Byte.class);
  }
  
  public Byte read(ByteBuf buffer) {
    return Byte.valueOf(buffer.readByte());
  }
  
  public void write(ByteBuf buffer, Byte object) {
    buffer.writeByte(object.byteValue());
  }
  
  public Byte from(Object o) {
    if (o instanceof Number)
      return Byte.valueOf(((Number)o).byteValue()); 
    if (o instanceof Boolean)
      return Byte.valueOf(((Boolean)o).booleanValue() ? 1 : 0); 
    return (Byte)o;
  }
}

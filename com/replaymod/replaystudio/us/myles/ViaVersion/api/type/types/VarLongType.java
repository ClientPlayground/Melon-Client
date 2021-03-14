package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.TypeConverter;

public class VarLongType extends Type<Long> implements TypeConverter<Long> {
  public VarLongType() {
    super("VarLong", Long.class);
  }
  
  public void write(ByteBuf buffer, Long object) {
    do {
      int part = (int)(object.longValue() & 0x7FL);
      object = Long.valueOf(object.longValue() >>> 7L);
      if (object.longValue() != 0L)
        part |= 0x80; 
      buffer.writeByte(part);
    } while (object.longValue() != 0L);
  }
  
  public Long read(ByteBuf buffer) {
    byte in;
    long out = 0L;
    int bytes = 0;
    do {
      in = buffer.readByte();
      out |= ((in & Byte.MAX_VALUE) << bytes++ * 7);
      if (bytes > 10)
        throw new RuntimeException("VarLong too big"); 
    } while ((in & 0x80) == 128);
    return Long.valueOf(out);
  }
  
  public Long from(Object o) {
    if (o instanceof Number)
      return Long.valueOf(((Number)o).longValue()); 
    if (o instanceof Boolean)
      return Long.valueOf(((Boolean)o).booleanValue() ? 1L : 0L); 
    return (Long)o;
  }
}

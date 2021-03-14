package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.TypeConverter;

public class LongType extends Type<Long> implements TypeConverter<Long> {
  public LongType() {
    super(Long.class);
  }
  
  public Long read(ByteBuf buffer) {
    return Long.valueOf(buffer.readLong());
  }
  
  public void write(ByteBuf buffer, Long object) {
    buffer.writeLong(object.longValue());
  }
  
  public Long from(Object o) {
    if (o instanceof Number)
      return Long.valueOf(((Number)o).longValue()); 
    if (o instanceof Boolean)
      return Long.valueOf(((Boolean)o).booleanValue() ? 1L : 0L); 
    return (Long)o;
  }
}

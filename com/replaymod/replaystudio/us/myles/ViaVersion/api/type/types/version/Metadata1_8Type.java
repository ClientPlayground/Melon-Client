package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_8;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.MetaTypeTemplate;

public class Metadata1_8Type extends MetaTypeTemplate {
  public Metadata read(ByteBuf buffer) throws Exception {
    byte item = buffer.readByte();
    if (item == Byte.MAX_VALUE)
      return null; 
    int typeID = (item & 0xE0) >> 5;
    MetaType1_8 type = MetaType1_8.byId(typeID);
    int id = item & 0x1F;
    return new Metadata(id, (MetaType)type, type.getType().read(buffer));
  }
  
  public void write(ByteBuf buffer, Metadata meta) throws Exception {
    byte item = (byte)(meta.getMetaType().getTypeID() << 5 | meta.getId() & 0x1F);
    buffer.writeByte(item);
    meta.getMetaType().getType().write(buffer, meta.getValue());
  }
}

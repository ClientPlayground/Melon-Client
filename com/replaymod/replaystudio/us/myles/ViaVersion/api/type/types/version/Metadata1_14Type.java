package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_14;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.MetaTypeTemplate;

public class Metadata1_14Type extends MetaTypeTemplate {
  public Metadata read(ByteBuf buffer) throws Exception {
    short index = buffer.readUnsignedByte();
    if (index == 255)
      return null; 
    MetaType1_14 type = MetaType1_14.byId(buffer.readByte());
    return new Metadata(index, (MetaType)type, type.getType().read(buffer));
  }
  
  public void write(ByteBuf buffer, Metadata object) throws Exception {
    if (object == null) {
      buffer.writeByte(255);
    } else {
      buffer.writeByte(object.getId());
      buffer.writeByte(object.getMetaType().getTypeID());
      object.getMetaType().getType().write(buffer, object.getValue());
    } 
  }
}

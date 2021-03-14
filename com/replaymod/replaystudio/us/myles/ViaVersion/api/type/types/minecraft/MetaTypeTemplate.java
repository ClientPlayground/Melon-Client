package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public abstract class MetaTypeTemplate extends Type<Metadata> {
  public MetaTypeTemplate() {
    super("Metadata type", Metadata.class);
  }
  
  public Class<? extends Type> getBaseClass() {
    return (Class)MetaTypeTemplate.class;
  }
}

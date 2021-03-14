package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import java.util.List;

public abstract class MetaListTypeTemplate extends Type<List<Metadata>> {
  public MetaListTypeTemplate() {
    super("MetaData List", List.class);
  }
  
  public Class<? extends Type> getBaseClass() {
    return (Class)MetaListTypeTemplate.class;
  }
}

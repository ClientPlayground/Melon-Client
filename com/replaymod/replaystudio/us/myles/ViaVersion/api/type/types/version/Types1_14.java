package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.Particle;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.Particle1_14Type;
import java.util.List;

public class Types1_14 {
  public static final Type<List<Metadata>> METADATA_LIST = (Type<List<Metadata>>)new MetadataList1_14Type();
  
  public static final Type<Metadata> METADATA = (Type<Metadata>)new Metadata1_14Type();
  
  public static final Type<Particle> PARTICLE = (Type<Particle>)new Particle1_14Type();
}

package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.Particle;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.types.Particle1_13_2Type;
import java.util.List;

public class Types1_13_2 {
  public static final Type<List<Metadata>> METADATA_LIST = (Type<List<Metadata>>)new MetadataList1_13_2Type();
  
  public static final Type<Metadata> METADATA = (Type<Metadata>)new Metadata1_13_2Type();
  
  public static Type<Particle> PARTICLE = (Type<Particle>)new Particle1_13_2Type();
}

package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import java.util.List;

public class Types1_9 {
  public static final Type<List<Metadata>> METADATA_LIST = (Type<List<Metadata>>)new MetadataList1_9Type();
  
  public static final Type<Metadata> METADATA = (Type<Metadata>)new Metadata1_9Type();
  
  public static final Type<ChunkSection> CHUNK_SECTION = new ChunkSectionType1_9();
}

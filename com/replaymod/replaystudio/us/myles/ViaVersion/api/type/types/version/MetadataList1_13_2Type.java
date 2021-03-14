package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.MetaListTypeTemplate;
import java.util.ArrayList;
import java.util.List;

public class MetadataList1_13_2Type extends MetaListTypeTemplate {
  public List<Metadata> read(ByteBuf buffer) throws Exception {
    Metadata meta;
    List<Metadata> list = new ArrayList<>();
    do {
      meta = (Metadata)Types1_13_2.METADATA.read(buffer);
      if (meta == null)
        continue; 
      list.add(meta);
    } while (meta != null);
    return list;
  }
  
  public void write(ByteBuf buffer, List<Metadata> object) throws Exception {
    for (Metadata m : object)
      Types1_13_2.METADATA.write(buffer, m); 
    Types1_13_2.METADATA.write(buffer, null);
  }
}

package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.MetaListTypeTemplate;
import java.util.ArrayList;
import java.util.List;

public class MetadataList1_8Type extends MetaListTypeTemplate {
  public List<Metadata> read(ByteBuf buffer) throws Exception {
    Metadata m;
    List<Metadata> list = new ArrayList<>();
    do {
      m = (Metadata)Types1_8.METADATA.read(buffer);
      if (m == null)
        continue; 
      list.add(m);
    } while (m != null);
    return list;
  }
  
  public void write(ByteBuf buffer, List<Metadata> object) throws Exception {
    for (Metadata data : object)
      Types1_8.METADATA.write(buffer, data); 
    buffer.writeByte(127);
  }
}

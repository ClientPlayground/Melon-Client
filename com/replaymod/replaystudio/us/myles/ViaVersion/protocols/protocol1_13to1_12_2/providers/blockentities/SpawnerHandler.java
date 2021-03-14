package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.EntityNameRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;

public class SpawnerHandler implements BlockEntityProvider.BlockEntityHandler {
  public int transform(UserConnection user, CompoundTag tag) {
    if (tag.contains("SpawnData") && tag.get("SpawnData") instanceof CompoundTag) {
      CompoundTag data = (CompoundTag)tag.get("SpawnData");
      if (data.contains("id") && data.get("id") instanceof StringTag) {
        StringTag s = (StringTag)data.get("id");
        s.setValue(EntityNameRewriter.rewrite(s.getValue()));
      } 
    } 
    return -1;
  }
}

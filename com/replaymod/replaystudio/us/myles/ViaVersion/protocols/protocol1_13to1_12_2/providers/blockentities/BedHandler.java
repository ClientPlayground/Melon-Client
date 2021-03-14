package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockStorage;

public class BedHandler implements BlockEntityProvider.BlockEntityHandler {
  public int transform(UserConnection user, CompoundTag tag) {
    BlockStorage storage = (BlockStorage)user.get(BlockStorage.class);
    Position position = new Position(Long.valueOf(getLong(tag.get("x"))), Long.valueOf(getLong(tag.get("y"))), Long.valueOf(getLong(tag.get("z"))));
    if (!storage.contains(position)) {
      Via.getPlatform().getLogger().warning("Received an bed color update packet, but there is no bed! O_o " + tag);
      return -1;
    } 
    int blockId = storage.get(position).getOriginal() - 972 + 748;
    Tag color = tag.get("color");
    if (color != null)
      blockId += ((Number)color.getValue()).intValue() * 16; 
    return blockId;
  }
  
  private long getLong(Tag tag) {
    return ((Integer)tag.getValue()).longValue();
  }
}

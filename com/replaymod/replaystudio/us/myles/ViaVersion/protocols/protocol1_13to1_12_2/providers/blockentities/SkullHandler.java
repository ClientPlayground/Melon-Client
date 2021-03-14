package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockStorage;

public class SkullHandler implements BlockEntityProvider.BlockEntityHandler {
  private final int SKULL_WALL_START = 5447;
  
  private final int SKULL_END = 5566;
  
  public int transform(UserConnection user, CompoundTag tag) {
    BlockStorage storage = (BlockStorage)user.get(BlockStorage.class);
    Position position = new Position(Long.valueOf(getLong(tag.get("x"))), Long.valueOf(getLong(tag.get("y"))), Long.valueOf(getLong(tag.get("z"))));
    if (!storage.contains(position)) {
      Via.getPlatform().getLogger().warning("Received an head update packet, but there is no head! O_o " + tag);
      return -1;
    } 
    int id = storage.get(position).getOriginal();
    if (id >= 5447 && id <= 5566) {
      Tag skullType = tag.get("SkullType");
      if (skullType != null)
        id += ((Number)tag.get("SkullType").getValue()).intValue() * 20; 
      if (tag.contains("Rot"))
        id += ((Number)tag.get("Rot").getValue()).intValue(); 
    } else {
      Via.getPlatform().getLogger().warning("Why does this block have the skull block entity? " + tag);
      return -1;
    } 
    return id;
  }
  
  private long getLong(Tag tag) {
    return ((Integer)tag.getValue()).longValue();
  }
}

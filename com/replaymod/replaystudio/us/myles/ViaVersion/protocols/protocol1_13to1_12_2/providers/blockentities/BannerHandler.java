package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockStorage;

public class BannerHandler implements BlockEntityProvider.BlockEntityHandler {
  private final int WALL_BANNER_START = 7110;
  
  private final int WALL_BANNER_STOP = 7173;
  
  private final int BANNER_START = 6854;
  
  private final int BANNER_STOP = 7109;
  
  public int transform(UserConnection user, CompoundTag tag) {
    BlockStorage storage = (BlockStorage)user.get(BlockStorage.class);
    Position position = new Position(Long.valueOf(getLong(tag.get("x"))), Long.valueOf(getLong(tag.get("y"))), Long.valueOf(getLong(tag.get("z"))));
    if (!storage.contains(position)) {
      Via.getPlatform().getLogger().warning("Received an banner color update packet, but there is no banner! O_o " + tag);
      return -1;
    } 
    int blockId = storage.get(position).getOriginal();
    Tag base = tag.get("Base");
    int color = 0;
    if (base != null)
      color = ((Number)tag.get("Base").getValue()).intValue(); 
    if (blockId >= 6854 && blockId <= 7109) {
      blockId += (15 - color) * 16;
    } else if (blockId >= 7110 && blockId <= 7173) {
      blockId += (15 - color) * 4;
    } else {
      Via.getPlatform().getLogger().warning("Why does this block have the banner block entity? :(" + tag);
    } 
    if (tag.get("Patterns") instanceof com.github.steveice10.opennbt.tag.builtin.ListTag)
      for (Tag pattern : tag.get("Patterns")) {
        if (pattern instanceof CompoundTag) {
          Tag c = ((CompoundTag)pattern).get("Color");
          if (c instanceof IntTag)
            ((IntTag)c).setValue(15 - ((Integer)c.getValue()).intValue()); 
        } 
      }  
    Tag name = tag.get("CustomName");
    if (name instanceof StringTag)
      ((StringTag)name).setValue(ChatRewriter.legacyTextToJson(((StringTag)name).getValue())); 
    return blockId;
  }
  
  private long getLong(Tag tag) {
    return ((Integer)tag.getValue()).longValue();
  }
}

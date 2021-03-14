package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;

public class CommandBlockHandler implements BlockEntityProvider.BlockEntityHandler {
  public int transform(UserConnection user, CompoundTag tag) {
    Tag name = tag.get("CustomName");
    if (name instanceof StringTag)
      ((StringTag)name).setValue(ChatRewriter.legacyTextToJson(((StringTag)name).getValue())); 
    Tag out = tag.get("LastOutput");
    if (out instanceof StringTag)
      ((StringTag)out).setValue(ChatRewriter.processTranslate(((StringTag)out).getValue())); 
    return -1;
  }
}

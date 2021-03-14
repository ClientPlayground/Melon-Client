package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class FlatItemType extends BaseItemType {
  public FlatItemType() {
    super("FlatItem");
  }
  
  public Item read(ByteBuf buffer) throws Exception {
    short id = buffer.readShort();
    if (id < 0)
      return null; 
    Item item = new Item();
    item.setIdentifier(id);
    item.setAmount(buffer.readByte());
    item.setTag((CompoundTag)Type.NBT.read(buffer));
    return item;
  }
  
  public void write(ByteBuf buffer, Item object) throws Exception {
    if (object == null) {
      buffer.writeShort(-1);
    } else {
      buffer.writeShort(object.getIdentifier());
      buffer.writeByte(object.getAmount());
      Type.NBT.write(buffer, object.getTag());
    } 
  }
}

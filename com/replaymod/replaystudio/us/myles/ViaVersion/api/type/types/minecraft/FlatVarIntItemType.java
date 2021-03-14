package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class FlatVarIntItemType extends BaseItemType {
  public FlatVarIntItemType() {
    super("FlatVarIntItem");
  }
  
  public Item read(ByteBuf buffer) throws Exception {
    boolean present = buffer.readBoolean();
    if (!present)
      return null; 
    Item item = new Item();
    item.setIdentifier(((Integer)Type.VAR_INT.read(buffer)).intValue());
    item.setAmount(buffer.readByte());
    item.setTag((CompoundTag)Type.NBT.read(buffer));
    return item;
  }
  
  public void write(ByteBuf buffer, Item object) throws Exception {
    if (object == null) {
      buffer.writeBoolean(false);
    } else {
      buffer.writeBoolean(true);
      Type.VAR_INT.write(buffer, Integer.valueOf(object.getIdentifier()));
      buffer.writeByte(object.getAmount());
      Type.NBT.write(buffer, object.getTag());
    } 
  }
}

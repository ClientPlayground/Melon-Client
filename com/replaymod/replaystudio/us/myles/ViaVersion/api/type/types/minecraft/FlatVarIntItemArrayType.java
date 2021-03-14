package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class FlatVarIntItemArrayType extends BaseItemArrayType {
  public FlatVarIntItemArrayType() {
    super("Flat Item Array");
  }
  
  public Item[] read(ByteBuf buffer) throws Exception {
    int amount = ((Short)Type.SHORT.read(buffer)).shortValue();
    Item[] array = new Item[amount];
    for (int i = 0; i < amount; i++)
      array[i] = (Item)Type.FLAT_VAR_INT_ITEM.read(buffer); 
    return array;
  }
  
  public void write(ByteBuf buffer, Item[] object) throws Exception {
    Type.SHORT.write(buffer, Short.valueOf((short)object.length));
    for (Item o : object)
      Type.FLAT_VAR_INT_ITEM.write(buffer, o); 
  }
}

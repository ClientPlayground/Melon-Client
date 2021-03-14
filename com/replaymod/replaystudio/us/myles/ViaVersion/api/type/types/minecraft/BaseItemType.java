package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public abstract class BaseItemType extends Type<Item> {
  public BaseItemType() {
    super(Item.class);
  }
  
  public BaseItemType(String typeName) {
    super(typeName, Item.class);
  }
  
  public Class<? extends Type> getBaseClass() {
    return (Class)BaseItemType.class;
  }
}

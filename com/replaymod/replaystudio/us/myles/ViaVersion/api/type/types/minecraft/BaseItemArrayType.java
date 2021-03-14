package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public abstract class BaseItemArrayType extends Type<Item[]> {
  public BaseItemArrayType() {
    super(Item[].class);
  }
  
  public BaseItemArrayType(String typeName) {
    super(typeName, Item[].class);
  }
  
  public Class<? extends Type> getBaseClass() {
    return (Class)BaseItemArrayType.class;
  }
}

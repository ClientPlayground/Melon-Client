package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public enum MetaType1_8 implements MetaType {
  Byte(0, Type.BYTE),
  Short(1, Type.SHORT),
  Int(2, Type.INT),
  Float(3, Type.FLOAT),
  String(4, Type.STRING),
  Slot(5, Type.ITEM),
  Position(6, Type.VECTOR),
  Rotation(7, Type.ROTATION),
  NonExistent(-1, Type.NOTHING);
  
  MetaType1_8(int typeID, Type type) {
    this.typeID = typeID;
    this.type = type;
  }
  
  private final int typeID;
  
  private final Type type;
  
  public int getTypeID() {
    return this.typeID;
  }
  
  public Type getType() {
    return this.type;
  }
  
  public static MetaType1_8 byId(int id) {
    return values()[id];
  }
}

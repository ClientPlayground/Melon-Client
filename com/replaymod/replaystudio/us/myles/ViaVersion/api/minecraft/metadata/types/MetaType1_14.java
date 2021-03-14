package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.types;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_14;

public enum MetaType1_14 implements MetaType {
  Byte(0, Type.BYTE),
  VarInt(1, Type.VAR_INT),
  Float(2, Type.FLOAT),
  String(3, Type.STRING),
  Chat(4, Type.STRING),
  OptChat(5, Type.OPTIONAL_CHAT),
  Slot(6, Type.FLAT_VAR_INT_ITEM),
  Boolean(7, Type.BOOLEAN),
  Vector3F(8, Type.ROTATION),
  Position(9, Type.POSITION1_14),
  OptPosition(10, Type.OPTIONAL_POSITION_1_14),
  Direction(11, Type.VAR_INT),
  OptUUID(12, Type.OPTIONAL_UUID),
  BlockID(13, Type.VAR_INT),
  NBTTag(14, Type.NBT),
  PARTICLE(15, Types1_14.PARTICLE),
  VillagerData(16, Type.VILLAGER_DATA),
  OptVarInt(17, Type.OPTIONAL_VAR_INT),
  Pose(18, Type.VAR_INT),
  Discontinued(99, null);
  
  MetaType1_14(int typeID, Type type) {
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
  
  public static MetaType1_14 byId(int id) {
    return values()[id];
  }
}
package com.replaymod.replaystudio.us.myles.ViaVersion.api.type;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.EulerAngle;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Vector;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.VillagerData;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.item.Item;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.ArrayType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.BooleanType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.ByteType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.DoubleType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.FloatType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.IntType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.LongType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.RemainingBytesType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.ShortType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.StringType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.UUIDType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.UnsignedByteType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.UnsignedShortType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.VarIntType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.VarLongType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.VoidType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.BlockChangeRecordType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.EulerAngleType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.FlatItemArrayType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.FlatItemType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.FlatVarIntItemArrayType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.FlatVarIntItemType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.ItemArrayType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.ItemType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.NBTType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.OptPosition1_14Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.OptPositionType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.OptUUIDType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.OptionalChatType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.OptionalVarIntType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.Position1_14Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.PositionType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.VectorType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.VillagerDataType;
import java.util.UUID;

public abstract class Type<T> implements ByteBufReader<T>, ByteBufWriter<T> {
  public static final Type<Byte> BYTE = (Type<Byte>)new ByteType();
  
  public static final Type<Byte[]> BYTE_ARRAY = (Type<Byte[]>)new ArrayType(BYTE);
  
  public static final Type<byte[]> REMAINING_BYTES = (Type<byte[]>)new RemainingBytesType();
  
  public static final Type<Short> UNSIGNED_BYTE = (Type<Short>)new UnsignedByteType();
  
  public static final Type<Short[]> UNSIGNED_BYTE_ARRAY = (Type<Short[]>)new ArrayType(UNSIGNED_BYTE);
  
  public static final Type<Boolean> BOOLEAN = (Type<Boolean>)new BooleanType();
  
  public static final Type<Boolean[]> BOOLEAN_ARRAY = (Type<Boolean[]>)new ArrayType(BOOLEAN);
  
  public static final Type<Integer> INT = (Type<Integer>)new IntType();
  
  public static final Type<Integer[]> INT_ARRAY = (Type<Integer[]>)new ArrayType(INT);
  
  public static final Type<Double> DOUBLE = (Type<Double>)new DoubleType();
  
  public static final Type<Double[]> DOUBLE_ARRAY = (Type<Double[]>)new ArrayType(DOUBLE);
  
  public static final Type<Long> LONG = (Type<Long>)new LongType();
  
  public static final Type<Long[]> LONG_ARRAY = (Type<Long[]>)new ArrayType(LONG);
  
  public static final Type<Float> FLOAT = (Type<Float>)new FloatType();
  
  public static final Type<Float[]> FLOAT_ARRAY = (Type<Float[]>)new ArrayType(FLOAT);
  
  public static final Type<Short> SHORT = (Type<Short>)new ShortType();
  
  public static final Type<Short[]> SHORT_ARRAY = (Type<Short[]>)new ArrayType(SHORT);
  
  public static final Type<Integer> UNSIGNED_SHORT = (Type<Integer>)new UnsignedShortType();
  
  public static final Type<Integer[]> UNSIGNED_SHORT_ARRAY = (Type<Integer[]>)new ArrayType(UNSIGNED_SHORT);
  
  public static final Type<String> STRING = (Type<String>)new StringType();
  
  public static final Type<String[]> STRING_ARRAY = (Type<String[]>)new ArrayType(STRING);
  
  public static final Type<UUID> UUID = (Type<UUID>)new UUIDType();
  
  public static final Type<UUID[]> UUID_ARRAY = (Type<UUID[]>)new ArrayType(UUID);
  
  public static final Type<Integer> VAR_INT = (Type<Integer>)new VarIntType();
  
  public static final Type<Integer[]> VAR_INT_ARRAY = (Type<Integer[]>)new ArrayType(VAR_INT);
  
  public static final Type<Integer> OPTIONAL_VAR_INT = (Type<Integer>)new OptionalVarIntType();
  
  public static final Type<Long> VAR_LONG = (Type<Long>)new VarLongType();
  
  public static final Type<Long[]> VAR_LONG_ARRAY = (Type<Long[]>)new ArrayType(VAR_LONG);
  
  public static final Type<Void> NOTHING = (Type<Void>)new VoidType();
  
  public static final Type<Position> POSITION = (Type<Position>)new PositionType();
  
  public static final Type<Position> POSITION1_14 = (Type<Position>)new Position1_14Type();
  
  public static final Type<EulerAngle> ROTATION = (Type<EulerAngle>)new EulerAngleType();
  
  public static final Type<Vector> VECTOR = (Type<Vector>)new VectorType();
  
  public static final Type<CompoundTag> NBT = (Type<CompoundTag>)new NBTType();
  
  public static final Type<CompoundTag[]> NBT_ARRAY = (Type<CompoundTag[]>)new ArrayType(NBT);
  
  public static final Type<UUID> OPTIONAL_UUID = (Type<UUID>)new OptUUIDType();
  
  public static final Type<String> OPTIONAL_CHAT = (Type<String>)new OptionalChatType();
  
  public static final Type<Position> OPTIONAL_POSITION = (Type<Position>)new OptPositionType();
  
  public static final Type<Position> OPTIONAL_POSITION_1_14 = (Type<Position>)new OptPosition1_14Type();
  
  public static final Type<Item> ITEM = (Type<Item>)new ItemType();
  
  public static final Type<Item[]> ITEM_ARRAY = (Type<Item[]>)new ItemArrayType();
  
  public static final Type<BlockChangeRecord> BLOCK_CHANGE_RECORD = (Type<BlockChangeRecord>)new BlockChangeRecordType();
  
  public static final Type<BlockChangeRecord[]> BLOCK_CHANGE_RECORD_ARRAY = (Type<BlockChangeRecord[]>)new ArrayType(BLOCK_CHANGE_RECORD);
  
  public static final Type<VillagerData> VILLAGER_DATA = (Type<VillagerData>)new VillagerDataType();
  
  public static final Type<Item> FLAT_ITEM = (Type<Item>)new FlatItemType();
  
  public static final Type<Item> FLAT_VAR_INT_ITEM = (Type<Item>)new FlatVarIntItemType();
  
  public static final Type<Item[]> FLAT_ITEM_ARRAY = (Type<Item[]>)new FlatItemArrayType();
  
  public static final Type<Item[]> FLAT_VAR_INT_ITEM_ARRAY = (Type<Item[]>)new FlatVarIntItemArrayType();
  
  public static final Type<Item[]> FLAT_ITEM_ARRAY_VAR_INT = (Type<Item[]>)new ArrayType(FLAT_ITEM);
  
  public static final Type<Item[]> FLAT_VAR_INT_ITEM_ARRAY_VAR_INT = (Type<Item[]>)new ArrayType(FLAT_VAR_INT_ITEM);
  
  private final Class<? super T> outputClass;
  
  private final String typeName;
  
  public Class<? super T> getOutputClass() {
    return this.outputClass;
  }
  
  public String getTypeName() {
    return this.typeName;
  }
  
  public Type(Class<? super T> outputClass) {
    this(outputClass.getSimpleName(), outputClass);
  }
  
  public Type(String typeName, Class<? super T> outputClass) {
    this.outputClass = outputClass;
    this.typeName = typeName;
  }
  
  public Class<? extends Type> getBaseClass() {
    return (Class)getClass();
  }
  
  public String toString() {
    return "Type|" + getTypeName();
  }
}

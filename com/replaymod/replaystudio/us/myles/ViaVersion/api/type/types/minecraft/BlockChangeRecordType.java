package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class BlockChangeRecordType extends Type<BlockChangeRecord> {
  public BlockChangeRecordType() {
    super(BlockChangeRecord.class);
  }
  
  public BlockChangeRecord read(ByteBuf buffer) throws Exception {
    short horizontal = ((Short)Type.UNSIGNED_BYTE.read(buffer)).shortValue();
    short y = ((Short)Type.UNSIGNED_BYTE.read(buffer)).shortValue();
    int blockId = ((Integer)Type.VAR_INT.read(buffer)).intValue();
    return new BlockChangeRecord(horizontal, y, blockId);
  }
  
  public void write(ByteBuf buffer, BlockChangeRecord object) throws Exception {
    Type.UNSIGNED_BYTE.write(buffer, Short.valueOf(object.getHorizontal()));
    Type.UNSIGNED_BYTE.write(buffer, Short.valueOf(object.getY()));
    Type.VAR_INT.write(buffer, Integer.valueOf(object.getBlockId()));
  }
}

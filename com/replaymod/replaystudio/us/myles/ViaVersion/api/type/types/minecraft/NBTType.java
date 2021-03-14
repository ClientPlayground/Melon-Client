package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufInputStream;
import com.github.steveice10.netty.buffer.ByteBufOutputStream;
import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.tag.TagRegistry;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.common.base.Preconditions;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import java.io.DataInput;
import java.io.DataOutput;

public class NBTType extends Type<CompoundTag> {
  static {
    TagRegistry.unregister(60);
    TagRegistry.unregister(61);
    TagRegistry.unregister(65);
  }
  
  public NBTType() {
    super(CompoundTag.class);
  }
  
  public CompoundTag read(ByteBuf buffer) throws Exception {
    Preconditions.checkArgument((buffer.readableBytes() <= 2097152), "Cannot read NBT (got %s bytes)", new Object[] { Integer.valueOf(buffer.readableBytes()) });
    int readerIndex = buffer.readerIndex();
    byte b = buffer.readByte();
    if (b == 0)
      return null; 
    buffer.readerIndex(readerIndex);
    return (CompoundTag)NBTIO.readTag((DataInput)new ByteBufInputStream(buffer));
  }
  
  public void write(ByteBuf buffer, CompoundTag object) throws Exception {
    if (object == null) {
      buffer.writeByte(0);
    } else {
      ByteBufOutputStream bytebufStream = new ByteBufOutputStream(buffer);
      NBTIO.writeTag((DataOutput)bytebufStream, (Tag)object);
    } 
  }
}

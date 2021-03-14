package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class RemainingBytesType extends Type<byte[]> {
  public RemainingBytesType() {
    super(byte[].class);
  }
  
  public byte[] read(ByteBuf buffer) {
    byte[] array = new byte[buffer.readableBytes()];
    buffer.readBytes(array);
    return array;
  }
  
  public void write(ByteBuf buffer, byte[] object) {
    buffer.writeBytes(object);
  }
}

package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.PartialType;

public class CustomByteType extends PartialType<byte[], Integer> {
  public CustomByteType(Integer param) {
    super(param, byte[].class);
  }
  
  public byte[] read(ByteBuf byteBuf, Integer integer) throws Exception {
    if (byteBuf.readableBytes() < integer.intValue())
      throw new RuntimeException("Readable bytes does not match expected!"); 
    byte[] byteArray = new byte[integer.intValue()];
    byteBuf.readBytes(byteArray);
    return byteArray;
  }
  
  public void write(ByteBuf byteBuf, Integer integer, byte[] bytes) throws Exception {
    byteBuf.writeBytes(bytes);
  }
}

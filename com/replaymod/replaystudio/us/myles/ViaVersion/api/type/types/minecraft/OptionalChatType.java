package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class OptionalChatType extends Type<String> {
  public OptionalChatType() {
    super(String.class);
  }
  
  public String read(ByteBuf buffer) throws Exception {
    boolean present = buffer.readBoolean();
    if (!present)
      return null; 
    return (String)Type.STRING.read(buffer);
  }
  
  public void write(ByteBuf buffer, String object) throws Exception {
    if (object == null) {
      buffer.writeBoolean(false);
    } else {
      buffer.writeBoolean(true);
      Type.STRING.write(buffer, object);
    } 
  }
}

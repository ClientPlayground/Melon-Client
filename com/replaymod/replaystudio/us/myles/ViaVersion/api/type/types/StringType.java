package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.google.common.base.Preconditions;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import java.nio.charset.StandardCharsets;

public class StringType extends Type<String> {
  private static final int maxJavaCharUtf8Length = (Character.toString('￿')
    .getBytes(StandardCharsets.UTF_8)).length;
  
  public StringType() {
    super(String.class);
  }
  
  public String read(ByteBuf buffer) throws Exception {
    int len = ((Integer)Type.VAR_INT.read(buffer)).intValue();
    Preconditions.checkArgument((len <= 32767 * maxJavaCharUtf8Length), "Cannot receive string longer than Short.MAX_VALUE * " + maxJavaCharUtf8Length + " bytes (got %s bytes)", new Object[] { Integer.valueOf(len) });
    String string = buffer.toString(buffer.readerIndex(), len, StandardCharsets.UTF_8);
    buffer.skipBytes(len);
    Preconditions.checkArgument((string.length() <= 32767), "Cannot receive string longer than Short.MAX_VALUE characters (got %s bytes)", new Object[] { Integer.valueOf(string.length()) });
    return string;
  }
  
  public void write(ByteBuf buffer, String object) throws Exception {
    Preconditions.checkArgument((object.length() <= 32767), "Cannot send string longer than Short.MAX_VALUE (got %s characters)", new Object[] { Integer.valueOf(object.length()) });
    byte[] b = object.getBytes(StandardCharsets.UTF_8);
    Type.VAR_INT.write(buffer, Integer.valueOf(b.length));
    buffer.writeBytes(b);
  }
}

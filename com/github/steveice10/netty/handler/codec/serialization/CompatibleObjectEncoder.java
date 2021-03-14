package com.github.steveice10.netty.handler.codec.serialization;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufOutputStream;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class CompatibleObjectEncoder extends MessageToByteEncoder<Serializable> {
  private final int resetInterval;
  
  private int writtenObjects;
  
  public CompatibleObjectEncoder() {
    this(16);
  }
  
  public CompatibleObjectEncoder(int resetInterval) {
    if (resetInterval < 0)
      throw new IllegalArgumentException("resetInterval: " + resetInterval); 
    this.resetInterval = resetInterval;
  }
  
  protected ObjectOutputStream newObjectOutputStream(OutputStream out) throws Exception {
    return new ObjectOutputStream(out);
  }
  
  protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
    ObjectOutputStream oos = newObjectOutputStream((OutputStream)new ByteBufOutputStream(out));
    try {
      if (this.resetInterval != 0) {
        this.writtenObjects++;
        if (this.writtenObjects % this.resetInterval == 0)
          oos.reset(); 
      } 
      oos.writeObject(msg);
      oos.flush();
    } finally {
      oos.close();
    } 
  }
}

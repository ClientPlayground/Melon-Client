package com.github.steveice10.netty.handler.codec.serialization;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufInputStream;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.LengthFieldBasedFrameDecoder;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class ObjectDecoder extends LengthFieldBasedFrameDecoder {
  private final ClassResolver classResolver;
  
  public ObjectDecoder(ClassResolver classResolver) {
    this(1048576, classResolver);
  }
  
  public ObjectDecoder(int maxObjectSize, ClassResolver classResolver) {
    super(maxObjectSize, 0, 4, 0, 4);
    this.classResolver = classResolver;
  }
  
  protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    ByteBuf frame = (ByteBuf)super.decode(ctx, in);
    if (frame == null)
      return null; 
    ObjectInputStream ois = new CompactObjectInputStream((InputStream)new ByteBufInputStream(frame, true), this.classResolver);
    try {
      return ois.readObject();
    } finally {
      ois.close();
    } 
  }
}

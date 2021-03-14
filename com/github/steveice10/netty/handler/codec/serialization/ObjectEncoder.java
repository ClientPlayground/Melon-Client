package com.github.steveice10.netty.handler.codec.serialization;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufOutputStream;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

@Sharable
public class ObjectEncoder extends MessageToByteEncoder<Serializable> {
  private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
  
  protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
    int startIdx = out.writerIndex();
    ByteBufOutputStream bout = new ByteBufOutputStream(out);
    ObjectOutputStream oout = null;
    try {
      bout.write(LENGTH_PLACEHOLDER);
      oout = new CompactObjectOutputStream((OutputStream)bout);
      oout.writeObject(msg);
      oout.flush();
    } finally {
      if (oout != null) {
        oout.close();
      } else {
        bout.close();
      } 
    } 
    int endIdx = out.writerIndex();
    out.setInt(startIdx, endIdx - startIdx - 4);
  }
}

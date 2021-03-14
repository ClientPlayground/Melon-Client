package com.github.steveice10.netty.handler.codec.base64;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

@Sharable
public class Base64Encoder extends MessageToMessageEncoder<ByteBuf> {
  private final boolean breakLines;
  
  private final Base64Dialect dialect;
  
  public Base64Encoder() {
    this(true);
  }
  
  public Base64Encoder(boolean breakLines) {
    this(breakLines, Base64Dialect.STANDARD);
  }
  
  public Base64Encoder(boolean breakLines, Base64Dialect dialect) {
    if (dialect == null)
      throw new NullPointerException("dialect"); 
    this.breakLines = breakLines;
    this.dialect = dialect;
  }
  
  protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
    out.add(Base64.encode(msg, msg.readerIndex(), msg.readableBytes(), this.breakLines, this.dialect));
  }
}

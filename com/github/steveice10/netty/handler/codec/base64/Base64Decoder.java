package com.github.steveice10.netty.handler.codec.base64;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

@Sharable
public class Base64Decoder extends MessageToMessageDecoder<ByteBuf> {
  private final Base64Dialect dialect;
  
  public Base64Decoder() {
    this(Base64Dialect.STANDARD);
  }
  
  public Base64Decoder(Base64Dialect dialect) {
    if (dialect == null)
      throw new NullPointerException("dialect"); 
    this.dialect = dialect;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
    out.add(Base64.decode(msg, msg.readerIndex(), msg.readableBytes(), this.dialect));
  }
}

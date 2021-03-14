package com.github.steveice10.netty.handler.codec.string;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import java.nio.charset.Charset;
import java.util.List;

@Sharable
public class StringDecoder extends MessageToMessageDecoder<ByteBuf> {
  private final Charset charset;
  
  public StringDecoder() {
    this(Charset.defaultCharset());
  }
  
  public StringDecoder(Charset charset) {
    if (charset == null)
      throw new NullPointerException("charset"); 
    this.charset = charset;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
    out.add(msg.toString(this.charset));
  }
}

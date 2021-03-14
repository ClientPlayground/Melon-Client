package com.github.steveice10.netty.handler.codec.string;

import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

@Sharable
public class StringEncoder extends MessageToMessageEncoder<CharSequence> {
  private final Charset charset;
  
  public StringEncoder() {
    this(Charset.defaultCharset());
  }
  
  public StringEncoder(Charset charset) {
    if (charset == null)
      throw new NullPointerException("charset"); 
    this.charset = charset;
  }
  
  protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {
    if (msg.length() == 0)
      return; 
    out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), this.charset));
  }
}

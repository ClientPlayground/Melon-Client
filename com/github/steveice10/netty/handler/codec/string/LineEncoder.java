package com.github.steveice10.netty.handler.codec.string;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

@Sharable
public class LineEncoder extends MessageToMessageEncoder<CharSequence> {
  private final Charset charset;
  
  private final byte[] lineSeparator;
  
  public LineEncoder() {
    this(LineSeparator.DEFAULT, CharsetUtil.UTF_8);
  }
  
  public LineEncoder(LineSeparator lineSeparator) {
    this(lineSeparator, CharsetUtil.UTF_8);
  }
  
  public LineEncoder(Charset charset) {
    this(LineSeparator.DEFAULT, charset);
  }
  
  public LineEncoder(LineSeparator lineSeparator, Charset charset) {
    this.charset = (Charset)ObjectUtil.checkNotNull(charset, "charset");
    this.lineSeparator = ((LineSeparator)ObjectUtil.checkNotNull(lineSeparator, "lineSeparator")).value().getBytes(charset);
  }
  
  protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {
    ByteBuf buffer = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), this.charset, this.lineSeparator.length);
    buffer.writeBytes(this.lineSeparator);
    out.add(buffer);
  }
}

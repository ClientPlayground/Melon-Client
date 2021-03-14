package com.github.steveice10.netty.handler.codec.smtp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import java.util.Iterator;
import java.util.List;

public final class SmtpRequestEncoder extends MessageToMessageEncoder<Object> {
  private static final int CRLF_SHORT = 3338;
  
  private static final byte SP = 32;
  
  private static final ByteBuf DOT_CRLF_BUFFER = Unpooled.unreleasableBuffer(
      Unpooled.directBuffer(3).writeByte(46).writeByte(13).writeByte(10));
  
  private boolean contentExpected;
  
  public boolean acceptOutboundMessage(Object msg) throws Exception {
    return (msg instanceof SmtpRequest || msg instanceof SmtpContent);
  }
  
  protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
    if (msg instanceof SmtpRequest) {
      SmtpRequest req = (SmtpRequest)msg;
      if (this.contentExpected)
        if (req.command().equals(SmtpCommand.RSET)) {
          this.contentExpected = false;
        } else {
          throw new IllegalStateException("SmtpContent expected");
        }  
      boolean release = true;
      ByteBuf buffer = ctx.alloc().buffer();
      try {
        req.command().encode(buffer);
        writeParameters(req.parameters(), buffer);
        ByteBufUtil.writeShortBE(buffer, 3338);
        out.add(buffer);
        release = false;
        if (req.command().isContentExpected())
          this.contentExpected = true; 
      } finally {
        if (release)
          buffer.release(); 
      } 
    } 
    if (msg instanceof SmtpContent) {
      if (!this.contentExpected)
        throw new IllegalStateException("No SmtpContent expected"); 
      ByteBuf content = ((SmtpContent)msg).content();
      out.add(content.retain());
      if (msg instanceof LastSmtpContent) {
        out.add(DOT_CRLF_BUFFER.retainedDuplicate());
        this.contentExpected = false;
      } 
    } 
  }
  
  private static void writeParameters(List<CharSequence> parameters, ByteBuf out) {
    if (parameters.isEmpty())
      return; 
    out.writeByte(32);
    if (parameters instanceof java.util.RandomAccess) {
      int sizeMinusOne = parameters.size() - 1;
      for (int i = 0; i < sizeMinusOne; i++) {
        ByteBufUtil.writeAscii(out, parameters.get(i));
        out.writeByte(32);
      } 
      ByteBufUtil.writeAscii(out, parameters.get(sizeMinusOne));
    } else {
      Iterator<CharSequence> params = parameters.iterator();
      while (true) {
        ByteBufUtil.writeAscii(out, params.next());
        if (params.hasNext()) {
          out.writeByte(32);
          continue;
        } 
        break;
      } 
    } 
  }
}

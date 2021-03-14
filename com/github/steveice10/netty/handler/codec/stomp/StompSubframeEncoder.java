package com.github.steveice10.netty.handler.codec.stomp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.AsciiHeadersEncoder;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import com.github.steveice10.netty.util.CharsetUtil;
import java.util.List;
import java.util.Map;

public class StompSubframeEncoder extends MessageToMessageEncoder<StompSubframe> {
  protected void encode(ChannelHandlerContext ctx, StompSubframe msg, List<Object> out) throws Exception {
    if (msg instanceof StompFrame) {
      StompFrame frame = (StompFrame)msg;
      ByteBuf frameBuf = encodeFrame(frame, ctx);
      out.add(frameBuf);
      ByteBuf contentBuf = encodeContent(frame, ctx);
      out.add(contentBuf);
    } else if (msg instanceof StompHeadersSubframe) {
      StompHeadersSubframe frame = (StompHeadersSubframe)msg;
      ByteBuf buf = encodeFrame(frame, ctx);
      out.add(buf);
    } else if (msg instanceof StompContentSubframe) {
      StompContentSubframe stompContentSubframe = (StompContentSubframe)msg;
      ByteBuf buf = encodeContent(stompContentSubframe, ctx);
      out.add(buf);
    } 
  }
  
  private static ByteBuf encodeContent(StompContentSubframe content, ChannelHandlerContext ctx) {
    if (content instanceof LastStompContentSubframe) {
      ByteBuf buf = ctx.alloc().buffer(content.content().readableBytes() + 1);
      buf.writeBytes(content.content());
      buf.writeByte(0);
      return buf;
    } 
    return content.content().retain();
  }
  
  private static ByteBuf encodeFrame(StompHeadersSubframe frame, ChannelHandlerContext ctx) {
    ByteBuf buf = ctx.alloc().buffer();
    buf.writeCharSequence(frame.command().toString(), CharsetUtil.US_ASCII);
    buf.writeByte(10);
    AsciiHeadersEncoder headersEncoder = new AsciiHeadersEncoder(buf, AsciiHeadersEncoder.SeparatorType.COLON, AsciiHeadersEncoder.NewlineType.LF);
    for (Map.Entry<CharSequence, CharSequence> entry : (Iterable<Map.Entry<CharSequence, CharSequence>>)frame.headers())
      headersEncoder.encode(entry); 
    buf.writeByte(10);
    return buf;
  }
}

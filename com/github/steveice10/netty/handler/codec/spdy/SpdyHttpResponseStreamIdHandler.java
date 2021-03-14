package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageCodec;
import com.github.steveice10.netty.handler.codec.http.HttpMessage;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SpdyHttpResponseStreamIdHandler extends MessageToMessageCodec<Object, HttpMessage> {
  private static final Integer NO_ID = Integer.valueOf(-1);
  
  private final Queue<Integer> ids = new LinkedList<Integer>();
  
  public boolean acceptInboundMessage(Object msg) throws Exception {
    return (msg instanceof HttpMessage || msg instanceof SpdyRstStreamFrame);
  }
  
  protected void encode(ChannelHandlerContext ctx, HttpMessage msg, List<Object> out) throws Exception {
    Integer id = this.ids.poll();
    if (id != null && id.intValue() != NO_ID.intValue() && !msg.headers().contains((CharSequence)SpdyHttpHeaders.Names.STREAM_ID))
      msg.headers().setInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID, id.intValue()); 
    out.add(ReferenceCountUtil.retain(msg));
  }
  
  protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
    if (msg instanceof HttpMessage) {
      boolean contains = ((HttpMessage)msg).headers().contains((CharSequence)SpdyHttpHeaders.Names.STREAM_ID);
      if (!contains) {
        this.ids.add(NO_ID);
      } else {
        this.ids.add(((HttpMessage)msg).headers().getInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID));
      } 
    } else if (msg instanceof SpdyRstStreamFrame) {
      this.ids.remove(Integer.valueOf(((SpdyRstStreamFrame)msg).streamId()));
    } 
    out.add(ReferenceCountUtil.retain(msg));
  }
}

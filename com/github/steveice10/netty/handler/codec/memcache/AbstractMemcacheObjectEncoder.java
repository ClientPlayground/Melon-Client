package com.github.steveice10.netty.handler.codec.memcache;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.FileRegion;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.List;

public abstract class AbstractMemcacheObjectEncoder<M extends MemcacheMessage> extends MessageToMessageEncoder<Object> {
  private boolean expectingMoreContent;
  
  protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
    if (msg instanceof MemcacheMessage) {
      if (this.expectingMoreContent)
        throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg)); 
      MemcacheMessage memcacheMessage = (MemcacheMessage)msg;
      out.add(encodeMessage(ctx, (M)memcacheMessage));
    } 
    if (msg instanceof MemcacheContent || msg instanceof ByteBuf || msg instanceof FileRegion) {
      int contentLength = contentLength(msg);
      if (contentLength > 0) {
        out.add(encodeAndRetain(msg));
      } else {
        out.add(Unpooled.EMPTY_BUFFER);
      } 
      this.expectingMoreContent = !(msg instanceof LastMemcacheContent);
    } 
  }
  
  public boolean acceptOutboundMessage(Object msg) throws Exception {
    return (msg instanceof MemcacheObject || msg instanceof ByteBuf || msg instanceof FileRegion);
  }
  
  protected abstract ByteBuf encodeMessage(ChannelHandlerContext paramChannelHandlerContext, M paramM);
  
  private static int contentLength(Object msg) {
    if (msg instanceof MemcacheContent)
      return ((MemcacheContent)msg).content().readableBytes(); 
    if (msg instanceof ByteBuf)
      return ((ByteBuf)msg).readableBytes(); 
    if (msg instanceof FileRegion)
      return (int)((FileRegion)msg).count(); 
    throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
  }
  
  private static Object encodeAndRetain(Object msg) {
    if (msg instanceof ByteBuf)
      return ((ByteBuf)msg).retain(); 
    if (msg instanceof MemcacheContent)
      return ((MemcacheContent)msg).content().retain(); 
    if (msg instanceof FileRegion)
      return ((FileRegion)msg).retain(); 
    throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
  }
}

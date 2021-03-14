package com.github.steveice10.netty.handler.codec.sctp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.sctp.SctpMessage;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SctpMessageCompletionHandler extends MessageToMessageDecoder<SctpMessage> {
  private final Map<Integer, ByteBuf> fragments = new HashMap<Integer, ByteBuf>();
  
  protected void decode(ChannelHandlerContext ctx, SctpMessage msg, List<Object> out) throws Exception {
    ByteBuf byteBuf = msg.content();
    int protocolIdentifier = msg.protocolIdentifier();
    int streamIdentifier = msg.streamIdentifier();
    boolean isComplete = msg.isComplete();
    boolean isUnordered = msg.isUnordered();
    ByteBuf frag = this.fragments.remove(Integer.valueOf(streamIdentifier));
    if (frag == null)
      frag = Unpooled.EMPTY_BUFFER; 
    if (isComplete && !frag.isReadable()) {
      out.add(msg);
    } else if (!isComplete && frag.isReadable()) {
      this.fragments.put(Integer.valueOf(streamIdentifier), Unpooled.wrappedBuffer(new ByteBuf[] { frag, byteBuf }));
    } else if (isComplete && frag.isReadable()) {
      SctpMessage assembledMsg = new SctpMessage(protocolIdentifier, streamIdentifier, isUnordered, Unpooled.wrappedBuffer(new ByteBuf[] { frag, byteBuf }));
      out.add(assembledMsg);
    } else {
      this.fragments.put(Integer.valueOf(streamIdentifier), byteBuf);
    } 
    byteBuf.retain();
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    for (ByteBuf buffer : this.fragments.values())
      buffer.release(); 
    this.fragments.clear();
    super.handlerRemoved(ctx);
  }
}

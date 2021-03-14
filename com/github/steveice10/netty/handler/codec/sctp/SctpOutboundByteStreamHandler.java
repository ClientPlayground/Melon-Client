package com.github.steveice10.netty.handler.codec.sctp;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.sctp.SctpMessage;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

public class SctpOutboundByteStreamHandler extends MessageToMessageEncoder<ByteBuf> {
  private final int streamIdentifier;
  
  private final int protocolIdentifier;
  
  private final boolean unordered;
  
  public SctpOutboundByteStreamHandler(int streamIdentifier, int protocolIdentifier) {
    this(streamIdentifier, protocolIdentifier, false);
  }
  
  public SctpOutboundByteStreamHandler(int streamIdentifier, int protocolIdentifier, boolean unordered) {
    this.streamIdentifier = streamIdentifier;
    this.protocolIdentifier = protocolIdentifier;
    this.unordered = unordered;
  }
  
  protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
    out.add(new SctpMessage(this.protocolIdentifier, this.streamIdentifier, this.unordered, msg.retain()));
  }
}

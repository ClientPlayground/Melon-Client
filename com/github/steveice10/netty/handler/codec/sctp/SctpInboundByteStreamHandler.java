package com.github.steveice10.netty.handler.codec.sctp;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.sctp.SctpMessage;
import com.github.steveice10.netty.handler.codec.CodecException;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

public class SctpInboundByteStreamHandler extends MessageToMessageDecoder<SctpMessage> {
  private final int protocolIdentifier;
  
  private final int streamIdentifier;
  
  public SctpInboundByteStreamHandler(int protocolIdentifier, int streamIdentifier) {
    this.protocolIdentifier = protocolIdentifier;
    this.streamIdentifier = streamIdentifier;
  }
  
  public final boolean acceptInboundMessage(Object msg) throws Exception {
    if (super.acceptInboundMessage(msg))
      return acceptInboundMessage((SctpMessage)msg); 
    return false;
  }
  
  protected boolean acceptInboundMessage(SctpMessage msg) {
    return (msg.protocolIdentifier() == this.protocolIdentifier && msg.streamIdentifier() == this.streamIdentifier);
  }
  
  protected void decode(ChannelHandlerContext ctx, SctpMessage msg, List<Object> out) throws Exception {
    if (!msg.isComplete())
      throw new CodecException(String.format("Received SctpMessage is not complete, please add %s in the pipeline before this handler", new Object[] { SctpMessageCompletionHandler.class
              .getSimpleName() })); 
    out.add(msg.content().retain());
  }
}

package com.github.steveice10.netty.handler.codec.sctp;

import com.github.steveice10.netty.channel.sctp.SctpMessage;
import com.github.steveice10.netty.handler.codec.CodecException;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;

public abstract class SctpMessageToMessageDecoder extends MessageToMessageDecoder<SctpMessage> {
  public boolean acceptInboundMessage(Object msg) throws Exception {
    if (msg instanceof SctpMessage) {
      SctpMessage sctpMsg = (SctpMessage)msg;
      if (sctpMsg.isComplete())
        return true; 
      throw new CodecException(String.format("Received SctpMessage is not complete, please add %s in the pipeline before this handler", new Object[] { SctpMessageCompletionHandler.class
              .getSimpleName() }));
    } 
    return false;
  }
}

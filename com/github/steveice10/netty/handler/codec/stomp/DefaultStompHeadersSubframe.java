package com.github.steveice10.netty.handler.codec.stomp;

import com.github.steveice10.netty.handler.codec.DecoderResult;

public class DefaultStompHeadersSubframe implements StompHeadersSubframe {
  protected final StompCommand command;
  
  protected DecoderResult decoderResult = DecoderResult.SUCCESS;
  
  protected final DefaultStompHeaders headers;
  
  public DefaultStompHeadersSubframe(StompCommand command) {
    this(command, null);
  }
  
  DefaultStompHeadersSubframe(StompCommand command, DefaultStompHeaders headers) {
    if (command == null)
      throw new NullPointerException("command"); 
    this.command = command;
    this.headers = (headers == null) ? new DefaultStompHeaders() : headers;
  }
  
  public StompCommand command() {
    return this.command;
  }
  
  public StompHeaders headers() {
    return this.headers;
  }
  
  public DecoderResult decoderResult() {
    return this.decoderResult;
  }
  
  public void setDecoderResult(DecoderResult decoderResult) {
    this.decoderResult = decoderResult;
  }
  
  public String toString() {
    return "StompFrame{command=" + this.command + ", headers=" + this.headers + '}';
  }
}

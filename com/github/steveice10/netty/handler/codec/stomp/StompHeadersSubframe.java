package com.github.steveice10.netty.handler.codec.stomp;

public interface StompHeadersSubframe extends StompSubframe {
  StompCommand command();
  
  StompHeaders headers();
}

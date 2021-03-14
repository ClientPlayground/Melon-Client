package com.github.steveice10.netty.handler.codec.stomp;

public enum StompCommand {
  STOMP, CONNECT, CONNECTED, SEND, SUBSCRIBE, UNSUBSCRIBE, ACK, NACK, BEGIN, DISCONNECT, MESSAGE, RECEIPT, ERROR, UNKNOWN;
}

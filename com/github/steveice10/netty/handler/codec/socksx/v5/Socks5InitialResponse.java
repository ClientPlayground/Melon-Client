package com.github.steveice10.netty.handler.codec.socksx.v5;

public interface Socks5InitialResponse extends Socks5Message {
  Socks5AuthMethod authMethod();
}

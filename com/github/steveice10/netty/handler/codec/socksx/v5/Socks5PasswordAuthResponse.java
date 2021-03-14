package com.github.steveice10.netty.handler.codec.socksx.v5;

public interface Socks5PasswordAuthResponse extends Socks5Message {
  Socks5PasswordAuthStatus status();
}

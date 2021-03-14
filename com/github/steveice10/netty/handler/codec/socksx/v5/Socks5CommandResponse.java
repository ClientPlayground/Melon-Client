package com.github.steveice10.netty.handler.codec.socksx.v5;

public interface Socks5CommandResponse extends Socks5Message {
  Socks5CommandStatus status();
  
  Socks5AddressType bndAddrType();
  
  String bndAddr();
  
  int bndPort();
}

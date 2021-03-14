package com.github.steveice10.netty.handler.codec.socksx.v5;

public interface Socks5CommandRequest extends Socks5Message {
  Socks5CommandType type();
  
  Socks5AddressType dstAddrType();
  
  String dstAddr();
  
  int dstPort();
}

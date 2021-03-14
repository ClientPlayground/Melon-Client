package com.github.steveice10.netty.resolver.dns;

import java.net.InetSocketAddress;

public interface DnsServerAddressStream {
  InetSocketAddress next();
  
  int size();
  
  DnsServerAddressStream duplicate();
}

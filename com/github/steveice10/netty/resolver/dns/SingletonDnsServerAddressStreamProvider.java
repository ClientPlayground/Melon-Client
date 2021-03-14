package com.github.steveice10.netty.resolver.dns;

import java.net.InetSocketAddress;

public final class SingletonDnsServerAddressStreamProvider extends UniSequentialDnsServerAddressStreamProvider {
  public SingletonDnsServerAddressStreamProvider(InetSocketAddress address) {
    super(DnsServerAddresses.singleton(address));
  }
}

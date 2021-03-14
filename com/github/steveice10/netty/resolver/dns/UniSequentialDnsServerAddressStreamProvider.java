package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.util.internal.ObjectUtil;

abstract class UniSequentialDnsServerAddressStreamProvider implements DnsServerAddressStreamProvider {
  private final DnsServerAddresses addresses;
  
  UniSequentialDnsServerAddressStreamProvider(DnsServerAddresses addresses) {
    this.addresses = (DnsServerAddresses)ObjectUtil.checkNotNull(addresses, "addresses");
  }
  
  public final DnsServerAddressStream nameServerAddressStream(String hostname) {
    return this.addresses.stream();
  }
}

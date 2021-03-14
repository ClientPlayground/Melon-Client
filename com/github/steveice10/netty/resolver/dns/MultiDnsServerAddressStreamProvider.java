package com.github.steveice10.netty.resolver.dns;

import java.util.List;

public final class MultiDnsServerAddressStreamProvider implements DnsServerAddressStreamProvider {
  private final DnsServerAddressStreamProvider[] providers;
  
  public MultiDnsServerAddressStreamProvider(List<DnsServerAddressStreamProvider> providers) {
    this.providers = providers.<DnsServerAddressStreamProvider>toArray(new DnsServerAddressStreamProvider[0]);
  }
  
  public MultiDnsServerAddressStreamProvider(DnsServerAddressStreamProvider... providers) {
    this.providers = (DnsServerAddressStreamProvider[])providers.clone();
  }
  
  public DnsServerAddressStream nameServerAddressStream(String hostname) {
    for (DnsServerAddressStreamProvider provider : this.providers) {
      DnsServerAddressStream stream = provider.nameServerAddressStream(hostname);
      if (stream != null)
        return stream; 
    } 
    return null;
  }
}

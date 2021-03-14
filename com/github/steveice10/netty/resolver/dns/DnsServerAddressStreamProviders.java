package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.util.internal.PlatformDependent;

public final class DnsServerAddressStreamProviders {
  private static final DnsServerAddressStreamProvider DEFAULT_DNS_SERVER_ADDRESS_STREAM_PROVIDER = PlatformDependent.isWindows() ? DefaultDnsServerAddressStreamProvider.INSTANCE : 
    UnixResolverDnsServerAddressStreamProvider.parseSilently();
  
  public static DnsServerAddressStreamProvider platformDefault() {
    return DEFAULT_DNS_SERVER_ADDRESS_STREAM_PROVIDER;
  }
}

package com.github.steveice10.netty.resolver;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Locale;
import java.util.Map;

public final class DefaultHostsFileEntriesResolver implements HostsFileEntriesResolver {
  private final Map<String, Inet4Address> inet4Entries;
  
  private final Map<String, Inet6Address> inet6Entries;
  
  public DefaultHostsFileEntriesResolver() {
    this(HostsFileParser.parseSilently());
  }
  
  DefaultHostsFileEntriesResolver(HostsFileEntries entries) {
    this.inet4Entries = entries.inet4Entries();
    this.inet6Entries = entries.inet6Entries();
  }
  
  public InetAddress address(String inetHost, ResolvedAddressTypes resolvedAddressTypes) {
    Inet4Address inet4Address;
    Inet6Address inet6Address;
    String normalized = normalize(inetHost);
    switch (resolvedAddressTypes) {
      case IPV4_ONLY:
        return this.inet4Entries.get(normalized);
      case IPV6_ONLY:
        return this.inet6Entries.get(normalized);
      case IPV4_PREFERRED:
        inet4Address = this.inet4Entries.get(normalized);
        return (inet4Address != null) ? inet4Address : this.inet6Entries.get(normalized);
      case IPV6_PREFERRED:
        inet6Address = this.inet6Entries.get(normalized);
        return (inet6Address != null) ? inet6Address : this.inet4Entries.get(normalized);
    } 
    throw new IllegalArgumentException("Unknown ResolvedAddressTypes " + resolvedAddressTypes);
  }
  
  String normalize(String inetHost) {
    return inetHost.toLowerCase(Locale.ENGLISH);
  }
}

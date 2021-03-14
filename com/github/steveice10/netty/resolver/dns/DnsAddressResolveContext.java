package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.handler.codec.dns.DnsRecord;
import com.github.steveice10.netty.handler.codec.dns.DnsRecordType;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

final class DnsAddressResolveContext extends DnsResolveContext<InetAddress> {
  private final DnsCache resolveCache;
  
  DnsAddressResolveContext(DnsNameResolver parent, String hostname, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs, DnsCache resolveCache) {
    super(parent, hostname, 1, parent.resolveRecordTypes(), additionals, nameServerAddrs);
    this.resolveCache = resolveCache;
  }
  
  DnsResolveContext<InetAddress> newResolverContext(DnsNameResolver parent, String hostname, int dnsClass, DnsRecordType[] expectedTypes, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs) {
    return new DnsAddressResolveContext(parent, hostname, additionals, nameServerAddrs, this.resolveCache);
  }
  
  InetAddress convertRecord(DnsRecord record, String hostname, DnsRecord[] additionals, EventLoop eventLoop) {
    return DnsAddressDecoder.decodeAddress(record, hostname, this.parent.isDecodeIdn());
  }
  
  List<InetAddress> filterResults(List<InetAddress> unfiltered) {
    Class<? extends InetAddress> inetAddressType = this.parent.preferredAddressType().addressType();
    int size = unfiltered.size();
    int numExpected = 0;
    for (int i = 0; i < size; i++) {
      InetAddress address = unfiltered.get(i);
      if (inetAddressType.isInstance(address))
        numExpected++; 
    } 
    if (numExpected == size || numExpected == 0)
      return unfiltered; 
    List<InetAddress> filtered = new ArrayList<InetAddress>(numExpected);
    for (int j = 0; j < size; j++) {
      InetAddress address = unfiltered.get(j);
      if (inetAddressType.isInstance(address))
        filtered.add(address); 
    } 
    return filtered;
  }
  
  void cache(String hostname, DnsRecord[] additionals, DnsRecord result, InetAddress convertedResult) {
    this.resolveCache.cache(hostname, additionals, convertedResult, result.timeToLive(), this.parent.ch.eventLoop());
  }
  
  void cache(String hostname, DnsRecord[] additionals, UnknownHostException cause) {
    this.resolveCache.cache(hostname, additionals, cause, this.parent.ch.eventLoop());
  }
}

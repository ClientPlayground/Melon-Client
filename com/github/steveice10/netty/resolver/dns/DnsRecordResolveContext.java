package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.handler.codec.dns.DnsQuestion;
import com.github.steveice10.netty.handler.codec.dns.DnsRecord;
import com.github.steveice10.netty.handler.codec.dns.DnsRecordType;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import java.net.UnknownHostException;
import java.util.List;

final class DnsRecordResolveContext extends DnsResolveContext<DnsRecord> {
  DnsRecordResolveContext(DnsNameResolver parent, DnsQuestion question, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs) {
    this(parent, question.name(), question.dnsClass(), new DnsRecordType[] { question
          .type() }, additionals, nameServerAddrs);
  }
  
  private DnsRecordResolveContext(DnsNameResolver parent, String hostname, int dnsClass, DnsRecordType[] expectedTypes, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs) {
    super(parent, hostname, dnsClass, expectedTypes, additionals, nameServerAddrs);
  }
  
  DnsResolveContext<DnsRecord> newResolverContext(DnsNameResolver parent, String hostname, int dnsClass, DnsRecordType[] expectedTypes, DnsRecord[] additionals, DnsServerAddressStream nameServerAddrs) {
    return new DnsRecordResolveContext(parent, hostname, dnsClass, expectedTypes, additionals, nameServerAddrs);
  }
  
  DnsRecord convertRecord(DnsRecord record, String hostname, DnsRecord[] additionals, EventLoop eventLoop) {
    return (DnsRecord)ReferenceCountUtil.retain(record);
  }
  
  List<DnsRecord> filterResults(List<DnsRecord> unfiltered) {
    return unfiltered;
  }
  
  void cache(String hostname, DnsRecord[] additionals, DnsRecord result, DnsRecord convertedResult) {}
  
  void cache(String hostname, DnsRecord[] additionals, UnknownHostException cause) {}
}

package com.github.steveice10.netty.handler.codec.dns;

public interface DnsResponse extends DnsMessage {
  boolean isAuthoritativeAnswer();
  
  DnsResponse setAuthoritativeAnswer(boolean paramBoolean);
  
  boolean isTruncated();
  
  DnsResponse setTruncated(boolean paramBoolean);
  
  boolean isRecursionAvailable();
  
  DnsResponse setRecursionAvailable(boolean paramBoolean);
  
  DnsResponseCode code();
  
  DnsResponse setCode(DnsResponseCode paramDnsResponseCode);
  
  DnsResponse setId(int paramInt);
  
  DnsResponse setOpCode(DnsOpCode paramDnsOpCode);
  
  DnsResponse setRecursionDesired(boolean paramBoolean);
  
  DnsResponse setZ(int paramInt);
  
  DnsResponse setRecord(DnsSection paramDnsSection, DnsRecord paramDnsRecord);
  
  DnsResponse addRecord(DnsSection paramDnsSection, DnsRecord paramDnsRecord);
  
  DnsResponse addRecord(DnsSection paramDnsSection, int paramInt, DnsRecord paramDnsRecord);
  
  DnsResponse clear(DnsSection paramDnsSection);
  
  DnsResponse clear();
  
  DnsResponse touch();
  
  DnsResponse touch(Object paramObject);
  
  DnsResponse retain();
  
  DnsResponse retain(int paramInt);
}

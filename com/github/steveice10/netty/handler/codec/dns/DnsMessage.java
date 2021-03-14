package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.util.ReferenceCounted;

public interface DnsMessage extends ReferenceCounted {
  int id();
  
  DnsMessage setId(int paramInt);
  
  DnsOpCode opCode();
  
  DnsMessage setOpCode(DnsOpCode paramDnsOpCode);
  
  boolean isRecursionDesired();
  
  DnsMessage setRecursionDesired(boolean paramBoolean);
  
  int z();
  
  DnsMessage setZ(int paramInt);
  
  int count(DnsSection paramDnsSection);
  
  int count();
  
  <T extends DnsRecord> T recordAt(DnsSection paramDnsSection);
  
  <T extends DnsRecord> T recordAt(DnsSection paramDnsSection, int paramInt);
  
  DnsMessage setRecord(DnsSection paramDnsSection, DnsRecord paramDnsRecord);
  
  <T extends DnsRecord> T setRecord(DnsSection paramDnsSection, int paramInt, DnsRecord paramDnsRecord);
  
  DnsMessage addRecord(DnsSection paramDnsSection, DnsRecord paramDnsRecord);
  
  DnsMessage addRecord(DnsSection paramDnsSection, int paramInt, DnsRecord paramDnsRecord);
  
  <T extends DnsRecord> T removeRecord(DnsSection paramDnsSection, int paramInt);
  
  DnsMessage clear(DnsSection paramDnsSection);
  
  DnsMessage clear();
  
  DnsMessage touch();
  
  DnsMessage touch(Object paramObject);
  
  DnsMessage retain();
  
  DnsMessage retain(int paramInt);
}

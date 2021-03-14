package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.util.ReferenceCounted;

public class DefaultDnsQuery extends AbstractDnsMessage implements DnsQuery {
  public DefaultDnsQuery(int id) {
    super(id);
  }
  
  public DefaultDnsQuery(int id, DnsOpCode opCode) {
    super(id, opCode);
  }
  
  public DnsQuery setId(int id) {
    return (DnsQuery)super.setId(id);
  }
  
  public DnsQuery setOpCode(DnsOpCode opCode) {
    return (DnsQuery)super.setOpCode(opCode);
  }
  
  public DnsQuery setRecursionDesired(boolean recursionDesired) {
    return (DnsQuery)super.setRecursionDesired(recursionDesired);
  }
  
  public DnsQuery setZ(int z) {
    return (DnsQuery)super.setZ(z);
  }
  
  public DnsQuery setRecord(DnsSection section, DnsRecord record) {
    return (DnsQuery)super.setRecord(section, record);
  }
  
  public DnsQuery addRecord(DnsSection section, DnsRecord record) {
    return (DnsQuery)super.addRecord(section, record);
  }
  
  public DnsQuery addRecord(DnsSection section, int index, DnsRecord record) {
    return (DnsQuery)super.addRecord(section, index, record);
  }
  
  public DnsQuery clear(DnsSection section) {
    return (DnsQuery)super.clear(section);
  }
  
  public DnsQuery clear() {
    return (DnsQuery)super.clear();
  }
  
  public DnsQuery touch() {
    return (DnsQuery)super.touch();
  }
  
  public DnsQuery touch(Object hint) {
    return (DnsQuery)super.touch(hint);
  }
  
  public DnsQuery retain() {
    return (DnsQuery)super.retain();
  }
  
  public DnsQuery retain(int increment) {
    return (DnsQuery)super.retain(increment);
  }
  
  public String toString() {
    return DnsMessageUtil.appendQuery(new StringBuilder(128), this).toString();
  }
}

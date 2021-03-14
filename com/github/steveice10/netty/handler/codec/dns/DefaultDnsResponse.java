package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public class DefaultDnsResponse extends AbstractDnsMessage implements DnsResponse {
  private boolean authoritativeAnswer;
  
  private boolean truncated;
  
  private boolean recursionAvailable;
  
  private DnsResponseCode code;
  
  public DefaultDnsResponse(int id) {
    this(id, DnsOpCode.QUERY, DnsResponseCode.NOERROR);
  }
  
  public DefaultDnsResponse(int id, DnsOpCode opCode) {
    this(id, opCode, DnsResponseCode.NOERROR);
  }
  
  public DefaultDnsResponse(int id, DnsOpCode opCode, DnsResponseCode code) {
    super(id, opCode);
    setCode(code);
  }
  
  public boolean isAuthoritativeAnswer() {
    return this.authoritativeAnswer;
  }
  
  public DnsResponse setAuthoritativeAnswer(boolean authoritativeAnswer) {
    this.authoritativeAnswer = authoritativeAnswer;
    return this;
  }
  
  public boolean isTruncated() {
    return this.truncated;
  }
  
  public DnsResponse setTruncated(boolean truncated) {
    this.truncated = truncated;
    return this;
  }
  
  public boolean isRecursionAvailable() {
    return this.recursionAvailable;
  }
  
  public DnsResponse setRecursionAvailable(boolean recursionAvailable) {
    this.recursionAvailable = recursionAvailable;
    return this;
  }
  
  public DnsResponseCode code() {
    return this.code;
  }
  
  public DnsResponse setCode(DnsResponseCode code) {
    this.code = (DnsResponseCode)ObjectUtil.checkNotNull(code, "code");
    return this;
  }
  
  public DnsResponse setId(int id) {
    return (DnsResponse)super.setId(id);
  }
  
  public DnsResponse setOpCode(DnsOpCode opCode) {
    return (DnsResponse)super.setOpCode(opCode);
  }
  
  public DnsResponse setRecursionDesired(boolean recursionDesired) {
    return (DnsResponse)super.setRecursionDesired(recursionDesired);
  }
  
  public DnsResponse setZ(int z) {
    return (DnsResponse)super.setZ(z);
  }
  
  public DnsResponse setRecord(DnsSection section, DnsRecord record) {
    return (DnsResponse)super.setRecord(section, record);
  }
  
  public DnsResponse addRecord(DnsSection section, DnsRecord record) {
    return (DnsResponse)super.addRecord(section, record);
  }
  
  public DnsResponse addRecord(DnsSection section, int index, DnsRecord record) {
    return (DnsResponse)super.addRecord(section, index, record);
  }
  
  public DnsResponse clear(DnsSection section) {
    return (DnsResponse)super.clear(section);
  }
  
  public DnsResponse clear() {
    return (DnsResponse)super.clear();
  }
  
  public DnsResponse touch() {
    return (DnsResponse)super.touch();
  }
  
  public DnsResponse touch(Object hint) {
    return (DnsResponse)super.touch(hint);
  }
  
  public DnsResponse retain() {
    return (DnsResponse)super.retain();
  }
  
  public DnsResponse retain(int increment) {
    return (DnsResponse)super.retain(increment);
  }
  
  public String toString() {
    return DnsMessageUtil.appendResponse(new StringBuilder(128), this).toString();
  }
}

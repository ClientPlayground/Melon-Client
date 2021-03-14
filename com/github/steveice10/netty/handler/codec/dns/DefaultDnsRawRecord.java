package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultDnsRawRecord extends AbstractDnsRecord implements DnsRawRecord {
  private final ByteBuf content;
  
  public DefaultDnsRawRecord(String name, DnsRecordType type, long timeToLive, ByteBuf content) {
    this(name, type, 1, timeToLive, content);
  }
  
  public DefaultDnsRawRecord(String name, DnsRecordType type, int dnsClass, long timeToLive, ByteBuf content) {
    super(name, type, dnsClass, timeToLive);
    this.content = (ByteBuf)ObjectUtil.checkNotNull(content, "content");
  }
  
  public ByteBuf content() {
    return this.content;
  }
  
  public DnsRawRecord copy() {
    return replace(content().copy());
  }
  
  public DnsRawRecord duplicate() {
    return replace(content().duplicate());
  }
  
  public DnsRawRecord retainedDuplicate() {
    return replace(content().retainedDuplicate());
  }
  
  public DnsRawRecord replace(ByteBuf content) {
    return new DefaultDnsRawRecord(name(), type(), dnsClass(), timeToLive(), content);
  }
  
  public int refCnt() {
    return content().refCnt();
  }
  
  public DnsRawRecord retain() {
    content().retain();
    return this;
  }
  
  public DnsRawRecord retain(int increment) {
    content().retain(increment);
    return this;
  }
  
  public boolean release() {
    return content().release();
  }
  
  public boolean release(int decrement) {
    return content().release(decrement);
  }
  
  public DnsRawRecord touch() {
    content().touch();
    return this;
  }
  
  public DnsRawRecord touch(Object hint) {
    content().touch(hint);
    return this;
  }
  
  public String toString() {
    StringBuilder buf = (new StringBuilder(64)).append(StringUtil.simpleClassName(this)).append('(');
    DnsRecordType type = type();
    if (type != DnsRecordType.OPT) {
      buf.append(name().isEmpty() ? "<root>" : name())
        .append(' ')
        .append(timeToLive())
        .append(' ');
      DnsMessageUtil.appendRecordClass(buf, dnsClass())
        .append(' ')
        .append(type.name());
    } else {
      buf.append("OPT flags:")
        .append(timeToLive())
        .append(" udp:")
        .append(dnsClass());
    } 
    buf.append(' ')
      .append(content().readableBytes())
      .append("B)");
    return buf.toString();
  }
}

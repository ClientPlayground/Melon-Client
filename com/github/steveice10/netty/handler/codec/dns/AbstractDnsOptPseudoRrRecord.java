package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.util.internal.StringUtil;

public abstract class AbstractDnsOptPseudoRrRecord extends AbstractDnsRecord implements DnsOptPseudoRecord {
  protected AbstractDnsOptPseudoRrRecord(int maxPayloadSize, int extendedRcode, int version) {
    super("", DnsRecordType.OPT, maxPayloadSize, packIntoLong(extendedRcode, version));
  }
  
  protected AbstractDnsOptPseudoRrRecord(int maxPayloadSize) {
    super("", DnsRecordType.OPT, maxPayloadSize, 0L);
  }
  
  private static long packIntoLong(int val, int val2) {
    return ((val & 0xFF) << 24 | (val2 & 0xFF) << 16 | 0x0 | 0x0) & 0xFFFFFFFFL;
  }
  
  public int extendedRcode() {
    return (short)((int)timeToLive() >> 24 & 0xFF);
  }
  
  public int version() {
    return (short)((int)timeToLive() >> 16 & 0xFF);
  }
  
  public int flags() {
    return (short)((short)(int)timeToLive() & 0xFF);
  }
  
  public String toString() {
    return toStringBuilder().toString();
  }
  
  final StringBuilder toStringBuilder() {
    return (new StringBuilder(64))
      .append(StringUtil.simpleClassName(this))
      .append('(')
      .append("OPT flags:")
      .append(flags())
      .append(" version:")
      .append(version())
      .append(" extendedRecode:")
      .append(extendedRcode())
      .append(" udp:")
      .append(dnsClass())
      .append(')');
  }
}

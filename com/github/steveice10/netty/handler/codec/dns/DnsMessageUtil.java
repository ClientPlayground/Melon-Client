package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.channel.AddressedEnvelope;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.net.SocketAddress;

final class DnsMessageUtil {
  static StringBuilder appendQuery(StringBuilder buf, DnsQuery query) {
    appendQueryHeader(buf, query);
    appendAllRecords(buf, query);
    return buf;
  }
  
  static StringBuilder appendResponse(StringBuilder buf, DnsResponse response) {
    appendResponseHeader(buf, response);
    appendAllRecords(buf, response);
    return buf;
  }
  
  static StringBuilder appendRecordClass(StringBuilder buf, int dnsClass) {
    String name;
    switch (dnsClass &= 0xFFFF) {
      case 1:
        name = "IN";
        break;
      case 2:
        name = "CSNET";
        break;
      case 3:
        name = "CHAOS";
        break;
      case 4:
        name = "HESIOD";
        break;
      case 254:
        name = "NONE";
        break;
      case 255:
        name = "ANY";
        break;
      default:
        name = null;
        break;
    } 
    if (name != null) {
      buf.append(name);
    } else {
      buf.append("UNKNOWN(").append(dnsClass).append(')');
    } 
    return buf;
  }
  
  private static void appendQueryHeader(StringBuilder buf, DnsQuery msg) {
    buf.append(StringUtil.simpleClassName(msg))
      .append('(');
    appendAddresses(buf, msg)
      .append(msg.id())
      .append(", ")
      .append(msg.opCode());
    if (msg.isRecursionDesired())
      buf.append(", RD"); 
    if (msg.z() != 0)
      buf.append(", Z: ")
        .append(msg.z()); 
    buf.append(')');
  }
  
  private static void appendResponseHeader(StringBuilder buf, DnsResponse msg) {
    buf.append(StringUtil.simpleClassName(msg))
      .append('(');
    appendAddresses(buf, msg)
      .append(msg.id())
      .append(", ")
      .append(msg.opCode())
      .append(", ")
      .append(msg.code())
      .append(',');
    boolean hasComma = true;
    if (msg.isRecursionDesired()) {
      hasComma = false;
      buf.append(" RD");
    } 
    if (msg.isAuthoritativeAnswer()) {
      hasComma = false;
      buf.append(" AA");
    } 
    if (msg.isTruncated()) {
      hasComma = false;
      buf.append(" TC");
    } 
    if (msg.isRecursionAvailable()) {
      hasComma = false;
      buf.append(" RA");
    } 
    if (msg.z() != 0) {
      if (!hasComma)
        buf.append(','); 
      buf.append(" Z: ")
        .append(msg.z());
    } 
    if (hasComma) {
      buf.setCharAt(buf.length() - 1, ')');
    } else {
      buf.append(')');
    } 
  }
  
  private static StringBuilder appendAddresses(StringBuilder buf, DnsMessage msg) {
    if (!(msg instanceof AddressedEnvelope))
      return buf; 
    AddressedEnvelope<?, SocketAddress> envelope = (AddressedEnvelope<?, SocketAddress>)msg;
    SocketAddress addr = envelope.sender();
    if (addr != null)
      buf.append("from: ")
        .append(addr)
        .append(", "); 
    addr = envelope.recipient();
    if (addr != null)
      buf.append("to: ")
        .append(addr)
        .append(", "); 
    return buf;
  }
  
  private static void appendAllRecords(StringBuilder buf, DnsMessage msg) {
    appendRecords(buf, msg, DnsSection.QUESTION);
    appendRecords(buf, msg, DnsSection.ANSWER);
    appendRecords(buf, msg, DnsSection.AUTHORITY);
    appendRecords(buf, msg, DnsSection.ADDITIONAL);
  }
  
  private static void appendRecords(StringBuilder buf, DnsMessage message, DnsSection section) {
    int count = message.count(section);
    for (int i = 0; i < count; i++)
      buf.append(StringUtil.NEWLINE)
        .append('\t')
        .append(message.recordAt(section, i)); 
  }
}

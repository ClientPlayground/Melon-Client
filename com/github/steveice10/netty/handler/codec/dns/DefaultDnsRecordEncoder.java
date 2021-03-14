package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.channel.socket.InternetProtocolFamily;
import com.github.steveice10.netty.handler.codec.UnsupportedMessageTypeException;
import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultDnsRecordEncoder implements DnsRecordEncoder {
  private static final int PREFIX_MASK = 7;
  
  public final void encodeQuestion(DnsQuestion question, ByteBuf out) throws Exception {
    encodeName(question.name(), out);
    out.writeShort(question.type().intValue());
    out.writeShort(question.dnsClass());
  }
  
  public void encodeRecord(DnsRecord record, ByteBuf out) throws Exception {
    if (record instanceof DnsQuestion) {
      encodeQuestion((DnsQuestion)record, out);
    } else if (record instanceof DnsPtrRecord) {
      encodePtrRecord((DnsPtrRecord)record, out);
    } else if (record instanceof DnsOptEcsRecord) {
      encodeOptEcsRecord((DnsOptEcsRecord)record, out);
    } else if (record instanceof DnsOptPseudoRecord) {
      encodeOptPseudoRecord((DnsOptPseudoRecord)record, out);
    } else if (record instanceof DnsRawRecord) {
      encodeRawRecord((DnsRawRecord)record, out);
    } else {
      throw new UnsupportedMessageTypeException(StringUtil.simpleClassName(record));
    } 
  }
  
  private void encodeRecord0(DnsRecord record, ByteBuf out) throws Exception {
    encodeName(record.name(), out);
    out.writeShort(record.type().intValue());
    out.writeShort(record.dnsClass());
    out.writeInt((int)record.timeToLive());
  }
  
  private void encodePtrRecord(DnsPtrRecord record, ByteBuf out) throws Exception {
    encodeRecord0(record, out);
    encodeName(record.hostname(), out);
  }
  
  private void encodeOptPseudoRecord(DnsOptPseudoRecord record, ByteBuf out) throws Exception {
    encodeRecord0(record, out);
    out.writeShort(0);
  }
  
  private void encodeOptEcsRecord(DnsOptEcsRecord record, ByteBuf out) throws Exception {
    encodeRecord0(record, out);
    int sourcePrefixLength = record.sourcePrefixLength();
    int scopePrefixLength = record.scopePrefixLength();
    int lowOrderBitsToPreserve = sourcePrefixLength & 0x7;
    byte[] bytes = record.address();
    int addressBits = bytes.length << 3;
    if (addressBits < sourcePrefixLength || sourcePrefixLength < 0)
      throw new IllegalArgumentException(sourcePrefixLength + ": " + sourcePrefixLength + " (expected: 0 >= " + addressBits + ')'); 
    short addressNumber = (short)((bytes.length == 4) ? InternetProtocolFamily.IPv4.addressNumber() : InternetProtocolFamily.IPv6.addressNumber());
    int payloadLength = calculateEcsAddressLength(sourcePrefixLength, lowOrderBitsToPreserve);
    int fullPayloadLength = 8 + payloadLength;
    out.writeShort(fullPayloadLength);
    out.writeShort(8);
    out.writeShort(fullPayloadLength - 4);
    out.writeShort(addressNumber);
    out.writeByte(sourcePrefixLength);
    out.writeByte(scopePrefixLength);
    if (lowOrderBitsToPreserve > 0) {
      int bytesLength = payloadLength - 1;
      out.writeBytes(bytes, 0, bytesLength);
      out.writeByte(padWithZeros(bytes[bytesLength], lowOrderBitsToPreserve));
    } else {
      out.writeBytes(bytes, 0, payloadLength);
    } 
  }
  
  static int calculateEcsAddressLength(int sourcePrefixLength, int lowOrderBitsToPreserve) {
    return (sourcePrefixLength >>> 3) + ((lowOrderBitsToPreserve != 0) ? 1 : 0);
  }
  
  private void encodeRawRecord(DnsRawRecord record, ByteBuf out) throws Exception {
    encodeRecord0(record, out);
    ByteBuf content = record.content();
    int contentLen = content.readableBytes();
    out.writeShort(contentLen);
    out.writeBytes(content, content.readerIndex(), contentLen);
  }
  
  protected void encodeName(String name, ByteBuf buf) throws Exception {
    if (".".equals(name)) {
      buf.writeByte(0);
      return;
    } 
    String[] labels = name.split("\\.");
    for (String label : labels) {
      int labelLen = label.length();
      if (labelLen == 0)
        break; 
      buf.writeByte(labelLen);
      ByteBufUtil.writeAscii(buf, label);
    } 
    buf.writeByte(0);
  }
  
  private static byte padWithZeros(byte b, int lowOrderBitsToPreserve) {
    switch (lowOrderBitsToPreserve) {
      case 0:
        return 0;
      case 1:
        return (byte)(0x80 & b);
      case 2:
        return (byte)(0xC0 & b);
      case 3:
        return (byte)(0xE0 & b);
      case 4:
        return (byte)(0xF0 & b);
      case 5:
        return (byte)(0xF8 & b);
      case 6:
        return (byte)(0xFC & b);
      case 7:
        return (byte)(0xFE & b);
      case 8:
        return b;
    } 
    throw new IllegalArgumentException("lowOrderBitsToPreserve: " + lowOrderBitsToPreserve);
  }
}

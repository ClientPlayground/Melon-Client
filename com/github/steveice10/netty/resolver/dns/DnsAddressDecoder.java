package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.handler.codec.dns.DnsRecord;
import java.net.IDN;
import java.net.InetAddress;
import java.net.UnknownHostException;

final class DnsAddressDecoder {
  private static final int INADDRSZ4 = 4;
  
  private static final int INADDRSZ6 = 16;
  
  static InetAddress decodeAddress(DnsRecord record, String name, boolean decodeIdn) {
    if (!(record instanceof com.github.steveice10.netty.handler.codec.dns.DnsRawRecord))
      return null; 
    ByteBuf content = ((ByteBufHolder)record).content();
    int contentLen = content.readableBytes();
    if (contentLen != 4 && contentLen != 16)
      return null; 
    byte[] addrBytes = new byte[contentLen];
    content.getBytes(content.readerIndex(), addrBytes);
    try {
      return InetAddress.getByAddress(decodeIdn ? IDN.toUnicode(name) : name, addrBytes);
    } catch (UnknownHostException e) {
      throw new Error(e);
    } 
  }
}

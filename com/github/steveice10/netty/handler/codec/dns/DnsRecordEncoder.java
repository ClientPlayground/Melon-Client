package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface DnsRecordEncoder {
  public static final DnsRecordEncoder DEFAULT = new DefaultDnsRecordEncoder();
  
  void encodeQuestion(DnsQuestion paramDnsQuestion, ByteBuf paramByteBuf) throws Exception;
  
  void encodeRecord(DnsRecord paramDnsRecord, ByteBuf paramByteBuf) throws Exception;
}

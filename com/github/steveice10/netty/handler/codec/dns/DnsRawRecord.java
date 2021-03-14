package com.github.steveice10.netty.handler.codec.dns;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;

public interface DnsRawRecord extends DnsRecord, ByteBufHolder {
  DnsRawRecord copy();
  
  DnsRawRecord duplicate();
  
  DnsRawRecord retainedDuplicate();
  
  DnsRawRecord replace(ByteBuf paramByteBuf);
  
  DnsRawRecord retain();
  
  DnsRawRecord retain(int paramInt);
  
  DnsRawRecord touch();
  
  DnsRawRecord touch(Object paramObject);
}

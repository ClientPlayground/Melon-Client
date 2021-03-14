package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.handler.codec.dns.DnsRecord;
import java.net.InetAddress;
import java.util.List;

public interface DnsCache {
  void clear();
  
  boolean clear(String paramString);
  
  List<? extends DnsCacheEntry> get(String paramString, DnsRecord[] paramArrayOfDnsRecord);
  
  DnsCacheEntry cache(String paramString, DnsRecord[] paramArrayOfDnsRecord, InetAddress paramInetAddress, long paramLong, EventLoop paramEventLoop);
  
  DnsCacheEntry cache(String paramString, DnsRecord[] paramArrayOfDnsRecord, Throwable paramThrowable, EventLoop paramEventLoop);
}

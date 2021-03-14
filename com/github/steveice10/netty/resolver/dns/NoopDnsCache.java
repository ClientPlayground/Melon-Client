package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.handler.codec.dns.DnsRecord;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

public final class NoopDnsCache implements DnsCache {
  public static final NoopDnsCache INSTANCE = new NoopDnsCache();
  
  public void clear() {}
  
  public boolean clear(String hostname) {
    return false;
  }
  
  public List<? extends DnsCacheEntry> get(String hostname, DnsRecord[] additionals) {
    return Collections.emptyList();
  }
  
  public DnsCacheEntry cache(String hostname, DnsRecord[] additional, InetAddress address, long originalTtl, EventLoop loop) {
    return new NoopDnsCacheEntry(address);
  }
  
  public DnsCacheEntry cache(String hostname, DnsRecord[] additional, Throwable cause, EventLoop loop) {
    return null;
  }
  
  public String toString() {
    return NoopDnsCache.class.getSimpleName();
  }
  
  private static final class NoopDnsCacheEntry implements DnsCacheEntry {
    private final InetAddress address;
    
    NoopDnsCacheEntry(InetAddress address) {
      this.address = address;
    }
    
    public InetAddress address() {
      return this.address;
    }
    
    public Throwable cause() {
      return null;
    }
    
    public String toString() {
      return this.address.toString();
    }
  }
}

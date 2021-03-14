package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.handler.codec.dns.DnsQuestion;

public final class NoopDnsQueryLifecycleObserverFactory implements DnsQueryLifecycleObserverFactory {
  public static final NoopDnsQueryLifecycleObserverFactory INSTANCE = new NoopDnsQueryLifecycleObserverFactory();
  
  public DnsQueryLifecycleObserver newDnsQueryLifecycleObserver(DnsQuestion question) {
    return NoopDnsQueryLifecycleObserver.INSTANCE;
  }
}

package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.handler.codec.dns.DnsQuestion;

public interface DnsQueryLifecycleObserverFactory {
  DnsQueryLifecycleObserver newDnsQueryLifecycleObserver(DnsQuestion paramDnsQuestion);
}

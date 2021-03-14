package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.handler.codec.dns.DnsQuestion;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public final class BiDnsQueryLifecycleObserverFactory implements DnsQueryLifecycleObserverFactory {
  private final DnsQueryLifecycleObserverFactory a;
  
  private final DnsQueryLifecycleObserverFactory b;
  
  public BiDnsQueryLifecycleObserverFactory(DnsQueryLifecycleObserverFactory a, DnsQueryLifecycleObserverFactory b) {
    this.a = (DnsQueryLifecycleObserverFactory)ObjectUtil.checkNotNull(a, "a");
    this.b = (DnsQueryLifecycleObserverFactory)ObjectUtil.checkNotNull(b, "b");
  }
  
  public DnsQueryLifecycleObserver newDnsQueryLifecycleObserver(DnsQuestion question) {
    return new BiDnsQueryLifecycleObserver(this.a.newDnsQueryLifecycleObserver(question), this.b
        .newDnsQueryLifecycleObserver(question));
  }
}

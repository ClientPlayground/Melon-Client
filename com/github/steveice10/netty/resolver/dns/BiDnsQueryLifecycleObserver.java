package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.handler.codec.dns.DnsQuestion;
import com.github.steveice10.netty.handler.codec.dns.DnsResponseCode;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.net.InetSocketAddress;
import java.util.List;

public final class BiDnsQueryLifecycleObserver implements DnsQueryLifecycleObserver {
  private final DnsQueryLifecycleObserver a;
  
  private final DnsQueryLifecycleObserver b;
  
  public BiDnsQueryLifecycleObserver(DnsQueryLifecycleObserver a, DnsQueryLifecycleObserver b) {
    this.a = (DnsQueryLifecycleObserver)ObjectUtil.checkNotNull(a, "a");
    this.b = (DnsQueryLifecycleObserver)ObjectUtil.checkNotNull(b, "b");
  }
  
  public void queryWritten(InetSocketAddress dnsServerAddress, ChannelFuture future) {
    try {
      this.a.queryWritten(dnsServerAddress, future);
    } finally {
      this.b.queryWritten(dnsServerAddress, future);
    } 
  }
  
  public void queryCancelled(int queriesRemaining) {
    try {
      this.a.queryCancelled(queriesRemaining);
    } finally {
      this.b.queryCancelled(queriesRemaining);
    } 
  }
  
  public DnsQueryLifecycleObserver queryRedirected(List<InetSocketAddress> nameServers) {
    try {
      this.a.queryRedirected(nameServers);
    } finally {
      this.b.queryRedirected(nameServers);
    } 
    return this;
  }
  
  public DnsQueryLifecycleObserver queryCNAMEd(DnsQuestion cnameQuestion) {
    try {
      this.a.queryCNAMEd(cnameQuestion);
    } finally {
      this.b.queryCNAMEd(cnameQuestion);
    } 
    return this;
  }
  
  public DnsQueryLifecycleObserver queryNoAnswer(DnsResponseCode code) {
    try {
      this.a.queryNoAnswer(code);
    } finally {
      this.b.queryNoAnswer(code);
    } 
    return this;
  }
  
  public void queryFailed(Throwable cause) {
    try {
      this.a.queryFailed(cause);
    } finally {
      this.b.queryFailed(cause);
    } 
  }
  
  public void querySucceed() {
    try {
      this.a.querySucceed();
    } finally {
      this.b.querySucceed();
    } 
  }
}

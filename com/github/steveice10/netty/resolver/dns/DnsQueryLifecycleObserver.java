package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.handler.codec.dns.DnsQuestion;
import com.github.steveice10.netty.handler.codec.dns.DnsResponseCode;
import java.net.InetSocketAddress;
import java.util.List;

public interface DnsQueryLifecycleObserver {
  void queryWritten(InetSocketAddress paramInetSocketAddress, ChannelFuture paramChannelFuture);
  
  void queryCancelled(int paramInt);
  
  DnsQueryLifecycleObserver queryRedirected(List<InetSocketAddress> paramList);
  
  DnsQueryLifecycleObserver queryCNAMEd(DnsQuestion paramDnsQuestion);
  
  DnsQueryLifecycleObserver queryNoAnswer(DnsResponseCode paramDnsResponseCode);
  
  void queryFailed(Throwable paramThrowable);
  
  void querySucceed();
}

package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.channel.ChannelFactory;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.socket.DatagramChannel;
import com.github.steveice10.netty.resolver.AddressResolver;
import com.github.steveice10.netty.resolver.NameResolver;
import com.github.steveice10.netty.resolver.RoundRobinInetAddressResolver;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class RoundRobinDnsAddressResolverGroup extends DnsAddressResolverGroup {
  public RoundRobinDnsAddressResolverGroup(DnsNameResolverBuilder dnsResolverBuilder) {
    super(dnsResolverBuilder);
  }
  
  public RoundRobinDnsAddressResolverGroup(Class<? extends DatagramChannel> channelType, DnsServerAddressStreamProvider nameServerProvider) {
    super(channelType, nameServerProvider);
  }
  
  public RoundRobinDnsAddressResolverGroup(ChannelFactory<? extends DatagramChannel> channelFactory, DnsServerAddressStreamProvider nameServerProvider) {
    super(channelFactory, nameServerProvider);
  }
  
  protected final AddressResolver<InetSocketAddress> newAddressResolver(EventLoop eventLoop, NameResolver<InetAddress> resolver) throws Exception {
    return (new RoundRobinInetAddressResolver((EventExecutor)eventLoop, resolver)).asAddressResolver();
  }
}

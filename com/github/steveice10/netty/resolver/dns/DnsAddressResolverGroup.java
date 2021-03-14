package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.channel.ChannelFactory;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.socket.DatagramChannel;
import com.github.steveice10.netty.resolver.AddressResolver;
import com.github.steveice10.netty.resolver.AddressResolverGroup;
import com.github.steveice10.netty.resolver.InetSocketAddressResolver;
import com.github.steveice10.netty.resolver.NameResolver;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class DnsAddressResolverGroup extends AddressResolverGroup<InetSocketAddress> {
  private final DnsNameResolverBuilder dnsResolverBuilder;
  
  private final ConcurrentMap<String, Promise<InetAddress>> resolvesInProgress = PlatformDependent.newConcurrentHashMap();
  
  private final ConcurrentMap<String, Promise<List<InetAddress>>> resolveAllsInProgress = PlatformDependent.newConcurrentHashMap();
  
  public DnsAddressResolverGroup(DnsNameResolverBuilder dnsResolverBuilder) {
    this.dnsResolverBuilder = dnsResolverBuilder.copy();
  }
  
  public DnsAddressResolverGroup(Class<? extends DatagramChannel> channelType, DnsServerAddressStreamProvider nameServerProvider) {
    this(new DnsNameResolverBuilder());
    this.dnsResolverBuilder.channelType(channelType).nameServerProvider(nameServerProvider);
  }
  
  public DnsAddressResolverGroup(ChannelFactory<? extends DatagramChannel> channelFactory, DnsServerAddressStreamProvider nameServerProvider) {
    this(new DnsNameResolverBuilder());
    this.dnsResolverBuilder.channelFactory(channelFactory).nameServerProvider(nameServerProvider);
  }
  
  protected final AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) throws Exception {
    if (!(executor instanceof EventLoop))
      throw new IllegalStateException("unsupported executor type: " + 
          StringUtil.simpleClassName(executor) + " (expected: " + 
          StringUtil.simpleClassName(EventLoop.class)); 
    return newResolver((EventLoop)executor, this.dnsResolverBuilder
        .channelFactory(), this.dnsResolverBuilder
        .nameServerProvider());
  }
  
  @Deprecated
  protected AddressResolver<InetSocketAddress> newResolver(EventLoop eventLoop, ChannelFactory<? extends DatagramChannel> channelFactory, DnsServerAddressStreamProvider nameServerProvider) throws Exception {
    NameResolver<InetAddress> resolver = new InflightNameResolver<InetAddress>((EventExecutor)eventLoop, newNameResolver(eventLoop, channelFactory, nameServerProvider), this.resolvesInProgress, this.resolveAllsInProgress);
    return newAddressResolver(eventLoop, resolver);
  }
  
  protected NameResolver<InetAddress> newNameResolver(EventLoop eventLoop, ChannelFactory<? extends DatagramChannel> channelFactory, DnsServerAddressStreamProvider nameServerProvider) throws Exception {
    return (NameResolver<InetAddress>)this.dnsResolverBuilder.eventLoop(eventLoop)
      .channelFactory(channelFactory)
      .nameServerProvider(nameServerProvider)
      .build();
  }
  
  protected AddressResolver<InetSocketAddress> newAddressResolver(EventLoop eventLoop, NameResolver<InetAddress> resolver) throws Exception {
    return (AddressResolver<InetSocketAddress>)new InetSocketAddressResolver((EventExecutor)eventLoop, resolver);
  }
}

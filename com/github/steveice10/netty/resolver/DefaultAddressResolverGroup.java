package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import java.net.InetSocketAddress;

public final class DefaultAddressResolverGroup extends AddressResolverGroup<InetSocketAddress> {
  public static final DefaultAddressResolverGroup INSTANCE = new DefaultAddressResolverGroup();
  
  protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) throws Exception {
    return (new DefaultNameResolver(executor)).asAddressResolver();
  }
}

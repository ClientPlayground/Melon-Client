package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import java.net.SocketAddress;

public final class NoopAddressResolverGroup extends AddressResolverGroup<SocketAddress> {
  public static final NoopAddressResolverGroup INSTANCE = new NoopAddressResolverGroup();
  
  protected AddressResolver<SocketAddress> newResolver(EventExecutor executor) throws Exception {
    return new NoopAddressResolver(executor);
  }
}

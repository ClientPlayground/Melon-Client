package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public abstract class InetNameResolver extends SimpleNameResolver<InetAddress> {
  private volatile AddressResolver<InetSocketAddress> addressResolver;
  
  protected InetNameResolver(EventExecutor executor) {
    super(executor);
  }
  
  public AddressResolver<InetSocketAddress> asAddressResolver() {
    AddressResolver<InetSocketAddress> result = this.addressResolver;
    if (result == null)
      synchronized (this) {
        result = this.addressResolver;
        if (result == null)
          this.addressResolver = result = new InetSocketAddressResolver(executor(), this); 
      }  
    return result;
  }
}

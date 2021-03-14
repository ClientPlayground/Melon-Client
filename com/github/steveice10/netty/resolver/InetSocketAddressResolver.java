package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.FutureListener;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class InetSocketAddressResolver extends AbstractAddressResolver<InetSocketAddress> {
  final NameResolver<InetAddress> nameResolver;
  
  public InetSocketAddressResolver(EventExecutor executor, NameResolver<InetAddress> nameResolver) {
    super(executor, InetSocketAddress.class);
    this.nameResolver = nameResolver;
  }
  
  protected boolean doIsResolved(InetSocketAddress address) {
    return !address.isUnresolved();
  }
  
  protected void doResolve(final InetSocketAddress unresolvedAddress, final Promise<InetSocketAddress> promise) throws Exception {
    this.nameResolver.resolve(unresolvedAddress.getHostName())
      .addListener((GenericFutureListener)new FutureListener<InetAddress>() {
          public void operationComplete(Future<InetAddress> future) throws Exception {
            if (future.isSuccess()) {
              promise.setSuccess(new InetSocketAddress((InetAddress)future.getNow(), unresolvedAddress.getPort()));
            } else {
              promise.setFailure(future.cause());
            } 
          }
        });
  }
  
  protected void doResolveAll(final InetSocketAddress unresolvedAddress, final Promise<List<InetSocketAddress>> promise) throws Exception {
    this.nameResolver.resolveAll(unresolvedAddress.getHostName())
      .addListener((GenericFutureListener)new FutureListener<List<InetAddress>>() {
          public void operationComplete(Future<List<InetAddress>> future) throws Exception {
            if (future.isSuccess()) {
              List<InetAddress> inetAddresses = (List<InetAddress>)future.getNow();
              List<InetSocketAddress> socketAddresses = new ArrayList<InetSocketAddress>(inetAddresses.size());
              for (InetAddress inetAddress : inetAddresses)
                socketAddresses.add(new InetSocketAddress(inetAddress, unresolvedAddress.getPort())); 
              promise.setSuccess(socketAddresses);
            } else {
              promise.setFailure(future.cause());
            } 
          }
        });
  }
  
  public void close() {
    this.nameResolver.close();
  }
}

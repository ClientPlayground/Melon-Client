package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.FutureListener;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoundRobinInetAddressResolver extends InetNameResolver {
  private final NameResolver<InetAddress> nameResolver;
  
  public RoundRobinInetAddressResolver(EventExecutor executor, NameResolver<InetAddress> nameResolver) {
    super(executor);
    this.nameResolver = nameResolver;
  }
  
  protected void doResolve(final String inetHost, final Promise<InetAddress> promise) throws Exception {
    this.nameResolver.resolveAll(inetHost).addListener((GenericFutureListener)new FutureListener<List<InetAddress>>() {
          public void operationComplete(Future<List<InetAddress>> future) throws Exception {
            if (future.isSuccess()) {
              List<InetAddress> inetAddresses = (List<InetAddress>)future.getNow();
              int numAddresses = inetAddresses.size();
              if (numAddresses > 0) {
                promise.setSuccess(inetAddresses.get(RoundRobinInetAddressResolver.randomIndex(numAddresses)));
              } else {
                promise.setFailure(new UnknownHostException(inetHost));
              } 
            } else {
              promise.setFailure(future.cause());
            } 
          }
        });
  }
  
  protected void doResolveAll(String inetHost, final Promise<List<InetAddress>> promise) throws Exception {
    this.nameResolver.resolveAll(inetHost).addListener((GenericFutureListener)new FutureListener<List<InetAddress>>() {
          public void operationComplete(Future<List<InetAddress>> future) throws Exception {
            if (future.isSuccess()) {
              List<InetAddress> inetAddresses = (List<InetAddress>)future.getNow();
              if (!inetAddresses.isEmpty()) {
                List<InetAddress> result = new ArrayList<InetAddress>(inetAddresses);
                Collections.rotate(result, RoundRobinInetAddressResolver.randomIndex(inetAddresses.size()));
                promise.setSuccess(result);
              } else {
                promise.setSuccess(inetAddresses);
              } 
            } else {
              promise.setFailure(future.cause());
            } 
          }
        });
  }
  
  private static int randomIndex(int numAddresses) {
    return (numAddresses == 1) ? 0 : PlatformDependent.threadLocalRandom().nextInt(numAddresses);
  }
}

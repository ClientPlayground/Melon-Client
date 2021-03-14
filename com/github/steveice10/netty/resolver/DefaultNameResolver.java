package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.SocketUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class DefaultNameResolver extends InetNameResolver {
  public DefaultNameResolver(EventExecutor executor) {
    super(executor);
  }
  
  protected void doResolve(String inetHost, Promise<InetAddress> promise) throws Exception {
    try {
      promise.setSuccess(SocketUtils.addressByName(inetHost));
    } catch (UnknownHostException e) {
      promise.setFailure(e);
    } 
  }
  
  protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) throws Exception {
    try {
      promise.setSuccess(Arrays.asList(SocketUtils.allAddressesByName(inetHost)));
    } catch (UnknownHostException e) {
      promise.setFailure(e);
    } 
  }
}

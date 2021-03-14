package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.Promise;
import java.io.Closeable;
import java.net.SocketAddress;
import java.util.List;

public interface AddressResolver<T extends SocketAddress> extends Closeable {
  boolean isSupported(SocketAddress paramSocketAddress);
  
  boolean isResolved(SocketAddress paramSocketAddress);
  
  Future<T> resolve(SocketAddress paramSocketAddress);
  
  Future<T> resolve(SocketAddress paramSocketAddress, Promise<T> paramPromise);
  
  Future<List<T>> resolveAll(SocketAddress paramSocketAddress);
  
  Future<List<T>> resolveAll(SocketAddress paramSocketAddress, Promise<List<T>> paramPromise);
  
  void close();
}

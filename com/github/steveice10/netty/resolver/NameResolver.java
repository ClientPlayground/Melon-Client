package com.github.steveice10.netty.resolver;

import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.Promise;
import java.io.Closeable;
import java.util.List;

public interface NameResolver<T> extends Closeable {
  Future<T> resolve(String paramString);
  
  Future<T> resolve(String paramString, Promise<T> paramPromise);
  
  Future<List<T>> resolveAll(String paramString);
  
  Future<List<T>> resolveAll(String paramString, Promise<List<T>> paramPromise);
  
  void close();
}

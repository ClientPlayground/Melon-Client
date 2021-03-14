package com.github.steveice10.netty.channel.pool;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.Promise;
import java.io.Closeable;

public interface ChannelPool extends Closeable {
  Future<Channel> acquire();
  
  Future<Channel> acquire(Promise<Channel> paramPromise);
  
  Future<Void> release(Channel paramChannel);
  
  Future<Void> release(Channel paramChannel, Promise<Void> paramPromise);
  
  void close();
}

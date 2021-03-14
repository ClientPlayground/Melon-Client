package com.github.steveice10.netty.channel.pool;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.ReadOnlyIterator;
import java.io.Closeable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractChannelPoolMap<K, P extends ChannelPool> implements ChannelPoolMap<K, P>, Iterable<Map.Entry<K, P>>, Closeable {
  private final ConcurrentMap<K, P> map = PlatformDependent.newConcurrentHashMap();
  
  public final P get(K key) {
    ChannelPool channelPool = (ChannelPool)this.map.get(ObjectUtil.checkNotNull(key, "key"));
    if (channelPool == null) {
      channelPool = (ChannelPool)newPool(key);
      ChannelPool channelPool1 = (ChannelPool)this.map.putIfAbsent(key, (P)channelPool);
      if (channelPool1 != null) {
        channelPool.close();
        channelPool = channelPool1;
      } 
    } 
    return (P)channelPool;
  }
  
  public final boolean remove(K key) {
    ChannelPool channelPool = (ChannelPool)this.map.remove(ObjectUtil.checkNotNull(key, "key"));
    if (channelPool != null) {
      channelPool.close();
      return true;
    } 
    return false;
  }
  
  public final Iterator<Map.Entry<K, P>> iterator() {
    return (Iterator<Map.Entry<K, P>>)new ReadOnlyIterator(this.map.entrySet().iterator());
  }
  
  public final int size() {
    return this.map.size();
  }
  
  public final boolean isEmpty() {
    return this.map.isEmpty();
  }
  
  public final boolean contains(K key) {
    return this.map.containsKey(ObjectUtil.checkNotNull(key, "key"));
  }
  
  protected abstract P newPool(K paramK);
  
  public final void close() {
    for (K key : this.map.keySet())
      remove(key); 
  }
}

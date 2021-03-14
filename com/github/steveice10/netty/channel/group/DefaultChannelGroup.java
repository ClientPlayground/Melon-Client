package com.github.steveice10.netty.channel.group;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelId;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultChannelGroup extends AbstractSet<Channel> implements ChannelGroup {
  private static final AtomicInteger nextId = new AtomicInteger();
  
  private final String name;
  
  private final EventExecutor executor;
  
  private final ConcurrentMap<ChannelId, Channel> serverChannels = PlatformDependent.newConcurrentHashMap();
  
  private final ConcurrentMap<ChannelId, Channel> nonServerChannels = PlatformDependent.newConcurrentHashMap();
  
  private final ChannelFutureListener remover = new ChannelFutureListener() {
      public void operationComplete(ChannelFuture future) throws Exception {
        DefaultChannelGroup.this.remove(future.channel());
      }
    };
  
  private final VoidChannelGroupFuture voidFuture = new VoidChannelGroupFuture(this);
  
  private final boolean stayClosed;
  
  private volatile boolean closed;
  
  public DefaultChannelGroup(EventExecutor executor) {
    this(executor, false);
  }
  
  public DefaultChannelGroup(String name, EventExecutor executor) {
    this(name, executor, false);
  }
  
  public DefaultChannelGroup(EventExecutor executor, boolean stayClosed) {
    this("group-0x" + Integer.toHexString(nextId.incrementAndGet()), executor, stayClosed);
  }
  
  public DefaultChannelGroup(String name, EventExecutor executor, boolean stayClosed) {
    if (name == null)
      throw new NullPointerException("name"); 
    this.name = name;
    this.executor = executor;
    this.stayClosed = stayClosed;
  }
  
  public String name() {
    return this.name;
  }
  
  public Channel find(ChannelId id) {
    Channel c = this.nonServerChannels.get(id);
    if (c != null)
      return c; 
    return this.serverChannels.get(id);
  }
  
  public boolean isEmpty() {
    return (this.nonServerChannels.isEmpty() && this.serverChannels.isEmpty());
  }
  
  public int size() {
    return this.nonServerChannels.size() + this.serverChannels.size();
  }
  
  public boolean contains(Object o) {
    if (o instanceof com.github.steveice10.netty.channel.ServerChannel)
      return this.serverChannels.containsValue(o); 
    if (o instanceof Channel)
      return this.nonServerChannels.containsValue(o); 
    return false;
  }
  
  public boolean add(Channel channel) {
    ConcurrentMap<ChannelId, Channel> map = (channel instanceof com.github.steveice10.netty.channel.ServerChannel) ? this.serverChannels : this.nonServerChannels;
    boolean added = (map.putIfAbsent(channel.id(), channel) == null);
    if (added)
      channel.closeFuture().addListener((GenericFutureListener)this.remover); 
    if (this.stayClosed && this.closed)
      channel.close(); 
    return added;
  }
  
  public boolean remove(Object o) {
    Channel c = null;
    if (o instanceof ChannelId) {
      c = this.nonServerChannels.remove(o);
      if (c == null)
        c = this.serverChannels.remove(o); 
    } else if (o instanceof Channel) {
      c = (Channel)o;
      if (c instanceof com.github.steveice10.netty.channel.ServerChannel) {
        c = this.serverChannels.remove(c.id());
      } else {
        c = this.nonServerChannels.remove(c.id());
      } 
    } 
    if (c == null)
      return false; 
    c.closeFuture().removeListener((GenericFutureListener)this.remover);
    return true;
  }
  
  public void clear() {
    this.nonServerChannels.clear();
    this.serverChannels.clear();
  }
  
  public Iterator<Channel> iterator() {
    return new CombinedIterator<Channel>(this.serverChannels
        .values().iterator(), this.nonServerChannels
        .values().iterator());
  }
  
  public Object[] toArray() {
    Collection<Channel> channels = new ArrayList<Channel>(size());
    channels.addAll(this.serverChannels.values());
    channels.addAll(this.nonServerChannels.values());
    return channels.toArray();
  }
  
  public <T> T[] toArray(T[] a) {
    Collection<Channel> channels = new ArrayList<Channel>(size());
    channels.addAll(this.serverChannels.values());
    channels.addAll(this.nonServerChannels.values());
    return channels.toArray(a);
  }
  
  public ChannelGroupFuture close() {
    return close(ChannelMatchers.all());
  }
  
  public ChannelGroupFuture disconnect() {
    return disconnect(ChannelMatchers.all());
  }
  
  public ChannelGroupFuture deregister() {
    return deregister(ChannelMatchers.all());
  }
  
  public ChannelGroupFuture write(Object message) {
    return write(message, ChannelMatchers.all());
  }
  
  private static Object safeDuplicate(Object message) {
    if (message instanceof ByteBuf)
      return ((ByteBuf)message).retainedDuplicate(); 
    if (message instanceof ByteBufHolder)
      return ((ByteBufHolder)message).retainedDuplicate(); 
    return ReferenceCountUtil.retain(message);
  }
  
  public ChannelGroupFuture write(Object message, ChannelMatcher matcher) {
    return write(message, matcher, false);
  }
  
  public ChannelGroupFuture write(Object message, ChannelMatcher matcher, boolean voidPromise) {
    ChannelGroupFuture future;
    if (message == null)
      throw new NullPointerException("message"); 
    if (matcher == null)
      throw new NullPointerException("matcher"); 
    if (voidPromise) {
      for (Channel c : this.nonServerChannels.values()) {
        if (matcher.matches(c))
          c.write(safeDuplicate(message), c.voidPromise()); 
      } 
      future = this.voidFuture;
    } else {
      Map<Channel, ChannelFuture> futures = new LinkedHashMap<Channel, ChannelFuture>(size());
      for (Channel c : this.nonServerChannels.values()) {
        if (matcher.matches(c))
          futures.put(c, c.write(safeDuplicate(message))); 
      } 
      future = new DefaultChannelGroupFuture(this, futures, this.executor);
    } 
    ReferenceCountUtil.release(message);
    return future;
  }
  
  public ChannelGroup flush() {
    return flush(ChannelMatchers.all());
  }
  
  public ChannelGroupFuture flushAndWrite(Object message) {
    return writeAndFlush(message);
  }
  
  public ChannelGroupFuture writeAndFlush(Object message) {
    return writeAndFlush(message, ChannelMatchers.all());
  }
  
  public ChannelGroupFuture disconnect(ChannelMatcher matcher) {
    if (matcher == null)
      throw new NullPointerException("matcher"); 
    Map<Channel, ChannelFuture> futures = new LinkedHashMap<Channel, ChannelFuture>(size());
    for (Channel c : this.serverChannels.values()) {
      if (matcher.matches(c))
        futures.put(c, c.disconnect()); 
    } 
    for (Channel c : this.nonServerChannels.values()) {
      if (matcher.matches(c))
        futures.put(c, c.disconnect()); 
    } 
    return new DefaultChannelGroupFuture(this, futures, this.executor);
  }
  
  public ChannelGroupFuture close(ChannelMatcher matcher) {
    if (matcher == null)
      throw new NullPointerException("matcher"); 
    Map<Channel, ChannelFuture> futures = new LinkedHashMap<Channel, ChannelFuture>(size());
    if (this.stayClosed)
      this.closed = true; 
    for (Channel c : this.serverChannels.values()) {
      if (matcher.matches(c))
        futures.put(c, c.close()); 
    } 
    for (Channel c : this.nonServerChannels.values()) {
      if (matcher.matches(c))
        futures.put(c, c.close()); 
    } 
    return new DefaultChannelGroupFuture(this, futures, this.executor);
  }
  
  public ChannelGroupFuture deregister(ChannelMatcher matcher) {
    if (matcher == null)
      throw new NullPointerException("matcher"); 
    Map<Channel, ChannelFuture> futures = new LinkedHashMap<Channel, ChannelFuture>(size());
    for (Channel c : this.serverChannels.values()) {
      if (matcher.matches(c))
        futures.put(c, c.deregister()); 
    } 
    for (Channel c : this.nonServerChannels.values()) {
      if (matcher.matches(c))
        futures.put(c, c.deregister()); 
    } 
    return new DefaultChannelGroupFuture(this, futures, this.executor);
  }
  
  public ChannelGroup flush(ChannelMatcher matcher) {
    for (Channel c : this.nonServerChannels.values()) {
      if (matcher.matches(c))
        c.flush(); 
    } 
    return this;
  }
  
  public ChannelGroupFuture flushAndWrite(Object message, ChannelMatcher matcher) {
    return writeAndFlush(message, matcher);
  }
  
  public ChannelGroupFuture writeAndFlush(Object message, ChannelMatcher matcher) {
    return writeAndFlush(message, matcher, false);
  }
  
  public ChannelGroupFuture writeAndFlush(Object message, ChannelMatcher matcher, boolean voidPromise) {
    ChannelGroupFuture future;
    if (message == null)
      throw new NullPointerException("message"); 
    if (voidPromise) {
      for (Channel c : this.nonServerChannels.values()) {
        if (matcher.matches(c))
          c.writeAndFlush(safeDuplicate(message), c.voidPromise()); 
      } 
      future = this.voidFuture;
    } else {
      Map<Channel, ChannelFuture> futures = new LinkedHashMap<Channel, ChannelFuture>(size());
      for (Channel c : this.nonServerChannels.values()) {
        if (matcher.matches(c))
          futures.put(c, c.writeAndFlush(safeDuplicate(message))); 
      } 
      future = new DefaultChannelGroupFuture(this, futures, this.executor);
    } 
    ReferenceCountUtil.release(message);
    return future;
  }
  
  public ChannelGroupFuture newCloseFuture() {
    return newCloseFuture(ChannelMatchers.all());
  }
  
  public ChannelGroupFuture newCloseFuture(ChannelMatcher matcher) {
    Map<Channel, ChannelFuture> futures = new LinkedHashMap<Channel, ChannelFuture>(size());
    for (Channel c : this.serverChannels.values()) {
      if (matcher.matches(c))
        futures.put(c, c.closeFuture()); 
    } 
    for (Channel c : this.nonServerChannels.values()) {
      if (matcher.matches(c))
        futures.put(c, c.closeFuture()); 
    } 
    return new DefaultChannelGroupFuture(this, futures, this.executor);
  }
  
  public int hashCode() {
    return System.identityHashCode(this);
  }
  
  public boolean equals(Object o) {
    return (this == o);
  }
  
  public int compareTo(ChannelGroup o) {
    int v = name().compareTo(o.name());
    if (v != 0)
      return v; 
    return System.identityHashCode(this) - System.identityHashCode(o);
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + "(name: " + name() + ", size: " + size() + ')';
  }
}

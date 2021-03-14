package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.handler.codec.dns.DnsRecord;
import com.github.steveice10.netty.util.concurrent.ScheduledFuture;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultDnsCache implements DnsCache {
  private final ConcurrentMap<String, Entries> resolveCache = PlatformDependent.newConcurrentHashMap();
  
  private static final int MAX_SUPPORTED_TTL_SECS = (int)TimeUnit.DAYS.toSeconds(730L);
  
  private final int minTtl;
  
  private final int maxTtl;
  
  private final int negativeTtl;
  
  public DefaultDnsCache() {
    this(0, MAX_SUPPORTED_TTL_SECS, 0);
  }
  
  public DefaultDnsCache(int minTtl, int maxTtl, int negativeTtl) {
    this.minTtl = Math.min(MAX_SUPPORTED_TTL_SECS, ObjectUtil.checkPositiveOrZero(minTtl, "minTtl"));
    this.maxTtl = Math.min(MAX_SUPPORTED_TTL_SECS, ObjectUtil.checkPositiveOrZero(maxTtl, "maxTtl"));
    if (minTtl > maxTtl)
      throw new IllegalArgumentException("minTtl: " + minTtl + ", maxTtl: " + maxTtl + " (expected: 0 <= minTtl <= maxTtl)"); 
    this.negativeTtl = ObjectUtil.checkPositiveOrZero(negativeTtl, "negativeTtl");
  }
  
  public int minTtl() {
    return this.minTtl;
  }
  
  public int maxTtl() {
    return this.maxTtl;
  }
  
  public int negativeTtl() {
    return this.negativeTtl;
  }
  
  public void clear() {
    while (!this.resolveCache.isEmpty()) {
      for (Iterator<Map.Entry<String, Entries>> i = this.resolveCache.entrySet().iterator(); i.hasNext(); ) {
        Map.Entry<String, Entries> e = i.next();
        i.remove();
        ((Entries)e.getValue()).clearAndCancel();
      } 
    } 
  }
  
  public boolean clear(String hostname) {
    ObjectUtil.checkNotNull(hostname, "hostname");
    Entries entries = this.resolveCache.remove(hostname);
    return (entries != null && entries.clearAndCancel());
  }
  
  private static boolean emptyAdditionals(DnsRecord[] additionals) {
    return (additionals == null || additionals.length == 0);
  }
  
  public List<? extends DnsCacheEntry> get(String hostname, DnsRecord[] additionals) {
    ObjectUtil.checkNotNull(hostname, "hostname");
    if (!emptyAdditionals(additionals))
      return Collections.emptyList(); 
    Entries entries = this.resolveCache.get(hostname);
    return (entries == null) ? null : (List)entries.get();
  }
  
  public DnsCacheEntry cache(String hostname, DnsRecord[] additionals, InetAddress address, long originalTtl, EventLoop loop) {
    ObjectUtil.checkNotNull(hostname, "hostname");
    ObjectUtil.checkNotNull(address, "address");
    ObjectUtil.checkNotNull(loop, "loop");
    DefaultDnsCacheEntry e = new DefaultDnsCacheEntry(hostname, address);
    if (this.maxTtl == 0 || !emptyAdditionals(additionals))
      return e; 
    cache0(e, Math.max(this.minTtl, Math.min(MAX_SUPPORTED_TTL_SECS, (int)Math.min(this.maxTtl, originalTtl))), loop);
    return e;
  }
  
  public DnsCacheEntry cache(String hostname, DnsRecord[] additionals, Throwable cause, EventLoop loop) {
    ObjectUtil.checkNotNull(hostname, "hostname");
    ObjectUtil.checkNotNull(cause, "cause");
    ObjectUtil.checkNotNull(loop, "loop");
    DefaultDnsCacheEntry e = new DefaultDnsCacheEntry(hostname, cause);
    if (this.negativeTtl == 0 || !emptyAdditionals(additionals))
      return e; 
    cache0(e, Math.min(MAX_SUPPORTED_TTL_SECS, this.negativeTtl), loop);
    return e;
  }
  
  private void cache0(DefaultDnsCacheEntry e, int ttl, EventLoop loop) {
    Entries entries = this.resolveCache.get(e.hostname());
    if (entries == null) {
      entries = new Entries(e);
      Entries oldEntries = this.resolveCache.putIfAbsent(e.hostname(), entries);
      if (oldEntries != null)
        entries = oldEntries; 
    } 
    entries.add(e);
    scheduleCacheExpiration(e, ttl, loop);
  }
  
  private void scheduleCacheExpiration(final DefaultDnsCacheEntry e, int ttl, EventLoop loop) {
    e.scheduleExpiration(loop, new Runnable() {
          public void run() {
            DefaultDnsCache.Entries entries = (DefaultDnsCache.Entries)DefaultDnsCache.this.resolveCache.remove(e.hostname);
            if (entries != null)
              entries.clearAndCancel(); 
          }
        },  ttl, TimeUnit.SECONDS);
  }
  
  public String toString() {
    return "DefaultDnsCache(minTtl=" + 
      this.minTtl + 
      ", maxTtl=" + this.maxTtl + 
      ", negativeTtl=" + this.negativeTtl + 
      ", cached resolved hostname=" + this.resolveCache
      .size() + ")";
  }
  
  private static final class DefaultDnsCacheEntry implements DnsCacheEntry {
    private final String hostname;
    
    private final InetAddress address;
    
    private final Throwable cause;
    
    private volatile ScheduledFuture<?> expirationFuture;
    
    DefaultDnsCacheEntry(String hostname, InetAddress address) {
      this.hostname = (String)ObjectUtil.checkNotNull(hostname, "hostname");
      this.address = (InetAddress)ObjectUtil.checkNotNull(address, "address");
      this.cause = null;
    }
    
    DefaultDnsCacheEntry(String hostname, Throwable cause) {
      this.hostname = (String)ObjectUtil.checkNotNull(hostname, "hostname");
      this.cause = (Throwable)ObjectUtil.checkNotNull(cause, "cause");
      this.address = null;
    }
    
    public InetAddress address() {
      return this.address;
    }
    
    public Throwable cause() {
      return this.cause;
    }
    
    String hostname() {
      return this.hostname;
    }
    
    void scheduleExpiration(EventLoop loop, Runnable task, long delay, TimeUnit unit) {
      assert this.expirationFuture == null : "expiration task scheduled already";
      this.expirationFuture = loop.schedule(task, delay, unit);
    }
    
    void cancelExpiration() {
      ScheduledFuture<?> expirationFuture = this.expirationFuture;
      if (expirationFuture != null)
        expirationFuture.cancel(false); 
    }
    
    public String toString() {
      if (this.cause != null)
        return this.hostname + '/' + this.cause; 
      return this.address.toString();
    }
  }
  
  private static final class Entries extends AtomicReference<List<DefaultDnsCacheEntry>> {
    Entries(DefaultDnsCache.DefaultDnsCacheEntry entry) {
      super(Collections.singletonList(entry));
    }
    
    void add(DefaultDnsCache.DefaultDnsCacheEntry e) {
      if (e.cause() == null) {
        while (true) {
          List<DefaultDnsCache.DefaultDnsCacheEntry> list = get();
          if (!list.isEmpty()) {
            DefaultDnsCache.DefaultDnsCacheEntry firstEntry = list.get(0);
            if (firstEntry.cause() != null) {
              assert list.size() == 1;
              if (compareAndSet(list, Collections.singletonList(e))) {
                firstEntry.cancelExpiration();
                return;
              } 
              continue;
            } 
            List<DefaultDnsCache.DefaultDnsCacheEntry> newEntries = new ArrayList<DefaultDnsCache.DefaultDnsCacheEntry>(list.size() + 1);
            DefaultDnsCache.DefaultDnsCacheEntry replacedEntry = null;
            for (int i = 0; i < list.size(); i++) {
              DefaultDnsCache.DefaultDnsCacheEntry entry = list.get(i);
              if (!e.address().equals(entry.address())) {
                newEntries.add(entry);
              } else {
                assert replacedEntry == null;
                replacedEntry = entry;
              } 
            } 
            newEntries.add(e);
            if (compareAndSet(list, newEntries)) {
              if (replacedEntry != null)
                replacedEntry.cancelExpiration(); 
              return;
            } 
            continue;
          } 
          if (compareAndSet(list, Collections.singletonList(e)))
            break; 
        } 
        return;
      } 
      List<DefaultDnsCache.DefaultDnsCacheEntry> entries = getAndSet(Collections.singletonList(e));
      cancelExpiration(entries);
    }
    
    boolean clearAndCancel() {
      List<DefaultDnsCache.DefaultDnsCacheEntry> entries = getAndSet(Collections.emptyList());
      if (entries.isEmpty())
        return false; 
      cancelExpiration(entries);
      return true;
    }
    
    private static void cancelExpiration(List<DefaultDnsCache.DefaultDnsCacheEntry> entryList) {
      int numEntries = entryList.size();
      for (int i = 0; i < numEntries; i++)
        ((DefaultDnsCache.DefaultDnsCacheEntry)entryList.get(i)).cancelExpiration(); 
    }
  }
}

package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.util.NetUtil;
import com.github.steveice10.netty.util.collection.IntObjectHashMap;
import com.github.steveice10.netty.util.collection.IntObjectMap;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

final class DnsQueryContextManager {
  final Map<InetSocketAddress, IntObjectMap<DnsQueryContext>> map = new HashMap<InetSocketAddress, IntObjectMap<DnsQueryContext>>();
  
  int add(DnsQueryContext qCtx) {
    IntObjectMap<DnsQueryContext> contexts = getOrCreateContextMap(qCtx.nameServerAddr());
    int id = PlatformDependent.threadLocalRandom().nextInt(65535) + 1;
    int maxTries = 131070;
    int tries = 0;
    synchronized (contexts) {
      while (true) {
        if (!contexts.containsKey(id)) {
          contexts.put(id, qCtx);
          return id;
        } 
        id = id + 1 & 0xFFFF;
        if (++tries >= 131070)
          throw new IllegalStateException("query ID space exhausted: " + qCtx.question()); 
      } 
    } 
  }
  
  DnsQueryContext get(InetSocketAddress nameServerAddr, int id) {
    DnsQueryContext qCtx;
    IntObjectMap<DnsQueryContext> contexts = getContextMap(nameServerAddr);
    if (contexts != null) {
      synchronized (contexts) {
        qCtx = (DnsQueryContext)contexts.get(id);
      } 
    } else {
      qCtx = null;
    } 
    return qCtx;
  }
  
  DnsQueryContext remove(InetSocketAddress nameServerAddr, int id) {
    IntObjectMap<DnsQueryContext> contexts = getContextMap(nameServerAddr);
    if (contexts == null)
      return null; 
    synchronized (contexts) {
      return (DnsQueryContext)contexts.remove(id);
    } 
  }
  
  private IntObjectMap<DnsQueryContext> getContextMap(InetSocketAddress nameServerAddr) {
    synchronized (this.map) {
      return this.map.get(nameServerAddr);
    } 
  }
  
  private IntObjectMap<DnsQueryContext> getOrCreateContextMap(InetSocketAddress nameServerAddr) {
    synchronized (this.map) {
      IntObjectMap<DnsQueryContext> contexts = this.map.get(nameServerAddr);
      if (contexts != null)
        return contexts; 
      IntObjectHashMap intObjectHashMap = new IntObjectHashMap();
      InetAddress a = nameServerAddr.getAddress();
      int port = nameServerAddr.getPort();
      this.map.put(nameServerAddr, intObjectHashMap);
      if (a instanceof Inet4Address) {
        Inet4Address a4 = (Inet4Address)a;
        if (a4.isLoopbackAddress()) {
          this.map.put(new InetSocketAddress(NetUtil.LOCALHOST6, port), intObjectHashMap);
        } else {
          this.map.put(new InetSocketAddress(toCompactAddress(a4), port), intObjectHashMap);
        } 
      } else if (a instanceof Inet6Address) {
        Inet6Address a6 = (Inet6Address)a;
        if (a6.isLoopbackAddress()) {
          this.map.put(new InetSocketAddress(NetUtil.LOCALHOST4, port), intObjectHashMap);
        } else if (a6.isIPv4CompatibleAddress()) {
          this.map.put(new InetSocketAddress(toIPv4Address(a6), port), intObjectHashMap);
        } 
      } 
      return (IntObjectMap<DnsQueryContext>)intObjectHashMap;
    } 
  }
  
  private static Inet6Address toCompactAddress(Inet4Address a4) {
    byte[] b4 = a4.getAddress();
    byte[] b6 = { 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, b4[0], b4[1], b4[2], b4[3] };
    try {
      return (Inet6Address)InetAddress.getByAddress(b6);
    } catch (UnknownHostException e) {
      throw new Error(e);
    } 
  }
  
  private static Inet4Address toIPv4Address(Inet6Address a6) {
    byte[] b6 = a6.getAddress();
    byte[] b4 = { b6[12], b6[13], b6[14], b6[15] };
    try {
      return (Inet4Address)InetAddress.getByAddress(b4);
    } catch (UnknownHostException e) {
      throw new Error(e);
    } 
  }
}

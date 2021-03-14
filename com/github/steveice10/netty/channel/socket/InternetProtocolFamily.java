package com.github.steveice10.netty.channel.socket;

import com.github.steveice10.netty.util.NetUtil;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public enum InternetProtocolFamily {
  IPv4((Class)Inet4Address.class, 1, NetUtil.LOCALHOST4),
  IPv6((Class)Inet6Address.class, 2, NetUtil.LOCALHOST6);
  
  private final Class<? extends InetAddress> addressType;
  
  private final int addressNumber;
  
  private final InetAddress localHost;
  
  InternetProtocolFamily(Class<? extends InetAddress> addressType, int addressNumber, InetAddress localHost) {
    this.addressType = addressType;
    this.addressNumber = addressNumber;
    this.localHost = localHost;
  }
  
  public Class<? extends InetAddress> addressType() {
    return this.addressType;
  }
  
  public int addressNumber() {
    return this.addressNumber;
  }
  
  public InetAddress localhost() {
    return this.localHost;
  }
  
  public static InternetProtocolFamily of(InetAddress address) {
    if (address instanceof Inet4Address)
      return IPv4; 
    if (address instanceof Inet6Address)
      return IPv6; 
    throw new IllegalArgumentException("address " + address + " not supported");
  }
}

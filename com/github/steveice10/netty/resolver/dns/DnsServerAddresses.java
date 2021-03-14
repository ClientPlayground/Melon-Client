package com.github.steveice10.netty.resolver.dns;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DnsServerAddresses {
  @Deprecated
  public static List<InetSocketAddress> defaultAddressList() {
    return DefaultDnsServerAddressStreamProvider.defaultAddressList();
  }
  
  @Deprecated
  public static DnsServerAddresses defaultAddresses() {
    return DefaultDnsServerAddressStreamProvider.defaultAddresses();
  }
  
  public static DnsServerAddresses sequential(Iterable<? extends InetSocketAddress> addresses) {
    return sequential0(sanitize(addresses));
  }
  
  public static DnsServerAddresses sequential(InetSocketAddress... addresses) {
    return sequential0(sanitize(addresses));
  }
  
  private static DnsServerAddresses sequential0(InetSocketAddress... addresses) {
    if (addresses.length == 1)
      return singleton(addresses[0]); 
    return new DefaultDnsServerAddresses("sequential", addresses) {
        public DnsServerAddressStream stream() {
          return new SequentialDnsServerAddressStream(this.addresses, 0);
        }
      };
  }
  
  public static DnsServerAddresses shuffled(Iterable<? extends InetSocketAddress> addresses) {
    return shuffled0(sanitize(addresses));
  }
  
  public static DnsServerAddresses shuffled(InetSocketAddress... addresses) {
    return shuffled0(sanitize(addresses));
  }
  
  private static DnsServerAddresses shuffled0(InetSocketAddress[] addresses) {
    if (addresses.length == 1)
      return singleton(addresses[0]); 
    return new DefaultDnsServerAddresses("shuffled", addresses) {
        public DnsServerAddressStream stream() {
          return new ShuffledDnsServerAddressStream(this.addresses);
        }
      };
  }
  
  public static DnsServerAddresses rotational(Iterable<? extends InetSocketAddress> addresses) {
    return rotational0(sanitize(addresses));
  }
  
  public static DnsServerAddresses rotational(InetSocketAddress... addresses) {
    return rotational0(sanitize(addresses));
  }
  
  private static DnsServerAddresses rotational0(InetSocketAddress[] addresses) {
    if (addresses.length == 1)
      return singleton(addresses[0]); 
    return new RotationalDnsServerAddresses(addresses);
  }
  
  public static DnsServerAddresses singleton(InetSocketAddress address) {
    if (address == null)
      throw new NullPointerException("address"); 
    if (address.isUnresolved())
      throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + address); 
    return new SingletonDnsServerAddresses(address);
  }
  
  private static InetSocketAddress[] sanitize(Iterable<? extends InetSocketAddress> addresses) {
    List<InetSocketAddress> list;
    if (addresses == null)
      throw new NullPointerException("addresses"); 
    if (addresses instanceof Collection) {
      list = new ArrayList<InetSocketAddress>(((Collection)addresses).size());
    } else {
      list = new ArrayList<InetSocketAddress>(4);
    } 
    for (InetSocketAddress a : addresses) {
      if (a == null)
        break; 
      if (a.isUnresolved())
        throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + a); 
      list.add(a);
    } 
    if (list.isEmpty())
      throw new IllegalArgumentException("empty addresses"); 
    return list.<InetSocketAddress>toArray(new InetSocketAddress[list.size()]);
  }
  
  private static InetSocketAddress[] sanitize(InetSocketAddress[] addresses) {
    if (addresses == null)
      throw new NullPointerException("addresses"); 
    List<InetSocketAddress> list = new ArrayList<InetSocketAddress>(addresses.length);
    for (InetSocketAddress a : addresses) {
      if (a == null)
        break; 
      if (a.isUnresolved())
        throw new IllegalArgumentException("cannot use an unresolved DNS server address: " + a); 
      list.add(a);
    } 
    if (list.isEmpty())
      return DefaultDnsServerAddressStreamProvider.defaultAddressArray(); 
    return list.<InetSocketAddress>toArray(new InetSocketAddress[list.size()]);
  }
  
  public abstract DnsServerAddressStream stream();
}

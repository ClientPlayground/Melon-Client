package com.github.steveice10.netty.resolver.dns;

import java.net.InetSocketAddress;

final class SequentialDnsServerAddressStream implements DnsServerAddressStream {
  private final InetSocketAddress[] addresses;
  
  private int i;
  
  SequentialDnsServerAddressStream(InetSocketAddress[] addresses, int startIdx) {
    this.addresses = addresses;
    this.i = startIdx;
  }
  
  public InetSocketAddress next() {
    int i = this.i;
    InetSocketAddress next = this.addresses[i];
    if (++i < this.addresses.length) {
      this.i = i;
    } else {
      this.i = 0;
    } 
    return next;
  }
  
  public int size() {
    return this.addresses.length;
  }
  
  public SequentialDnsServerAddressStream duplicate() {
    return new SequentialDnsServerAddressStream(this.addresses, this.i);
  }
  
  public String toString() {
    return toString("sequential", this.i, this.addresses);
  }
  
  static String toString(String type, int index, InetSocketAddress[] addresses) {
    StringBuilder buf = new StringBuilder(type.length() + 2 + addresses.length * 16);
    buf.append(type).append("(index: ").append(index);
    buf.append(", addrs: (");
    for (InetSocketAddress a : addresses)
      buf.append(a).append(", "); 
    buf.setLength(buf.length() - 2);
    buf.append("))");
    return buf.toString();
  }
}

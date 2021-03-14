package com.github.steveice10.netty.resolver.dns;

import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.net.InetSocketAddress;
import java.util.Random;

final class ShuffledDnsServerAddressStream implements DnsServerAddressStream {
  private final InetSocketAddress[] addresses;
  
  private int i;
  
  ShuffledDnsServerAddressStream(InetSocketAddress[] addresses) {
    this.addresses = addresses;
    shuffle();
  }
  
  private ShuffledDnsServerAddressStream(InetSocketAddress[] addresses, int startIdx) {
    this.addresses = addresses;
    this.i = startIdx;
  }
  
  private void shuffle() {
    InetSocketAddress[] addresses = this.addresses;
    Random r = PlatformDependent.threadLocalRandom();
    for (int i = addresses.length - 1; i >= 0; i--) {
      InetSocketAddress tmp = addresses[i];
      int j = r.nextInt(i + 1);
      addresses[i] = addresses[j];
      addresses[j] = tmp;
    } 
  }
  
  public InetSocketAddress next() {
    int i = this.i;
    InetSocketAddress next = this.addresses[i];
    if (++i < this.addresses.length) {
      this.i = i;
    } else {
      this.i = 0;
      shuffle();
    } 
    return next;
  }
  
  public int size() {
    return this.addresses.length;
  }
  
  public ShuffledDnsServerAddressStream duplicate() {
    return new ShuffledDnsServerAddressStream(this.addresses, this.i);
  }
  
  public String toString() {
    return SequentialDnsServerAddressStream.toString("shuffled", this.i, this.addresses);
  }
}

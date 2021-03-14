package com.github.steveice10.netty.channel.unix;

import java.net.InetSocketAddress;

public final class DatagramSocketAddress extends InetSocketAddress {
  private static final long serialVersionUID = 3094819287843178401L;
  
  private final int receivedAmount;
  
  private final DatagramSocketAddress localAddress;
  
  DatagramSocketAddress(String addr, int port, int receivedAmount, DatagramSocketAddress local) {
    super(addr, port);
    this.receivedAmount = receivedAmount;
    this.localAddress = local;
  }
  
  public DatagramSocketAddress localAddress() {
    return this.localAddress;
  }
  
  public int receivedAmount() {
    return this.receivedAmount;
  }
}

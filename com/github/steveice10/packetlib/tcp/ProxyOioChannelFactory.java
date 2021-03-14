package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFactory;
import com.github.steveice10.netty.channel.socket.oio.OioSocketChannel;
import java.net.Proxy;
import java.net.Socket;

public class ProxyOioChannelFactory implements ChannelFactory<OioSocketChannel> {
  private Proxy proxy;
  
  public ProxyOioChannelFactory(Proxy proxy) {
    this.proxy = proxy;
  }
  
  public OioSocketChannel newChannel() {
    return new OioSocketChannel(new Socket(this.proxy));
  }
}

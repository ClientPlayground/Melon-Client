package com.github.steveice10.netty.channel.udt.nio;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.SocketChannelUDT;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.udt.UdtChannel;

@Deprecated
public class NioUdtMessageAcceptorChannel extends NioUdtAcceptorChannel {
  public NioUdtMessageAcceptorChannel() {
    super(TypeUDT.DATAGRAM);
  }
  
  protected UdtChannel newConnectorChannel(SocketChannelUDT channelUDT) {
    return new NioUdtMessageConnectorChannel((Channel)this, channelUDT);
  }
}

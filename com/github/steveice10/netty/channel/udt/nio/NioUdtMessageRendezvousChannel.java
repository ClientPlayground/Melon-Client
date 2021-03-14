package com.github.steveice10.netty.channel.udt.nio;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.SocketChannelUDT;

@Deprecated
public class NioUdtMessageRendezvousChannel extends NioUdtMessageConnectorChannel {
  public NioUdtMessageRendezvousChannel() {
    super((SocketChannelUDT)NioUdtProvider.newRendezvousChannelUDT(TypeUDT.DATAGRAM));
  }
}

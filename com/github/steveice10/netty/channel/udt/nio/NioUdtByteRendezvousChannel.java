package com.github.steveice10.netty.channel.udt.nio;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.SocketChannelUDT;

@Deprecated
public class NioUdtByteRendezvousChannel extends NioUdtByteConnectorChannel {
  public NioUdtByteRendezvousChannel() {
    super((SocketChannelUDT)NioUdtProvider.newRendezvousChannelUDT(TypeUDT.STREAM));
  }
}

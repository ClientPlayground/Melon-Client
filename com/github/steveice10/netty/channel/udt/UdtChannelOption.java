package com.github.steveice10.netty.channel.udt;

import com.github.steveice10.netty.channel.ChannelOption;

@Deprecated
public final class UdtChannelOption<T> extends ChannelOption<T> {
  public static final ChannelOption<Integer> PROTOCOL_RECEIVE_BUFFER_SIZE = valueOf(UdtChannelOption.class, "PROTOCOL_RECEIVE_BUFFER_SIZE");
  
  public static final ChannelOption<Integer> PROTOCOL_SEND_BUFFER_SIZE = valueOf(UdtChannelOption.class, "PROTOCOL_SEND_BUFFER_SIZE");
  
  public static final ChannelOption<Integer> SYSTEM_RECEIVE_BUFFER_SIZE = valueOf(UdtChannelOption.class, "SYSTEM_RECEIVE_BUFFER_SIZE");
  
  public static final ChannelOption<Integer> SYSTEM_SEND_BUFFER_SIZE = valueOf(UdtChannelOption.class, "SYSTEM_SEND_BUFFER_SIZE");
  
  private UdtChannelOption() {
    super(null);
  }
}

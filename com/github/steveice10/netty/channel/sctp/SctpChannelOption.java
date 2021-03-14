package com.github.steveice10.netty.channel.sctp;

import com.github.steveice10.netty.channel.ChannelOption;
import com.sun.nio.sctp.SctpStandardSocketOptions;
import java.net.SocketAddress;

public final class SctpChannelOption<T> extends ChannelOption<T> {
  public static final ChannelOption<Boolean> SCTP_DISABLE_FRAGMENTS = valueOf(SctpChannelOption.class, "SCTP_DISABLE_FRAGMENTS");
  
  public static final ChannelOption<Boolean> SCTP_EXPLICIT_COMPLETE = valueOf(SctpChannelOption.class, "SCTP_EXPLICIT_COMPLETE");
  
  public static final ChannelOption<Integer> SCTP_FRAGMENT_INTERLEAVE = valueOf(SctpChannelOption.class, "SCTP_FRAGMENT_INTERLEAVE");
  
  public static final ChannelOption<SctpStandardSocketOptions.InitMaxStreams> SCTP_INIT_MAXSTREAMS = valueOf(SctpChannelOption.class, "SCTP_INIT_MAXSTREAMS");
  
  public static final ChannelOption<Boolean> SCTP_NODELAY = valueOf(SctpChannelOption.class, "SCTP_NODELAY");
  
  public static final ChannelOption<SocketAddress> SCTP_PRIMARY_ADDR = valueOf(SctpChannelOption.class, "SCTP_PRIMARY_ADDR");
  
  public static final ChannelOption<SocketAddress> SCTP_SET_PEER_PRIMARY_ADDR = valueOf(SctpChannelOption.class, "SCTP_SET_PEER_PRIMARY_ADDR");
  
  private SctpChannelOption() {
    super(null);
  }
}

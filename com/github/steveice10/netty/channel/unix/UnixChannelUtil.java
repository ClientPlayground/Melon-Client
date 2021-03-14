package com.github.steveice10.netty.channel.unix;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public final class UnixChannelUtil {
  public static boolean isBufferCopyNeededForWrite(ByteBuf byteBuf) {
    return isBufferCopyNeededForWrite(byteBuf, Limits.IOV_MAX);
  }
  
  static boolean isBufferCopyNeededForWrite(ByteBuf byteBuf, int iovMax) {
    return (!byteBuf.hasMemoryAddress() && (!byteBuf.isDirect() || byteBuf.nioBufferCount() > iovMax));
  }
  
  public static InetSocketAddress computeRemoteAddr(InetSocketAddress remoteAddr, InetSocketAddress osRemoteAddr) {
    if (osRemoteAddr != null) {
      if (PlatformDependent.javaVersion() >= 7)
        try {
          return new InetSocketAddress(InetAddress.getByAddress(remoteAddr.getHostString(), osRemoteAddr
                .getAddress().getAddress()), osRemoteAddr
              .getPort());
        } catch (UnknownHostException unknownHostException) {} 
      return osRemoteAddr;
    } 
    return remoteAddr;
  }
}

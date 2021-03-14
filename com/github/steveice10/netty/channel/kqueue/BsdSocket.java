package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.channel.DefaultFileRegion;
import com.github.steveice10.netty.channel.unix.Errors;
import com.github.steveice10.netty.channel.unix.PeerCredentials;
import com.github.steveice10.netty.channel.unix.Socket;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;

final class BsdSocket extends Socket {
  private static final Errors.NativeIoException SENDFILE_CONNECTION_RESET_EXCEPTION;
  
  private static final ClosedChannelException SENDFILE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Native.class, "sendfile(..)");
  
  private static final int APPLE_SND_LOW_AT_MAX = 131072;
  
  private static final int FREEBSD_SND_LOW_AT_MAX = 32768;
  
  static final int BSD_SND_LOW_AT_MAX = Math.min(131072, 32768);
  
  static {
    SENDFILE_CONNECTION_RESET_EXCEPTION = Errors.newConnectionResetException("syscall:sendfile", Errors.ERRNO_EPIPE_NEGATIVE);
  }
  
  BsdSocket(int fd) {
    super(fd);
  }
  
  void setAcceptFilter(AcceptFilter acceptFilter) throws IOException {
    setAcceptFilter(intValue(), acceptFilter.filterName(), acceptFilter.filterArgs());
  }
  
  void setTcpNoPush(boolean tcpNoPush) throws IOException {
    setTcpNoPush(intValue(), tcpNoPush ? 1 : 0);
  }
  
  void setSndLowAt(int lowAt) throws IOException {
    setSndLowAt(intValue(), lowAt);
  }
  
  boolean isTcpNoPush() throws IOException {
    return (getTcpNoPush(intValue()) != 0);
  }
  
  int getSndLowAt() throws IOException {
    return getSndLowAt(intValue());
  }
  
  AcceptFilter getAcceptFilter() throws IOException {
    String[] result = getAcceptFilter(intValue());
    return (result == null) ? AcceptFilter.PLATFORM_UNSUPPORTED : new AcceptFilter(result[0], result[1]);
  }
  
  PeerCredentials getPeerCredentials() throws IOException {
    return getPeerCredentials(intValue());
  }
  
  long sendFile(DefaultFileRegion src, long baseOffset, long offset, long length) throws IOException {
    src.open();
    long res = sendFile(intValue(), src, baseOffset, offset, length);
    if (res >= 0L)
      return res; 
    return Errors.ioResult("sendfile", (int)res, SENDFILE_CONNECTION_RESET_EXCEPTION, SENDFILE_CLOSED_CHANNEL_EXCEPTION);
  }
  
  public static BsdSocket newSocketStream() {
    return new BsdSocket(newSocketStream0());
  }
  
  public static BsdSocket newSocketDgram() {
    return new BsdSocket(newSocketDgram0());
  }
  
  public static BsdSocket newSocketDomain() {
    return new BsdSocket(newSocketDomain0());
  }
  
  private static native long sendFile(int paramInt, DefaultFileRegion paramDefaultFileRegion, long paramLong1, long paramLong2, long paramLong3) throws IOException;
  
  private static native String[] getAcceptFilter(int paramInt) throws IOException;
  
  private static native int getTcpNoPush(int paramInt) throws IOException;
  
  private static native int getSndLowAt(int paramInt) throws IOException;
  
  private static native PeerCredentials getPeerCredentials(int paramInt) throws IOException;
  
  private static native void setAcceptFilter(int paramInt, String paramString1, String paramString2) throws IOException;
  
  private static native void setTcpNoPush(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setSndLowAt(int paramInt1, int paramInt2) throws IOException;
}

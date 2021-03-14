package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.channel.DefaultFileRegion;
import com.github.steveice10.netty.channel.unix.Errors;
import com.github.steveice10.netty.channel.unix.NativeInetAddress;
import com.github.steveice10.netty.channel.unix.PeerCredentials;
import com.github.steveice10.netty.channel.unix.Socket;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.ClosedChannelException;

final class LinuxSocket extends Socket {
  private static final long MAX_UINT32_T = 4294967295L;
  
  private static final Errors.NativeIoException SENDFILE_CONNECTION_RESET_EXCEPTION = Errors.newConnectionResetException("syscall:sendfile(...)", Errors.ERRNO_EPIPE_NEGATIVE);
  
  private static final ClosedChannelException SENDFILE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Native.class, "sendfile(...)");
  
  public LinuxSocket(int fd) {
    super(fd);
  }
  
  void setTcpDeferAccept(int deferAccept) throws IOException {
    setTcpDeferAccept(intValue(), deferAccept);
  }
  
  void setTcpQuickAck(boolean quickAck) throws IOException {
    setTcpQuickAck(intValue(), quickAck ? 1 : 0);
  }
  
  void setTcpCork(boolean tcpCork) throws IOException {
    setTcpCork(intValue(), tcpCork ? 1 : 0);
  }
  
  void setTcpNotSentLowAt(long tcpNotSentLowAt) throws IOException {
    if (tcpNotSentLowAt < 0L || tcpNotSentLowAt > 4294967295L)
      throw new IllegalArgumentException("tcpNotSentLowAt must be a uint32_t"); 
    setTcpNotSentLowAt(intValue(), (int)tcpNotSentLowAt);
  }
  
  void setTcpFastOpen(int tcpFastopenBacklog) throws IOException {
    setTcpFastOpen(intValue(), tcpFastopenBacklog);
  }
  
  void setTcpFastOpenConnect(boolean tcpFastOpenConnect) throws IOException {
    setTcpFastOpenConnect(intValue(), tcpFastOpenConnect ? 1 : 0);
  }
  
  boolean isTcpFastOpenConnect() throws IOException {
    return (isTcpFastOpenConnect(intValue()) != 0);
  }
  
  void setTcpKeepIdle(int seconds) throws IOException {
    setTcpKeepIdle(intValue(), seconds);
  }
  
  void setTcpKeepIntvl(int seconds) throws IOException {
    setTcpKeepIntvl(intValue(), seconds);
  }
  
  void setTcpKeepCnt(int probes) throws IOException {
    setTcpKeepCnt(intValue(), probes);
  }
  
  void setTcpUserTimeout(int milliseconds) throws IOException {
    setTcpUserTimeout(intValue(), milliseconds);
  }
  
  void setIpFreeBind(boolean enabled) throws IOException {
    setIpFreeBind(intValue(), enabled ? 1 : 0);
  }
  
  void setIpTransparent(boolean enabled) throws IOException {
    setIpTransparent(intValue(), enabled ? 1 : 0);
  }
  
  void setIpRecvOrigDestAddr(boolean enabled) throws IOException {
    setIpRecvOrigDestAddr(intValue(), enabled ? 1 : 0);
  }
  
  void getTcpInfo(EpollTcpInfo info) throws IOException {
    getTcpInfo(intValue(), info.info);
  }
  
  void setTcpMd5Sig(InetAddress address, byte[] key) throws IOException {
    NativeInetAddress a = NativeInetAddress.newInstance(address);
    setTcpMd5Sig(intValue(), a.address(), a.scopeId(), key);
  }
  
  boolean isTcpCork() throws IOException {
    return (isTcpCork(intValue()) != 0);
  }
  
  int getTcpDeferAccept() throws IOException {
    return getTcpDeferAccept(intValue());
  }
  
  boolean isTcpQuickAck() throws IOException {
    return (isTcpQuickAck(intValue()) != 0);
  }
  
  long getTcpNotSentLowAt() throws IOException {
    return getTcpNotSentLowAt(intValue()) & 0xFFFFFFFFL;
  }
  
  int getTcpKeepIdle() throws IOException {
    return getTcpKeepIdle(intValue());
  }
  
  int getTcpKeepIntvl() throws IOException {
    return getTcpKeepIntvl(intValue());
  }
  
  int getTcpKeepCnt() throws IOException {
    return getTcpKeepCnt(intValue());
  }
  
  int getTcpUserTimeout() throws IOException {
    return getTcpUserTimeout(intValue());
  }
  
  boolean isIpFreeBind() throws IOException {
    return (isIpFreeBind(intValue()) != 0);
  }
  
  boolean isIpTransparent() throws IOException {
    return (isIpTransparent(intValue()) != 0);
  }
  
  boolean isIpRecvOrigDestAddr() throws IOException {
    return (isIpRecvOrigDestAddr(intValue()) != 0);
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
  
  public static LinuxSocket newSocketStream() {
    return new LinuxSocket(newSocketStream0());
  }
  
  public static LinuxSocket newSocketDgram() {
    return new LinuxSocket(newSocketDgram0());
  }
  
  public static LinuxSocket newSocketDomain() {
    return new LinuxSocket(newSocketDomain0());
  }
  
  private static native long sendFile(int paramInt, DefaultFileRegion paramDefaultFileRegion, long paramLong1, long paramLong2, long paramLong3) throws IOException;
  
  private static native int getTcpDeferAccept(int paramInt) throws IOException;
  
  private static native int isTcpQuickAck(int paramInt) throws IOException;
  
  private static native int isTcpCork(int paramInt) throws IOException;
  
  private static native int getTcpNotSentLowAt(int paramInt) throws IOException;
  
  private static native int getTcpKeepIdle(int paramInt) throws IOException;
  
  private static native int getTcpKeepIntvl(int paramInt) throws IOException;
  
  private static native int getTcpKeepCnt(int paramInt) throws IOException;
  
  private static native int getTcpUserTimeout(int paramInt) throws IOException;
  
  private static native int isIpFreeBind(int paramInt) throws IOException;
  
  private static native int isIpTransparent(int paramInt) throws IOException;
  
  private static native int isIpRecvOrigDestAddr(int paramInt) throws IOException;
  
  private static native void getTcpInfo(int paramInt, long[] paramArrayOflong) throws IOException;
  
  private static native PeerCredentials getPeerCredentials(int paramInt) throws IOException;
  
  private static native int isTcpFastOpenConnect(int paramInt) throws IOException;
  
  private static native void setTcpDeferAccept(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTcpQuickAck(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTcpCork(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTcpNotSentLowAt(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTcpFastOpen(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTcpFastOpenConnect(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTcpKeepIdle(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTcpKeepIntvl(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTcpKeepCnt(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTcpUserTimeout(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setIpFreeBind(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setIpTransparent(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setIpRecvOrigDestAddr(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTcpMd5Sig(int paramInt1, byte[] paramArrayOfbyte1, int paramInt2, byte[] paramArrayOfbyte2) throws IOException;
}

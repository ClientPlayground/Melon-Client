package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.channel.unix.Errors;
import com.github.steveice10.netty.channel.unix.FileDescriptor;
import com.github.steveice10.netty.channel.unix.Socket;
import com.github.steveice10.netty.util.internal.NativeLibraryLoader;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Locale;

public final class Native {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(Native.class);
  
  static {
    try {
      offsetofEpollData();
    } catch (UnsatisfiedLinkError ignore) {
      loadNativeLibrary();
    } 
    Socket.initialize();
  }
  
  public static final int EPOLLIN = NativeStaticallyReferencedJniMethods.epollin();
  
  public static final int EPOLLOUT = NativeStaticallyReferencedJniMethods.epollout();
  
  public static final int EPOLLRDHUP = NativeStaticallyReferencedJniMethods.epollrdhup();
  
  public static final int EPOLLET = NativeStaticallyReferencedJniMethods.epollet();
  
  public static final int EPOLLERR = NativeStaticallyReferencedJniMethods.epollerr();
  
  public static final boolean IS_SUPPORTING_SENDMMSG = NativeStaticallyReferencedJniMethods.isSupportingSendmmsg();
  
  public static final boolean IS_SUPPORTING_TCP_FASTOPEN = NativeStaticallyReferencedJniMethods.isSupportingTcpFastopen();
  
  public static final int TCP_MD5SIG_MAXKEYLEN = NativeStaticallyReferencedJniMethods.tcpMd5SigMaxKeyLen();
  
  public static final String KERNEL_VERSION = NativeStaticallyReferencedJniMethods.kernelVersion();
  
  private static final Errors.NativeIoException SENDMMSG_CONNECTION_RESET_EXCEPTION;
  
  private static final Errors.NativeIoException SPLICE_CONNECTION_RESET_EXCEPTION;
  
  private static final ClosedChannelException SENDMMSG_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Native.class, "sendmmsg(...)");
  
  private static final ClosedChannelException SPLICE_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Native.class, "splice(...)");
  
  static {
    SENDMMSG_CONNECTION_RESET_EXCEPTION = Errors.newConnectionResetException("syscall:sendmmsg(...)", Errors.ERRNO_EPIPE_NEGATIVE);
    SPLICE_CONNECTION_RESET_EXCEPTION = Errors.newConnectionResetException("syscall:splice(...)", Errors.ERRNO_EPIPE_NEGATIVE);
  }
  
  public static FileDescriptor newEventFd() {
    return new FileDescriptor(eventFd());
  }
  
  public static FileDescriptor newTimerFd() {
    return new FileDescriptor(timerFd());
  }
  
  public static FileDescriptor newEpollCreate() {
    return new FileDescriptor(epollCreate());
  }
  
  public static int epollWait(FileDescriptor epollFd, EpollEventArray events, FileDescriptor timerFd, int timeoutSec, int timeoutNs) throws IOException {
    int ready = epollWait0(epollFd.intValue(), events.memoryAddress(), events.length(), timerFd.intValue(), timeoutSec, timeoutNs);
    if (ready < 0)
      throw Errors.newIOException("epoll_wait", ready); 
    return ready;
  }
  
  public static void epollCtlAdd(int efd, int fd, int flags) throws IOException {
    int res = epollCtlAdd0(efd, fd, flags);
    if (res < 0)
      throw Errors.newIOException("epoll_ctl", res); 
  }
  
  public static void epollCtlMod(int efd, int fd, int flags) throws IOException {
    int res = epollCtlMod0(efd, fd, flags);
    if (res < 0)
      throw Errors.newIOException("epoll_ctl", res); 
  }
  
  public static void epollCtlDel(int efd, int fd) throws IOException {
    int res = epollCtlDel0(efd, fd);
    if (res < 0)
      throw Errors.newIOException("epoll_ctl", res); 
  }
  
  public static int splice(int fd, long offIn, int fdOut, long offOut, long len) throws IOException {
    int res = splice0(fd, offIn, fdOut, offOut, len);
    if (res >= 0)
      return res; 
    return Errors.ioResult("splice", res, SPLICE_CONNECTION_RESET_EXCEPTION, SPLICE_CLOSED_CHANNEL_EXCEPTION);
  }
  
  public static int sendmmsg(int fd, NativeDatagramPacketArray.NativeDatagramPacket[] msgs, int offset, int len) throws IOException {
    int res = sendmmsg0(fd, msgs, offset, len);
    if (res >= 0)
      return res; 
    return Errors.ioResult("sendmmsg", res, SENDMMSG_CONNECTION_RESET_EXCEPTION, SENDMMSG_CLOSED_CHANNEL_EXCEPTION);
  }
  
  private static void loadNativeLibrary() {
    String name = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
    if (!name.startsWith("linux"))
      throw new IllegalStateException("Only supported on Linux"); 
    String staticLibName = "netty_transport_native_epoll";
    String sharedLibName = staticLibName + '_' + PlatformDependent.normalizedArch();
    ClassLoader cl = PlatformDependent.getClassLoader(Native.class);
    try {
      NativeLibraryLoader.load(sharedLibName, cl);
    } catch (UnsatisfiedLinkError e1) {
      try {
        NativeLibraryLoader.load(staticLibName, cl);
        logger.debug("Failed to load {}", sharedLibName, e1);
      } catch (UnsatisfiedLinkError e2) {
        ThrowableUtil.addSuppressed(e1, e2);
        throw e1;
      } 
    } 
  }
  
  private static native int eventFd();
  
  private static native int timerFd();
  
  public static native void eventFdWrite(int paramInt, long paramLong);
  
  public static native void eventFdRead(int paramInt);
  
  static native void timerFdRead(int paramInt);
  
  private static native int epollCreate();
  
  private static native int epollWait0(int paramInt1, long paramLong, int paramInt2, int paramInt3, int paramInt4, int paramInt5);
  
  private static native int epollCtlAdd0(int paramInt1, int paramInt2, int paramInt3);
  
  private static native int epollCtlMod0(int paramInt1, int paramInt2, int paramInt3);
  
  private static native int epollCtlDel0(int paramInt1, int paramInt2);
  
  private static native int splice0(int paramInt1, long paramLong1, int paramInt2, long paramLong2, long paramLong3);
  
  private static native int sendmmsg0(int paramInt1, NativeDatagramPacketArray.NativeDatagramPacket[] paramArrayOfNativeDatagramPacket, int paramInt2, int paramInt3);
  
  public static native int sizeofEpollEvent();
  
  public static native int offsetofEpollData();
}

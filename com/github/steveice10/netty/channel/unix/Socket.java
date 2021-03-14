package com.github.steveice10.netty.channel.unix;

import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.NetUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Socket extends FileDescriptor {
  private static final ClosedChannelException SHUTDOWN_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Socket.class, "shutdown(..)");
  
  private static final ClosedChannelException SEND_TO_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Socket.class, "sendTo(..)");
  
  private static final ClosedChannelException SEND_TO_ADDRESS_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Socket.class, "sendToAddress(..)");
  
  private static final ClosedChannelException SEND_TO_ADDRESSES_CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), Socket.class, "sendToAddresses(..)");
  
  private static final Errors.NativeIoException SEND_TO_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(
      Errors.newConnectionResetException("syscall:sendto", Errors.ERRNO_EPIPE_NEGATIVE), Socket.class, "sendTo(..)");
  
  private static final Errors.NativeIoException SEND_TO_ADDRESS_CONNECTION_RESET_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(Errors.newConnectionResetException("syscall:sendto", Errors.ERRNO_EPIPE_NEGATIVE), Socket.class, "sendToAddress");
  
  private static final Errors.NativeIoException CONNECTION_RESET_EXCEPTION_SENDMSG = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(
      Errors.newConnectionResetException("syscall:sendmsg", Errors.ERRNO_EPIPE_NEGATIVE), Socket.class, "sendToAddresses(..)");
  
  private static final Errors.NativeIoException CONNECTION_RESET_SHUTDOWN_EXCEPTION = (Errors.NativeIoException)ThrowableUtil.unknownStackTrace(Errors.newConnectionResetException("syscall:shutdown", Errors.ERRNO_ECONNRESET_NEGATIVE), Socket.class, "shutdown");
  
  private static final Errors.NativeConnectException FINISH_CONNECT_REFUSED_EXCEPTION = (Errors.NativeConnectException)ThrowableUtil.unknownStackTrace(new Errors.NativeConnectException("syscall:getsockopt", Errors.ERROR_ECONNREFUSED_NEGATIVE), Socket.class, "finishConnect(..)");
  
  private static final Errors.NativeConnectException CONNECT_REFUSED_EXCEPTION = (Errors.NativeConnectException)ThrowableUtil.unknownStackTrace(new Errors.NativeConnectException("syscall:connect", Errors.ERROR_ECONNREFUSED_NEGATIVE), Socket.class, "connect(..)");
  
  public static final int UDS_SUN_PATH_SIZE = LimitsStaticallyReferencedJniMethods.udsSunPathSize();
  
  public Socket(int fd) {
    super(fd);
  }
  
  public final void shutdown() throws IOException {
    shutdown(true, true);
  }
  
  public final void shutdown(boolean read, boolean write) throws IOException {
    int oldState, newState;
    do {
      oldState = this.state;
      if (isClosed(oldState))
        throw new ClosedChannelException(); 
      newState = oldState;
      if (read && !isInputShutdown(newState))
        newState = inputShutdown(newState); 
      if (write && !isOutputShutdown(newState))
        newState = outputShutdown(newState); 
      if (newState == oldState)
        return; 
    } while (!casState(oldState, newState));
    int res = shutdown(this.fd, read, write);
    if (res < 0)
      Errors.ioResult("shutdown", res, CONNECTION_RESET_SHUTDOWN_EXCEPTION, SHUTDOWN_CLOSED_CHANNEL_EXCEPTION); 
  }
  
  public final boolean isShutdown() {
    int state = this.state;
    return (isInputShutdown(state) && isOutputShutdown(state));
  }
  
  public final boolean isInputShutdown() {
    return isInputShutdown(this.state);
  }
  
  public final boolean isOutputShutdown() {
    return isOutputShutdown(this.state);
  }
  
  public final int sendTo(ByteBuffer buf, int pos, int limit, InetAddress addr, int port) throws IOException {
    byte[] address;
    int scopeId;
    if (addr instanceof Inet6Address) {
      address = addr.getAddress();
      scopeId = ((Inet6Address)addr).getScopeId();
    } else {
      scopeId = 0;
      address = NativeInetAddress.ipv4MappedIpv6Address(addr.getAddress());
    } 
    int res = sendTo(this.fd, buf, pos, limit, address, scopeId, port);
    if (res >= 0)
      return res; 
    if (res == Errors.ERROR_ECONNREFUSED_NEGATIVE)
      throw new PortUnreachableException("sendTo failed"); 
    return Errors.ioResult("sendTo", res, SEND_TO_CONNECTION_RESET_EXCEPTION, SEND_TO_CLOSED_CHANNEL_EXCEPTION);
  }
  
  public final int sendToAddress(long memoryAddress, int pos, int limit, InetAddress addr, int port) throws IOException {
    byte[] address;
    int scopeId;
    if (addr instanceof Inet6Address) {
      address = addr.getAddress();
      scopeId = ((Inet6Address)addr).getScopeId();
    } else {
      scopeId = 0;
      address = NativeInetAddress.ipv4MappedIpv6Address(addr.getAddress());
    } 
    int res = sendToAddress(this.fd, memoryAddress, pos, limit, address, scopeId, port);
    if (res >= 0)
      return res; 
    if (res == Errors.ERROR_ECONNREFUSED_NEGATIVE)
      throw new PortUnreachableException("sendToAddress failed"); 
    return Errors.ioResult("sendToAddress", res, SEND_TO_ADDRESS_CONNECTION_RESET_EXCEPTION, SEND_TO_ADDRESS_CLOSED_CHANNEL_EXCEPTION);
  }
  
  public final int sendToAddresses(long memoryAddress, int length, InetAddress addr, int port) throws IOException {
    byte[] address;
    int scopeId;
    if (addr instanceof Inet6Address) {
      address = addr.getAddress();
      scopeId = ((Inet6Address)addr).getScopeId();
    } else {
      scopeId = 0;
      address = NativeInetAddress.ipv4MappedIpv6Address(addr.getAddress());
    } 
    int res = sendToAddresses(this.fd, memoryAddress, length, address, scopeId, port);
    if (res >= 0)
      return res; 
    if (res == Errors.ERROR_ECONNREFUSED_NEGATIVE)
      throw new PortUnreachableException("sendToAddresses failed"); 
    return Errors.ioResult("sendToAddresses", res, CONNECTION_RESET_EXCEPTION_SENDMSG, SEND_TO_ADDRESSES_CLOSED_CHANNEL_EXCEPTION);
  }
  
  public final DatagramSocketAddress recvFrom(ByteBuffer buf, int pos, int limit) throws IOException {
    return recvFrom(this.fd, buf, pos, limit);
  }
  
  public final DatagramSocketAddress recvFromAddress(long memoryAddress, int pos, int limit) throws IOException {
    return recvFromAddress(this.fd, memoryAddress, pos, limit);
  }
  
  public final int recvFd() throws IOException {
    int res = recvFd(this.fd);
    if (res > 0)
      return res; 
    if (res == 0)
      return -1; 
    if (res == Errors.ERRNO_EAGAIN_NEGATIVE || res == Errors.ERRNO_EWOULDBLOCK_NEGATIVE)
      return 0; 
    throw Errors.newIOException("recvFd", res);
  }
  
  public final int sendFd(int fdToSend) throws IOException {
    int res = sendFd(this.fd, fdToSend);
    if (res >= 0)
      return res; 
    if (res == Errors.ERRNO_EAGAIN_NEGATIVE || res == Errors.ERRNO_EWOULDBLOCK_NEGATIVE)
      return -1; 
    throw Errors.newIOException("sendFd", res);
  }
  
  public final boolean connect(SocketAddress socketAddress) throws IOException {
    int res;
    if (socketAddress instanceof InetSocketAddress) {
      InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
      NativeInetAddress address = NativeInetAddress.newInstance(inetSocketAddress.getAddress());
      res = connect(this.fd, address.address, address.scopeId, inetSocketAddress.getPort());
    } else if (socketAddress instanceof DomainSocketAddress) {
      DomainSocketAddress unixDomainSocketAddress = (DomainSocketAddress)socketAddress;
      res = connectDomainSocket(this.fd, unixDomainSocketAddress.path().getBytes(CharsetUtil.UTF_8));
    } else {
      throw new Error("Unexpected SocketAddress implementation " + socketAddress);
    } 
    if (res < 0) {
      if (res == Errors.ERRNO_EINPROGRESS_NEGATIVE)
        return false; 
      Errors.throwConnectException("connect", CONNECT_REFUSED_EXCEPTION, res);
    } 
    return true;
  }
  
  public final boolean finishConnect() throws IOException {
    int res = finishConnect(this.fd);
    if (res < 0) {
      if (res == Errors.ERRNO_EINPROGRESS_NEGATIVE)
        return false; 
      Errors.throwConnectException("finishConnect", FINISH_CONNECT_REFUSED_EXCEPTION, res);
    } 
    return true;
  }
  
  public final void disconnect() throws IOException {
    int res = disconnect(this.fd);
    if (res < 0)
      Errors.throwConnectException("disconnect", FINISH_CONNECT_REFUSED_EXCEPTION, res); 
  }
  
  public final void bind(SocketAddress socketAddress) throws IOException {
    if (socketAddress instanceof InetSocketAddress) {
      InetSocketAddress addr = (InetSocketAddress)socketAddress;
      NativeInetAddress address = NativeInetAddress.newInstance(addr.getAddress());
      int res = bind(this.fd, address.address, address.scopeId, addr.getPort());
      if (res < 0)
        throw Errors.newIOException("bind", res); 
    } else if (socketAddress instanceof DomainSocketAddress) {
      DomainSocketAddress addr = (DomainSocketAddress)socketAddress;
      int res = bindDomainSocket(this.fd, addr.path().getBytes(CharsetUtil.UTF_8));
      if (res < 0)
        throw Errors.newIOException("bind", res); 
    } else {
      throw new Error("Unexpected SocketAddress implementation " + socketAddress);
    } 
  }
  
  public final void listen(int backlog) throws IOException {
    int res = listen(this.fd, backlog);
    if (res < 0)
      throw Errors.newIOException("listen", res); 
  }
  
  public final int accept(byte[] addr) throws IOException {
    int res = accept(this.fd, addr);
    if (res >= 0)
      return res; 
    if (res == Errors.ERRNO_EAGAIN_NEGATIVE || res == Errors.ERRNO_EWOULDBLOCK_NEGATIVE)
      return -1; 
    throw Errors.newIOException("accept", res);
  }
  
  public final InetSocketAddress remoteAddress() {
    byte[] addr = remoteAddress(this.fd);
    return (addr == null) ? null : NativeInetAddress.address(addr, 0, addr.length);
  }
  
  public final InetSocketAddress localAddress() {
    byte[] addr = localAddress(this.fd);
    return (addr == null) ? null : NativeInetAddress.address(addr, 0, addr.length);
  }
  
  public final int getReceiveBufferSize() throws IOException {
    return getReceiveBufferSize(this.fd);
  }
  
  public final int getSendBufferSize() throws IOException {
    return getSendBufferSize(this.fd);
  }
  
  public final boolean isKeepAlive() throws IOException {
    return (isKeepAlive(this.fd) != 0);
  }
  
  public final boolean isTcpNoDelay() throws IOException {
    return (isTcpNoDelay(this.fd) != 0);
  }
  
  public final boolean isReuseAddress() throws IOException {
    return (isReuseAddress(this.fd) != 0);
  }
  
  public final boolean isReusePort() throws IOException {
    return (isReusePort(this.fd) != 0);
  }
  
  public final boolean isBroadcast() throws IOException {
    return (isBroadcast(this.fd) != 0);
  }
  
  public final int getSoLinger() throws IOException {
    return getSoLinger(this.fd);
  }
  
  public final int getSoError() throws IOException {
    return getSoError(this.fd);
  }
  
  public final int getTrafficClass() throws IOException {
    return getTrafficClass(this.fd);
  }
  
  public final void setKeepAlive(boolean keepAlive) throws IOException {
    setKeepAlive(this.fd, keepAlive ? 1 : 0);
  }
  
  public final void setReceiveBufferSize(int receiveBufferSize) throws IOException {
    setReceiveBufferSize(this.fd, receiveBufferSize);
  }
  
  public final void setSendBufferSize(int sendBufferSize) throws IOException {
    setSendBufferSize(this.fd, sendBufferSize);
  }
  
  public final void setTcpNoDelay(boolean tcpNoDelay) throws IOException {
    setTcpNoDelay(this.fd, tcpNoDelay ? 1 : 0);
  }
  
  public final void setSoLinger(int soLinger) throws IOException {
    setSoLinger(this.fd, soLinger);
  }
  
  public final void setReuseAddress(boolean reuseAddress) throws IOException {
    setReuseAddress(this.fd, reuseAddress ? 1 : 0);
  }
  
  public final void setReusePort(boolean reusePort) throws IOException {
    setReusePort(this.fd, reusePort ? 1 : 0);
  }
  
  public final void setBroadcast(boolean broadcast) throws IOException {
    setBroadcast(this.fd, broadcast ? 1 : 0);
  }
  
  public final void setTrafficClass(int trafficClass) throws IOException {
    setTrafficClass(this.fd, trafficClass);
  }
  
  public String toString() {
    return "Socket{fd=" + this.fd + '}';
  }
  
  private static final AtomicBoolean INITIALIZED = new AtomicBoolean();
  
  public static Socket newSocketStream() {
    return new Socket(newSocketStream0());
  }
  
  public static Socket newSocketDgram() {
    return new Socket(newSocketDgram0());
  }
  
  public static Socket newSocketDomain() {
    return new Socket(newSocketDomain0());
  }
  
  public static void initialize() {
    if (INITIALIZED.compareAndSet(false, true))
      initialize(NetUtil.isIpV4StackPreferred()); 
  }
  
  protected static int newSocketStream0() {
    int res = newSocketStreamFd();
    if (res < 0)
      throw new ChannelException(Errors.newIOException("newSocketStream", res)); 
    return res;
  }
  
  protected static int newSocketDgram0() {
    int res = newSocketDgramFd();
    if (res < 0)
      throw new ChannelException(Errors.newIOException("newSocketDgram", res)); 
    return res;
  }
  
  protected static int newSocketDomain0() {
    int res = newSocketDomainFd();
    if (res < 0)
      throw new ChannelException(Errors.newIOException("newSocketDomain", res)); 
    return res;
  }
  
  private static native int shutdown(int paramInt, boolean paramBoolean1, boolean paramBoolean2);
  
  private static native int connect(int paramInt1, byte[] paramArrayOfbyte, int paramInt2, int paramInt3);
  
  private static native int connectDomainSocket(int paramInt, byte[] paramArrayOfbyte);
  
  private static native int finishConnect(int paramInt);
  
  private static native int disconnect(int paramInt);
  
  private static native int bind(int paramInt1, byte[] paramArrayOfbyte, int paramInt2, int paramInt3);
  
  private static native int bindDomainSocket(int paramInt, byte[] paramArrayOfbyte);
  
  private static native int listen(int paramInt1, int paramInt2);
  
  private static native int accept(int paramInt, byte[] paramArrayOfbyte);
  
  private static native byte[] remoteAddress(int paramInt);
  
  private static native byte[] localAddress(int paramInt);
  
  private static native int sendTo(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3, byte[] paramArrayOfbyte, int paramInt4, int paramInt5);
  
  private static native int sendToAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3, byte[] paramArrayOfbyte, int paramInt4, int paramInt5);
  
  private static native int sendToAddresses(int paramInt1, long paramLong, int paramInt2, byte[] paramArrayOfbyte, int paramInt3, int paramInt4);
  
  private static native DatagramSocketAddress recvFrom(int paramInt1, ByteBuffer paramByteBuffer, int paramInt2, int paramInt3) throws IOException;
  
  private static native DatagramSocketAddress recvFromAddress(int paramInt1, long paramLong, int paramInt2, int paramInt3) throws IOException;
  
  private static native int recvFd(int paramInt);
  
  private static native int sendFd(int paramInt1, int paramInt2);
  
  private static native int newSocketStreamFd();
  
  private static native int newSocketDgramFd();
  
  private static native int newSocketDomainFd();
  
  private static native int isReuseAddress(int paramInt) throws IOException;
  
  private static native int isReusePort(int paramInt) throws IOException;
  
  private static native int getReceiveBufferSize(int paramInt) throws IOException;
  
  private static native int getSendBufferSize(int paramInt) throws IOException;
  
  private static native int isKeepAlive(int paramInt) throws IOException;
  
  private static native int isTcpNoDelay(int paramInt) throws IOException;
  
  private static native int isBroadcast(int paramInt) throws IOException;
  
  private static native int getSoLinger(int paramInt) throws IOException;
  
  private static native int getSoError(int paramInt) throws IOException;
  
  private static native int getTrafficClass(int paramInt) throws IOException;
  
  private static native void setReuseAddress(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setReusePort(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setKeepAlive(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setReceiveBufferSize(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setSendBufferSize(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTcpNoDelay(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setSoLinger(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setBroadcast(int paramInt1, int paramInt2) throws IOException;
  
  private static native void setTrafficClass(int paramInt1, int paramInt2) throws IOException;
  
  private static native void initialize(boolean paramBoolean);
}

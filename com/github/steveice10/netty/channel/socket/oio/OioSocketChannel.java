package com.github.steveice10.netty.channel.socket.oio;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.ConnectTimeoutException;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.oio.OioByteStreamChannel;
import com.github.steveice10.netty.channel.socket.ServerSocketChannel;
import com.github.steveice10.netty.channel.socket.SocketChannel;
import com.github.steveice10.netty.channel.socket.SocketChannelConfig;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.SocketUtils;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class OioSocketChannel extends OioByteStreamChannel implements SocketChannel {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioSocketChannel.class);
  
  private final Socket socket;
  
  private final OioSocketChannelConfig config;
  
  public OioSocketChannel() {
    this(new Socket());
  }
  
  public OioSocketChannel(Socket socket) {
    this((Channel)null, socket);
  }
  
  public OioSocketChannel(Channel parent, Socket socket) {
    super(parent);
    this.socket = socket;
    this.config = new DefaultOioSocketChannelConfig(this, socket);
    boolean success = false;
    try {
      if (socket.isConnected())
        activate(socket.getInputStream(), socket.getOutputStream()); 
      socket.setSoTimeout(1000);
      success = true;
    } catch (Exception e) {
      throw new ChannelException("failed to initialize a socket", e);
    } finally {
      if (!success)
        try {
          socket.close();
        } catch (IOException e) {
          logger.warn("Failed to close a socket.", e);
        }  
    } 
  }
  
  public ServerSocketChannel parent() {
    return (ServerSocketChannel)super.parent();
  }
  
  public OioSocketChannelConfig config() {
    return this.config;
  }
  
  public boolean isOpen() {
    return !this.socket.isClosed();
  }
  
  public boolean isActive() {
    return (!this.socket.isClosed() && this.socket.isConnected());
  }
  
  public boolean isOutputShutdown() {
    return (this.socket.isOutputShutdown() || !isActive());
  }
  
  public boolean isInputShutdown() {
    return (this.socket.isInputShutdown() || !isActive());
  }
  
  public boolean isShutdown() {
    return ((this.socket.isInputShutdown() && this.socket.isOutputShutdown()) || !isActive());
  }
  
  protected final void doShutdownOutput() throws Exception {
    shutdownOutput0();
  }
  
  public ChannelFuture shutdownOutput() {
    return shutdownOutput(newPromise());
  }
  
  public ChannelFuture shutdownInput() {
    return shutdownInput(newPromise());
  }
  
  public ChannelFuture shutdown() {
    return shutdown(newPromise());
  }
  
  protected int doReadBytes(ByteBuf buf) throws Exception {
    if (this.socket.isClosed())
      return -1; 
    try {
      return super.doReadBytes(buf);
    } catch (SocketTimeoutException ignored) {
      return 0;
    } 
  }
  
  public ChannelFuture shutdownOutput(final ChannelPromise promise) {
    EventLoop loop = eventLoop();
    if (loop.inEventLoop()) {
      shutdownOutput0(promise);
    } else {
      loop.execute(new Runnable() {
            public void run() {
              OioSocketChannel.this.shutdownOutput0(promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  private void shutdownOutput0(ChannelPromise promise) {
    try {
      shutdownOutput0();
      promise.setSuccess();
    } catch (Throwable t) {
      promise.setFailure(t);
    } 
  }
  
  private void shutdownOutput0() throws IOException {
    this.socket.shutdownOutput();
  }
  
  public ChannelFuture shutdownInput(final ChannelPromise promise) {
    EventLoop loop = eventLoop();
    if (loop.inEventLoop()) {
      shutdownInput0(promise);
    } else {
      loop.execute(new Runnable() {
            public void run() {
              OioSocketChannel.this.shutdownInput0(promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  private void shutdownInput0(ChannelPromise promise) {
    try {
      this.socket.shutdownInput();
      promise.setSuccess();
    } catch (Throwable t) {
      promise.setFailure(t);
    } 
  }
  
  public ChannelFuture shutdown(final ChannelPromise promise) {
    ChannelFuture shutdownOutputFuture = shutdownOutput();
    if (shutdownOutputFuture.isDone()) {
      shutdownOutputDone(shutdownOutputFuture, promise);
    } else {
      shutdownOutputFuture.addListener((GenericFutureListener)new ChannelFutureListener() {
            public void operationComplete(ChannelFuture shutdownOutputFuture) throws Exception {
              OioSocketChannel.this.shutdownOutputDone(shutdownOutputFuture, promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  private void shutdownOutputDone(final ChannelFuture shutdownOutputFuture, final ChannelPromise promise) {
    ChannelFuture shutdownInputFuture = shutdownInput();
    if (shutdownInputFuture.isDone()) {
      shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
    } else {
      shutdownInputFuture.addListener((GenericFutureListener)new ChannelFutureListener() {
            public void operationComplete(ChannelFuture shutdownInputFuture) throws Exception {
              OioSocketChannel.shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
            }
          });
    } 
  }
  
  private static void shutdownDone(ChannelFuture shutdownOutputFuture, ChannelFuture shutdownInputFuture, ChannelPromise promise) {
    Throwable shutdownOutputCause = shutdownOutputFuture.cause();
    Throwable shutdownInputCause = shutdownInputFuture.cause();
    if (shutdownOutputCause != null) {
      if (shutdownInputCause != null)
        logger.debug("Exception suppressed because a previous exception occurred.", shutdownInputCause); 
      promise.setFailure(shutdownOutputCause);
    } else if (shutdownInputCause != null) {
      promise.setFailure(shutdownInputCause);
    } else {
      promise.setSuccess();
    } 
  }
  
  public InetSocketAddress localAddress() {
    return (InetSocketAddress)super.localAddress();
  }
  
  public InetSocketAddress remoteAddress() {
    return (InetSocketAddress)super.remoteAddress();
  }
  
  protected SocketAddress localAddress0() {
    return this.socket.getLocalSocketAddress();
  }
  
  protected SocketAddress remoteAddress0() {
    return this.socket.getRemoteSocketAddress();
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    SocketUtils.bind(this.socket, localAddress);
  }
  
  protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    if (localAddress != null)
      SocketUtils.bind(this.socket, localAddress); 
    boolean success = false;
    try {
      SocketUtils.connect(this.socket, remoteAddress, config().getConnectTimeoutMillis());
      activate(this.socket.getInputStream(), this.socket.getOutputStream());
      success = true;
    } catch (SocketTimeoutException e) {
      ConnectTimeoutException cause = new ConnectTimeoutException("connection timed out: " + remoteAddress);
      cause.setStackTrace(e.getStackTrace());
      throw cause;
    } finally {
      if (!success)
        doClose(); 
    } 
  }
  
  protected void doDisconnect() throws Exception {
    doClose();
  }
  
  protected void doClose() throws Exception {
    this.socket.close();
  }
  
  protected boolean checkInputShutdown() {
    if (isInputShutdown()) {
      try {
        Thread.sleep(config().getSoTimeout());
      } catch (Throwable throwable) {}
      return true;
    } 
    return false;
  }
  
  @Deprecated
  protected void setReadPending(boolean readPending) {
    super.setReadPending(readPending);
  }
  
  final void clearReadPending0() {
    clearReadPending();
  }
}

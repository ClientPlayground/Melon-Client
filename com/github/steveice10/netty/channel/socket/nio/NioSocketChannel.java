package com.github.steveice10.netty.channel.socket.nio;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.FileRegion;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.nio.AbstractNioByteChannel;
import com.github.steveice10.netty.channel.nio.AbstractNioChannel;
import com.github.steveice10.netty.channel.nio.NioEventLoop;
import com.github.steveice10.netty.channel.socket.DefaultSocketChannelConfig;
import com.github.steveice10.netty.channel.socket.ServerSocketChannel;
import com.github.steveice10.netty.channel.socket.SocketChannel;
import com.github.steveice10.netty.channel.socket.SocketChannelConfig;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.GlobalEventExecutor;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.SocketUtils;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.Executor;

public class NioSocketChannel extends AbstractNioByteChannel implements SocketChannel {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioSocketChannel.class);
  
  private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
  
  private final SocketChannelConfig config;
  
  private static SocketChannel newSocket(SelectorProvider provider) {
    try {
      return provider.openSocketChannel();
    } catch (IOException e) {
      throw new ChannelException("Failed to open a socket.", e);
    } 
  }
  
  public NioSocketChannel() {
    this(DEFAULT_SELECTOR_PROVIDER);
  }
  
  public NioSocketChannel(SelectorProvider provider) {
    this(newSocket(provider));
  }
  
  public NioSocketChannel(SocketChannel socket) {
    this((Channel)null, socket);
  }
  
  public NioSocketChannel(Channel parent, SocketChannel socket) {
    super(parent, socket);
    this.config = (SocketChannelConfig)new NioSocketChannelConfig(this, socket.socket());
  }
  
  public ServerSocketChannel parent() {
    return (ServerSocketChannel)super.parent();
  }
  
  public SocketChannelConfig config() {
    return this.config;
  }
  
  protected SocketChannel javaChannel() {
    return (SocketChannel)super.javaChannel();
  }
  
  public boolean isActive() {
    SocketChannel ch = javaChannel();
    return (ch.isOpen() && ch.isConnected());
  }
  
  public boolean isOutputShutdown() {
    return (javaChannel().socket().isOutputShutdown() || !isActive());
  }
  
  public boolean isInputShutdown() {
    return (javaChannel().socket().isInputShutdown() || !isActive());
  }
  
  public boolean isShutdown() {
    Socket socket = javaChannel().socket();
    return ((socket.isInputShutdown() && socket.isOutputShutdown()) || !isActive());
  }
  
  public InetSocketAddress localAddress() {
    return (InetSocketAddress)super.localAddress();
  }
  
  public InetSocketAddress remoteAddress() {
    return (InetSocketAddress)super.remoteAddress();
  }
  
  protected final void doShutdownOutput() throws Exception {
    if (PlatformDependent.javaVersion() >= 7) {
      javaChannel().shutdownOutput();
    } else {
      javaChannel().socket().shutdownOutput();
    } 
  }
  
  public ChannelFuture shutdownOutput() {
    return shutdownOutput(newPromise());
  }
  
  public ChannelFuture shutdownOutput(final ChannelPromise promise) {
    NioEventLoop nioEventLoop = eventLoop();
    if (nioEventLoop.inEventLoop()) {
      ((AbstractChannel.AbstractUnsafe)unsafe()).shutdownOutput(promise);
    } else {
      nioEventLoop.execute(new Runnable() {
            public void run() {
              ((AbstractChannel.AbstractUnsafe)NioSocketChannel.this.unsafe()).shutdownOutput(promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture shutdownInput() {
    return shutdownInput(newPromise());
  }
  
  protected boolean isInputShutdown0() {
    return isInputShutdown();
  }
  
  public ChannelFuture shutdownInput(final ChannelPromise promise) {
    NioEventLoop nioEventLoop = eventLoop();
    if (nioEventLoop.inEventLoop()) {
      shutdownInput0(promise);
    } else {
      nioEventLoop.execute(new Runnable() {
            public void run() {
              NioSocketChannel.this.shutdownInput0(promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture shutdown() {
    return shutdown(newPromise());
  }
  
  public ChannelFuture shutdown(final ChannelPromise promise) {
    ChannelFuture shutdownOutputFuture = shutdownOutput();
    if (shutdownOutputFuture.isDone()) {
      shutdownOutputDone(shutdownOutputFuture, promise);
    } else {
      shutdownOutputFuture.addListener((GenericFutureListener)new ChannelFutureListener() {
            public void operationComplete(ChannelFuture shutdownOutputFuture) throws Exception {
              NioSocketChannel.this.shutdownOutputDone(shutdownOutputFuture, promise);
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
              NioSocketChannel.shutdownDone(shutdownOutputFuture, shutdownInputFuture, promise);
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
  
  private void shutdownInput0(ChannelPromise promise) {
    try {
      shutdownInput0();
      promise.setSuccess();
    } catch (Throwable t) {
      promise.setFailure(t);
    } 
  }
  
  private void shutdownInput0() throws Exception {
    if (PlatformDependent.javaVersion() >= 7) {
      javaChannel().shutdownInput();
    } else {
      javaChannel().socket().shutdownInput();
    } 
  }
  
  protected SocketAddress localAddress0() {
    return javaChannel().socket().getLocalSocketAddress();
  }
  
  protected SocketAddress remoteAddress0() {
    return javaChannel().socket().getRemoteSocketAddress();
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    doBind0(localAddress);
  }
  
  private void doBind0(SocketAddress localAddress) throws Exception {
    if (PlatformDependent.javaVersion() >= 7) {
      SocketUtils.bind(javaChannel(), localAddress);
    } else {
      SocketUtils.bind(javaChannel().socket(), localAddress);
    } 
  }
  
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    if (localAddress != null)
      doBind0(localAddress); 
    boolean success = false;
    try {
      boolean connected = SocketUtils.connect(javaChannel(), remoteAddress);
      if (!connected)
        selectionKey().interestOps(8); 
      success = true;
      return connected;
    } finally {
      if (!success)
        doClose(); 
    } 
  }
  
  protected void doFinishConnect() throws Exception {
    if (!javaChannel().finishConnect())
      throw new Error(); 
  }
  
  protected void doDisconnect() throws Exception {
    doClose();
  }
  
  protected void doClose() throws Exception {
    super.doClose();
    javaChannel().close();
  }
  
  protected int doReadBytes(ByteBuf byteBuf) throws Exception {
    RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
    allocHandle.attemptedBytesRead(byteBuf.writableBytes());
    return byteBuf.writeBytes(javaChannel(), allocHandle.attemptedBytesRead());
  }
  
  protected int doWriteBytes(ByteBuf buf) throws Exception {
    int expectedWrittenBytes = buf.readableBytes();
    return buf.readBytes(javaChannel(), expectedWrittenBytes);
  }
  
  protected long doWriteFileRegion(FileRegion region) throws Exception {
    long position = region.transferred();
    return region.transferTo(javaChannel(), position);
  }
  
  private void adjustMaxBytesPerGatheringWrite(int attempted, int written, int oldMaxBytesPerGatheringWrite) {
    if (attempted == written) {
      if (attempted << 1 > oldMaxBytesPerGatheringWrite)
        ((NioSocketChannelConfig)this.config).setMaxBytesPerGatheringWrite(attempted << 1); 
    } else if (attempted > 4096 && written < attempted >>> 1) {
      ((NioSocketChannelConfig)this.config).setMaxBytesPerGatheringWrite(attempted >>> 1);
    } 
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    SocketChannel ch = javaChannel();
    int writeSpinCount = config().getWriteSpinCount();
    do {
      ByteBuffer buffer;
      int attemptedBytes, localWrittenBytes;
      long l1, l2;
      if (in.isEmpty()) {
        clearOpWrite();
        return;
      } 
      int maxBytesPerGatheringWrite = ((NioSocketChannelConfig)this.config).getMaxBytesPerGatheringWrite();
      ByteBuffer[] nioBuffers = in.nioBuffers(1024, maxBytesPerGatheringWrite);
      int nioBufferCnt = in.nioBufferCount();
      switch (nioBufferCnt) {
        case 0:
          writeSpinCount -= doWrite0(in);
          break;
        case 1:
          buffer = nioBuffers[0];
          attemptedBytes = buffer.remaining();
          localWrittenBytes = ch.write(buffer);
          if (localWrittenBytes <= 0) {
            incompleteWrite(true);
            return;
          } 
          adjustMaxBytesPerGatheringWrite(attemptedBytes, localWrittenBytes, maxBytesPerGatheringWrite);
          in.removeBytes(localWrittenBytes);
          writeSpinCount--;
          break;
        default:
          l1 = in.nioBufferSize();
          l2 = ch.write(nioBuffers, 0, nioBufferCnt);
          if (l2 <= 0L) {
            incompleteWrite(true);
            return;
          } 
          adjustMaxBytesPerGatheringWrite((int)l1, (int)l2, maxBytesPerGatheringWrite);
          in.removeBytes(l2);
          writeSpinCount--;
          break;
      } 
    } while (writeSpinCount > 0);
    incompleteWrite((writeSpinCount < 0));
  }
  
  protected AbstractNioChannel.AbstractNioUnsafe newUnsafe() {
    return (AbstractNioChannel.AbstractNioUnsafe)new NioSocketChannelUnsafe();
  }
  
  private final class NioSocketChannelUnsafe extends AbstractNioByteChannel.NioByteUnsafe {
    private NioSocketChannelUnsafe() {
      super(NioSocketChannel.this);
    }
    
    protected Executor prepareToClose() {
      try {
        if (NioSocketChannel.this.javaChannel().isOpen() && NioSocketChannel.this.config().getSoLinger() > 0) {
          NioSocketChannel.this.doDeregister();
          return (Executor)GlobalEventExecutor.INSTANCE;
        } 
      } catch (Throwable throwable) {}
      return null;
    }
  }
  
  private final class NioSocketChannelConfig extends DefaultSocketChannelConfig {
    private volatile int maxBytesPerGatheringWrite = Integer.MAX_VALUE;
    
    private NioSocketChannelConfig(NioSocketChannel channel, Socket javaSocket) {
      super(channel, javaSocket);
      calculateMaxBytesPerGatheringWrite();
    }
    
    protected void autoReadCleared() {
      NioSocketChannel.this.clearReadPending();
    }
    
    public NioSocketChannelConfig setSendBufferSize(int sendBufferSize) {
      super.setSendBufferSize(sendBufferSize);
      calculateMaxBytesPerGatheringWrite();
      return this;
    }
    
    void setMaxBytesPerGatheringWrite(int maxBytesPerGatheringWrite) {
      this.maxBytesPerGatheringWrite = maxBytesPerGatheringWrite;
    }
    
    int getMaxBytesPerGatheringWrite() {
      return this.maxBytesPerGatheringWrite;
    }
    
    private void calculateMaxBytesPerGatheringWrite() {
      int newSendBufferSize = getSendBufferSize() << 1;
      if (newSendBufferSize > 0)
        setMaxBytesPerGatheringWrite(getSendBufferSize() << 1); 
    }
  }
}

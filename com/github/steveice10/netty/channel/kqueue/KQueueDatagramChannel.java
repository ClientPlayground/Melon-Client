package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.AbstractChannel;
import com.github.steveice10.netty.channel.AddressedEnvelope;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.DefaultAddressedEnvelope;
import com.github.steveice10.netty.channel.socket.DatagramChannel;
import com.github.steveice10.netty.channel.socket.DatagramChannelConfig;
import com.github.steveice10.netty.channel.socket.DatagramPacket;
import com.github.steveice10.netty.channel.unix.DatagramSocketAddress;
import com.github.steveice10.netty.channel.unix.IovArray;
import com.github.steveice10.netty.channel.unix.UnixChannelUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public final class KQueueDatagramChannel extends AbstractKQueueChannel implements DatagramChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(true);
  
  private static final String EXPECTED_TYPES = " (expected: " + 
    StringUtil.simpleClassName(DatagramPacket.class) + ", " + 
    StringUtil.simpleClassName(AddressedEnvelope.class) + '<' + 
    StringUtil.simpleClassName(ByteBuf.class) + ", " + 
    StringUtil.simpleClassName(InetSocketAddress.class) + ">, " + 
    StringUtil.simpleClassName(ByteBuf.class) + ')';
  
  private volatile boolean connected;
  
  private final KQueueDatagramChannelConfig config;
  
  public KQueueDatagramChannel() {
    super((Channel)null, BsdSocket.newSocketDgram(), false);
    this.config = new KQueueDatagramChannelConfig(this);
  }
  
  public KQueueDatagramChannel(int fd) {
    this(new BsdSocket(fd), true);
  }
  
  KQueueDatagramChannel(BsdSocket socket, boolean active) {
    super((Channel)null, socket, active);
    this.config = new KQueueDatagramChannelConfig(this);
  }
  
  public InetSocketAddress remoteAddress() {
    return (InetSocketAddress)super.remoteAddress();
  }
  
  public InetSocketAddress localAddress() {
    return (InetSocketAddress)super.localAddress();
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  public boolean isActive() {
    return (this.socket.isOpen() && ((this.config.getActiveOnOpen() && isRegistered()) || this.active));
  }
  
  public boolean isConnected() {
    return this.connected;
  }
  
  public ChannelFuture joinGroup(InetAddress multicastAddress) {
    return joinGroup(multicastAddress, newPromise());
  }
  
  public ChannelFuture joinGroup(InetAddress multicastAddress, ChannelPromise promise) {
    try {
      return joinGroup(multicastAddress, 
          
          NetworkInterface.getByInetAddress(localAddress().getAddress()), (InetAddress)null, promise);
    } catch (SocketException e) {
      promise.setFailure(e);
      return (ChannelFuture)promise;
    } 
  }
  
  public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface) {
    return joinGroup(multicastAddress, networkInterface, newPromise());
  }
  
  public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise) {
    return joinGroup(multicastAddress.getAddress(), networkInterface, (InetAddress)null, promise);
  }
  
  public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source) {
    return joinGroup(multicastAddress, networkInterface, source, newPromise());
  }
  
  public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise) {
    if (multicastAddress == null)
      throw new NullPointerException("multicastAddress"); 
    if (networkInterface == null)
      throw new NullPointerException("networkInterface"); 
    promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture leaveGroup(InetAddress multicastAddress) {
    return leaveGroup(multicastAddress, newPromise());
  }
  
  public ChannelFuture leaveGroup(InetAddress multicastAddress, ChannelPromise promise) {
    try {
      return leaveGroup(multicastAddress, 
          NetworkInterface.getByInetAddress(localAddress().getAddress()), (InetAddress)null, promise);
    } catch (SocketException e) {
      promise.setFailure(e);
      return (ChannelFuture)promise;
    } 
  }
  
  public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface) {
    return leaveGroup(multicastAddress, networkInterface, newPromise());
  }
  
  public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise) {
    return leaveGroup(multicastAddress.getAddress(), networkInterface, (InetAddress)null, promise);
  }
  
  public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source) {
    return leaveGroup(multicastAddress, networkInterface, source, newPromise());
  }
  
  public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise) {
    if (multicastAddress == null)
      throw new NullPointerException("multicastAddress"); 
    if (networkInterface == null)
      throw new NullPointerException("networkInterface"); 
    promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock) {
    return block(multicastAddress, networkInterface, sourceToBlock, newPromise());
  }
  
  public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock, ChannelPromise promise) {
    if (multicastAddress == null)
      throw new NullPointerException("multicastAddress"); 
    if (sourceToBlock == null)
      throw new NullPointerException("sourceToBlock"); 
    if (networkInterface == null)
      throw new NullPointerException("networkInterface"); 
    promise.setFailure(new UnsupportedOperationException("Multicast not supported"));
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock) {
    return block(multicastAddress, sourceToBlock, newPromise());
  }
  
  public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock, ChannelPromise promise) {
    try {
      return block(multicastAddress, 
          
          NetworkInterface.getByInetAddress(localAddress().getAddress()), sourceToBlock, promise);
    } catch (Throwable e) {
      promise.setFailure(e);
      return (ChannelFuture)promise;
    } 
  }
  
  protected AbstractKQueueChannel.AbstractKQueueUnsafe newUnsafe() {
    return new KQueueDatagramChannelUnsafe();
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    super.doBind(localAddress);
    this.active = true;
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    while (true) {
      Object msg = in.current();
      if (msg == null) {
        writeFilter(false);
        break;
      } 
      try {
        boolean done = false;
        for (int i = config().getWriteSpinCount(); i > 0; i--) {
          if (doWriteMessage(msg)) {
            done = true;
            break;
          } 
        } 
        if (done) {
          in.remove();
          continue;
        } 
        writeFilter(true);
        break;
      } catch (IOException e) {
        in.remove(e);
      } 
    } 
  }
  
  private boolean doWriteMessage(Object msg) throws Exception {
    ByteBuf data;
    InetSocketAddress remoteAddress;
    long writtenBytes;
    if (msg instanceof AddressedEnvelope) {
      AddressedEnvelope<ByteBuf, InetSocketAddress> envelope = (AddressedEnvelope<ByteBuf, InetSocketAddress>)msg;
      data = (ByteBuf)envelope.content();
      remoteAddress = (InetSocketAddress)envelope.recipient();
    } else {
      data = (ByteBuf)msg;
      remoteAddress = null;
    } 
    int dataLen = data.readableBytes();
    if (dataLen == 0)
      return true; 
    if (data.hasMemoryAddress()) {
      long memoryAddress = data.memoryAddress();
      if (remoteAddress == null) {
        writtenBytes = this.socket.writeAddress(memoryAddress, data.readerIndex(), data.writerIndex());
      } else {
        writtenBytes = this.socket.sendToAddress(memoryAddress, data.readerIndex(), data.writerIndex(), remoteAddress
            .getAddress(), remoteAddress.getPort());
      } 
    } else if (data.nioBufferCount() > 1) {
      IovArray array = ((KQueueEventLoop)eventLoop()).cleanArray();
      array.add(data);
      int cnt = array.count();
      assert cnt != 0;
      if (remoteAddress == null) {
        writtenBytes = this.socket.writevAddresses(array.memoryAddress(0), cnt);
      } else {
        writtenBytes = this.socket.sendToAddresses(array.memoryAddress(0), cnt, remoteAddress
            .getAddress(), remoteAddress.getPort());
      } 
    } else {
      ByteBuffer nioData = data.internalNioBuffer(data.readerIndex(), data.readableBytes());
      if (remoteAddress == null) {
        writtenBytes = this.socket.write(nioData, nioData.position(), nioData.limit());
      } else {
        writtenBytes = this.socket.sendTo(nioData, nioData.position(), nioData.limit(), remoteAddress
            .getAddress(), remoteAddress.getPort());
      } 
    } 
    return (writtenBytes > 0L);
  }
  
  protected Object filterOutboundMessage(Object msg) {
    if (msg instanceof DatagramPacket) {
      DatagramPacket packet = (DatagramPacket)msg;
      ByteBuf content = (ByteBuf)packet.content();
      return UnixChannelUtil.isBufferCopyNeededForWrite(content) ? new DatagramPacket(
          newDirectBuffer(packet, content), (InetSocketAddress)packet.recipient()) : msg;
    } 
    if (msg instanceof ByteBuf) {
      ByteBuf buf = (ByteBuf)msg;
      return UnixChannelUtil.isBufferCopyNeededForWrite(buf) ? newDirectBuffer(buf) : buf;
    } 
    if (msg instanceof AddressedEnvelope) {
      AddressedEnvelope<Object, SocketAddress> e = (AddressedEnvelope<Object, SocketAddress>)msg;
      if (e.content() instanceof ByteBuf && (e
        .recipient() == null || e.recipient() instanceof InetSocketAddress)) {
        ByteBuf content = (ByteBuf)e.content();
        return UnixChannelUtil.isBufferCopyNeededForWrite(content) ? new DefaultAddressedEnvelope(
            
            newDirectBuffer(e, content), e.recipient()) : e;
      } 
    } 
    throw new UnsupportedOperationException("unsupported message type: " + 
        StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
  }
  
  public KQueueDatagramChannelConfig config() {
    return this.config;
  }
  
  protected void doDisconnect() throws Exception {
    this.socket.disconnect();
    this.connected = this.active = false;
  }
  
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    if (super.doConnect(remoteAddress, localAddress)) {
      this.connected = true;
      return true;
    } 
    return false;
  }
  
  protected void doClose() throws Exception {
    super.doClose();
    this.connected = false;
  }
  
  final class KQueueDatagramChannelUnsafe extends AbstractKQueueChannel.AbstractKQueueUnsafe {
    void readReady(KQueueRecvByteAllocatorHandle allocHandle) {
      assert KQueueDatagramChannel.this.eventLoop().inEventLoop();
      DatagramChannelConfig config = KQueueDatagramChannel.this.config();
      if (KQueueDatagramChannel.this.shouldBreakReadReady((ChannelConfig)config)) {
        clearReadFilter0();
        return;
      } 
      ChannelPipeline pipeline = KQueueDatagramChannel.this.pipeline();
      ByteBufAllocator allocator = config.getAllocator();
      allocHandle.reset((ChannelConfig)config);
      readReadyBefore();
      Throwable exception = null;
      try {
        ByteBuf data = null;
        try {
          do {
            DatagramSocketAddress remoteAddress;
            data = allocHandle.allocate(allocator);
            allocHandle.attemptedBytesRead(data.writableBytes());
            if (data.hasMemoryAddress()) {
              remoteAddress = KQueueDatagramChannel.this.socket.recvFromAddress(data.memoryAddress(), data.writerIndex(), data
                  .capacity());
            } else {
              ByteBuffer nioData = data.internalNioBuffer(data.writerIndex(), data.writableBytes());
              remoteAddress = KQueueDatagramChannel.this.socket.recvFrom(nioData, nioData.position(), nioData.limit());
            } 
            if (remoteAddress == null) {
              allocHandle.lastBytesRead(-1);
              data.release();
              data = null;
              break;
            } 
            allocHandle.incMessagesRead(1);
            allocHandle.lastBytesRead(remoteAddress.receivedAmount());
            data.writerIndex(data.writerIndex() + allocHandle.lastBytesRead());
            this.readPending = false;
            pipeline.fireChannelRead(new DatagramPacket(data, (InetSocketAddress)
                  localAddress(), (InetSocketAddress)remoteAddress));
            data = null;
          } while (allocHandle.continueReading());
        } catch (Throwable t) {
          if (data != null)
            data.release(); 
          exception = t;
        } 
        allocHandle.readComplete();
        pipeline.fireChannelReadComplete();
        if (exception != null)
          pipeline.fireExceptionCaught(exception); 
      } finally {
        readReadyFinally((ChannelConfig)config);
      } 
    }
  }
}

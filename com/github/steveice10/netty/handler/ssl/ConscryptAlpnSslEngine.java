package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.SystemPropertyUtil;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import org.conscrypt.AllocatedBuffer;
import org.conscrypt.BufferAllocator;
import org.conscrypt.Conscrypt;
import org.conscrypt.HandshakeListener;

abstract class ConscryptAlpnSslEngine extends JdkSslEngine {
  private static final boolean USE_BUFFER_ALLOCATOR = SystemPropertyUtil.getBoolean("com.github.steveice10.netty.handler.ssl.conscrypt.useBufferAllocator", true);
  
  static ConscryptAlpnSslEngine newClientEngine(SSLEngine engine, ByteBufAllocator alloc, JdkApplicationProtocolNegotiator applicationNegotiator) {
    return new ClientEngine(engine, alloc, applicationNegotiator);
  }
  
  static ConscryptAlpnSslEngine newServerEngine(SSLEngine engine, ByteBufAllocator alloc, JdkApplicationProtocolNegotiator applicationNegotiator) {
    return new ServerEngine(engine, alloc, applicationNegotiator);
  }
  
  private ConscryptAlpnSslEngine(SSLEngine engine, ByteBufAllocator alloc, List<String> protocols) {
    super(engine);
    if (USE_BUFFER_ALLOCATOR)
      Conscrypt.setBufferAllocator(engine, new BufferAllocatorAdapter(alloc)); 
    Conscrypt.setApplicationProtocols(engine, protocols.<String>toArray(new String[protocols.size()]));
  }
  
  final int calculateOutNetBufSize(int plaintextBytes, int numBuffers) {
    long maxOverhead = Conscrypt.maxSealOverhead(getWrappedEngine()) * numBuffers;
    return (int)Math.min(2147483647L, plaintextBytes + maxOverhead);
  }
  
  final SSLEngineResult unwrap(ByteBuffer[] srcs, ByteBuffer[] dests) throws SSLException {
    return Conscrypt.unwrap(getWrappedEngine(), srcs, dests);
  }
  
  private static final class ClientEngine extends ConscryptAlpnSslEngine {
    private final JdkApplicationProtocolNegotiator.ProtocolSelectionListener protocolListener;
    
    ClientEngine(SSLEngine engine, ByteBufAllocator alloc, JdkApplicationProtocolNegotiator applicationNegotiator) {
      super(engine, alloc, applicationNegotiator.protocols());
      Conscrypt.setHandshakeListener(engine, new HandshakeListener() {
            public void onHandshakeFinished() throws SSLException {
              ConscryptAlpnSslEngine.ClientEngine.this.selectProtocol();
            }
          });
      this.protocolListener = (JdkApplicationProtocolNegotiator.ProtocolSelectionListener)ObjectUtil.checkNotNull(applicationNegotiator
          .protocolListenerFactory().newListener(this, applicationNegotiator.protocols()), "protocolListener");
    }
    
    private void selectProtocol() throws SSLException {
      String protocol = Conscrypt.getApplicationProtocol(getWrappedEngine());
      try {
        this.protocolListener.selected(protocol);
      } catch (Throwable e) {
        throw SslUtils.toSSLHandshakeException(e);
      } 
    }
  }
  
  private static final class ServerEngine extends ConscryptAlpnSslEngine {
    private final JdkApplicationProtocolNegotiator.ProtocolSelector protocolSelector;
    
    ServerEngine(SSLEngine engine, ByteBufAllocator alloc, JdkApplicationProtocolNegotiator applicationNegotiator) {
      super(engine, alloc, applicationNegotiator.protocols());
      Conscrypt.setHandshakeListener(engine, new HandshakeListener() {
            public void onHandshakeFinished() throws SSLException {
              ConscryptAlpnSslEngine.ServerEngine.this.selectProtocol();
            }
          });
      this.protocolSelector = (JdkApplicationProtocolNegotiator.ProtocolSelector)ObjectUtil.checkNotNull(applicationNegotiator.protocolSelectorFactory()
          .newSelector(this, new LinkedHashSet<String>(applicationNegotiator
              .protocols())), "protocolSelector");
    }
    
    private void selectProtocol() throws SSLException {
      try {
        String protocol = Conscrypt.getApplicationProtocol(getWrappedEngine());
        this.protocolSelector.select((protocol != null) ? Collections.<String>singletonList(protocol) : 
            Collections.<String>emptyList());
      } catch (Throwable e) {
        throw SslUtils.toSSLHandshakeException(e);
      } 
    }
  }
  
  private static final class BufferAllocatorAdapter extends BufferAllocator {
    private final ByteBufAllocator alloc;
    
    BufferAllocatorAdapter(ByteBufAllocator alloc) {
      this.alloc = alloc;
    }
    
    public AllocatedBuffer allocateDirectBuffer(int capacity) {
      return new ConscryptAlpnSslEngine.BufferAdapter(this.alloc.directBuffer(capacity));
    }
  }
  
  private static final class BufferAdapter extends AllocatedBuffer {
    private final ByteBuf nettyBuffer;
    
    private final ByteBuffer buffer;
    
    BufferAdapter(ByteBuf nettyBuffer) {
      this.nettyBuffer = nettyBuffer;
      this.buffer = nettyBuffer.nioBuffer(0, nettyBuffer.capacity());
    }
    
    public ByteBuffer nioBuffer() {
      return this.buffer;
    }
    
    public AllocatedBuffer retain() {
      this.nettyBuffer.retain();
      return this;
    }
    
    public AllocatedBuffer release() {
      this.nettyBuffer.release();
      return this;
    }
  }
}

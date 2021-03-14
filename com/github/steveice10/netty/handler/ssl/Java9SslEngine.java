package com.github.steveice10.netty.handler.ssl;

import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiFunction;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

final class Java9SslEngine extends JdkSslEngine {
  private final JdkApplicationProtocolNegotiator.ProtocolSelectionListener selectionListener;
  
  private final AlpnSelector alpnSelector;
  
  private final class AlpnSelector implements BiFunction<SSLEngine, List<String>, String> {
    private final JdkApplicationProtocolNegotiator.ProtocolSelector selector;
    
    private boolean called;
    
    AlpnSelector(JdkApplicationProtocolNegotiator.ProtocolSelector selector) {
      this.selector = selector;
    }
    
    public String apply(SSLEngine sslEngine, List<String> strings) {
      assert !this.called;
      this.called = true;
      try {
        String selected = this.selector.select(strings);
        return (selected == null) ? "" : selected;
      } catch (Exception cause) {
        return null;
      } 
    }
    
    void checkUnsupported() {
      if (this.called)
        return; 
      String protocol = Java9SslEngine.this.getApplicationProtocol();
      assert protocol != null;
      if (protocol.isEmpty())
        this.selector.unsupported(); 
    }
  }
  
  Java9SslEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer) {
    super(engine);
    if (isServer) {
      this.selectionListener = null;
      this
        .alpnSelector = new AlpnSelector(applicationNegotiator.protocolSelectorFactory().newSelector(this, new LinkedHashSet<String>(applicationNegotiator.protocols())));
      Java9SslUtils.setHandshakeApplicationProtocolSelector(engine, this.alpnSelector);
    } else {
      this
        .selectionListener = applicationNegotiator.protocolListenerFactory().newListener(this, applicationNegotiator.protocols());
      this.alpnSelector = null;
      Java9SslUtils.setApplicationProtocols(engine, applicationNegotiator.protocols());
    } 
  }
  
  private SSLEngineResult verifyProtocolSelection(SSLEngineResult result) throws SSLException {
    if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED)
      if (this.alpnSelector == null) {
        try {
          String protocol = getApplicationProtocol();
          assert protocol != null;
          if (protocol.isEmpty()) {
            this.selectionListener.unsupported();
          } else {
            this.selectionListener.selected(protocol);
          } 
        } catch (Throwable e) {
          throw SslUtils.toSSLHandshakeException(e);
        } 
      } else {
        assert this.selectionListener == null;
        this.alpnSelector.checkUnsupported();
      }  
    return result;
  }
  
  public SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
    return verifyProtocolSelection(super.wrap(src, dst));
  }
  
  public SSLEngineResult wrap(ByteBuffer[] srcs, ByteBuffer dst) throws SSLException {
    return verifyProtocolSelection(super.wrap(srcs, dst));
  }
  
  public SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int len, ByteBuffer dst) throws SSLException {
    return verifyProtocolSelection(super.wrap(srcs, offset, len, dst));
  }
  
  public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
    return verifyProtocolSelection(super.unwrap(src, dst));
  }
  
  public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts) throws SSLException {
    return verifyProtocolSelection(super.unwrap(src, dsts));
  }
  
  public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dst, int offset, int len) throws SSLException {
    return verifyProtocolSelection(super.unwrap(src, dst, offset, len));
  }
  
  void setNegotiatedApplicationProtocol(String applicationProtocol) {}
  
  public String getNegotiatedApplicationProtocol() {
    String protocol = getApplicationProtocol();
    if (protocol != null)
      return protocol.isEmpty() ? null : protocol; 
    return protocol;
  }
  
  public String getApplicationProtocol() {
    return Java9SslUtils.getApplicationProtocol(getWrappedEngine());
  }
  
  public String getHandshakeApplicationProtocol() {
    return Java9SslUtils.getHandshakeApplicationProtocol(getWrappedEngine());
  }
  
  public void setHandshakeApplicationProtocolSelector(BiFunction<SSLEngine, List<String>, String> selector) {
    Java9SslUtils.setHandshakeApplicationProtocolSelector(getWrappedEngine(), selector);
  }
  
  public BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector() {
    return Java9SslUtils.getHandshakeApplicationProtocolSelector(getWrappedEngine());
  }
}

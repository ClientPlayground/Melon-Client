package com.github.steveice10.netty.handler.ssl;

import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLEngine;

final class JdkDefaultApplicationProtocolNegotiator implements JdkApplicationProtocolNegotiator {
  public static final JdkDefaultApplicationProtocolNegotiator INSTANCE = new JdkDefaultApplicationProtocolNegotiator();
  
  private static final JdkApplicationProtocolNegotiator.SslEngineWrapperFactory DEFAULT_SSL_ENGINE_WRAPPER_FACTORY = new JdkApplicationProtocolNegotiator.SslEngineWrapperFactory() {
      public SSLEngine wrapSslEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer) {
        return engine;
      }
    };
  
  public JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory() {
    return DEFAULT_SSL_ENGINE_WRAPPER_FACTORY;
  }
  
  public JdkApplicationProtocolNegotiator.ProtocolSelectorFactory protocolSelectorFactory() {
    throw new UnsupportedOperationException("Application protocol negotiation unsupported");
  }
  
  public JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory protocolListenerFactory() {
    throw new UnsupportedOperationException("Application protocol negotiation unsupported");
  }
  
  public List<String> protocols() {
    return Collections.emptyList();
  }
}

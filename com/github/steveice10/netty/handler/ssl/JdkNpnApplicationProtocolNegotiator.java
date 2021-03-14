package com.github.steveice10.netty.handler.ssl;

import java.util.List;
import javax.net.ssl.SSLEngine;

@Deprecated
public final class JdkNpnApplicationProtocolNegotiator extends JdkBaseApplicationProtocolNegotiator {
  private static final JdkApplicationProtocolNegotiator.SslEngineWrapperFactory NPN_WRAPPER = new JdkApplicationProtocolNegotiator.SslEngineWrapperFactory() {
      public SSLEngine wrapSslEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer) {
        return new JettyNpnSslEngine(engine, applicationNegotiator, isServer);
      }
    };
  
  public JdkNpnApplicationProtocolNegotiator(Iterable<String> protocols) {
    this(false, protocols);
  }
  
  public JdkNpnApplicationProtocolNegotiator(String... protocols) {
    this(false, protocols);
  }
  
  public JdkNpnApplicationProtocolNegotiator(boolean failIfNoCommonProtocols, Iterable<String> protocols) {
    this(failIfNoCommonProtocols, failIfNoCommonProtocols, protocols);
  }
  
  public JdkNpnApplicationProtocolNegotiator(boolean failIfNoCommonProtocols, String... protocols) {
    this(failIfNoCommonProtocols, failIfNoCommonProtocols, protocols);
  }
  
  public JdkNpnApplicationProtocolNegotiator(boolean clientFailIfNoCommonProtocols, boolean serverFailIfNoCommonProtocols, Iterable<String> protocols) {
    this(clientFailIfNoCommonProtocols ? FAIL_SELECTOR_FACTORY : NO_FAIL_SELECTOR_FACTORY, serverFailIfNoCommonProtocols ? FAIL_SELECTION_LISTENER_FACTORY : NO_FAIL_SELECTION_LISTENER_FACTORY, protocols);
  }
  
  public JdkNpnApplicationProtocolNegotiator(boolean clientFailIfNoCommonProtocols, boolean serverFailIfNoCommonProtocols, String... protocols) {
    this(clientFailIfNoCommonProtocols ? FAIL_SELECTOR_FACTORY : NO_FAIL_SELECTOR_FACTORY, serverFailIfNoCommonProtocols ? FAIL_SELECTION_LISTENER_FACTORY : NO_FAIL_SELECTION_LISTENER_FACTORY, protocols);
  }
  
  public JdkNpnApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, Iterable<String> protocols) {
    super(NPN_WRAPPER, selectorFactory, listenerFactory, protocols);
  }
  
  public JdkNpnApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, String... protocols) {
    super(NPN_WRAPPER, selectorFactory, listenerFactory, protocols);
  }
}

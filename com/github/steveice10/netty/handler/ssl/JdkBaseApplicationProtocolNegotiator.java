package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLHandshakeException;

class JdkBaseApplicationProtocolNegotiator implements JdkApplicationProtocolNegotiator {
  private final List<String> protocols;
  
  private final JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory;
  
  private final JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory;
  
  private final JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory;
  
  JdkBaseApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory, JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, Iterable<String> protocols) {
    this(wrapperFactory, selectorFactory, listenerFactory, ApplicationProtocolUtil.toList(protocols));
  }
  
  JdkBaseApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory, JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, String... protocols) {
    this(wrapperFactory, selectorFactory, listenerFactory, ApplicationProtocolUtil.toList(protocols));
  }
  
  private JdkBaseApplicationProtocolNegotiator(JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory, JdkApplicationProtocolNegotiator.ProtocolSelectorFactory selectorFactory, JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory listenerFactory, List<String> protocols) {
    this.wrapperFactory = (JdkApplicationProtocolNegotiator.SslEngineWrapperFactory)ObjectUtil.checkNotNull(wrapperFactory, "wrapperFactory");
    this.selectorFactory = (JdkApplicationProtocolNegotiator.ProtocolSelectorFactory)ObjectUtil.checkNotNull(selectorFactory, "selectorFactory");
    this.listenerFactory = (JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory)ObjectUtil.checkNotNull(listenerFactory, "listenerFactory");
    this.protocols = Collections.unmodifiableList((List<? extends String>)ObjectUtil.checkNotNull(protocols, "protocols"));
  }
  
  public List<String> protocols() {
    return this.protocols;
  }
  
  public JdkApplicationProtocolNegotiator.ProtocolSelectorFactory protocolSelectorFactory() {
    return this.selectorFactory;
  }
  
  public JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory protocolListenerFactory() {
    return this.listenerFactory;
  }
  
  public JdkApplicationProtocolNegotiator.SslEngineWrapperFactory wrapperFactory() {
    return this.wrapperFactory;
  }
  
  static final JdkApplicationProtocolNegotiator.ProtocolSelectorFactory FAIL_SELECTOR_FACTORY = new JdkApplicationProtocolNegotiator.ProtocolSelectorFactory() {
      public JdkApplicationProtocolNegotiator.ProtocolSelector newSelector(SSLEngine engine, Set<String> supportedProtocols) {
        return new JdkBaseApplicationProtocolNegotiator.FailProtocolSelector((JdkSslEngine)engine, supportedProtocols);
      }
    };
  
  static final JdkApplicationProtocolNegotiator.ProtocolSelectorFactory NO_FAIL_SELECTOR_FACTORY = new JdkApplicationProtocolNegotiator.ProtocolSelectorFactory() {
      public JdkApplicationProtocolNegotiator.ProtocolSelector newSelector(SSLEngine engine, Set<String> supportedProtocols) {
        return new JdkBaseApplicationProtocolNegotiator.NoFailProtocolSelector((JdkSslEngine)engine, supportedProtocols);
      }
    };
  
  static final JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory FAIL_SELECTION_LISTENER_FACTORY = new JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory() {
      public JdkApplicationProtocolNegotiator.ProtocolSelectionListener newListener(SSLEngine engine, List<String> supportedProtocols) {
        return new JdkBaseApplicationProtocolNegotiator.FailProtocolSelectionListener((JdkSslEngine)engine, supportedProtocols);
      }
    };
  
  static final JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory NO_FAIL_SELECTION_LISTENER_FACTORY = new JdkApplicationProtocolNegotiator.ProtocolSelectionListenerFactory() {
      public JdkApplicationProtocolNegotiator.ProtocolSelectionListener newListener(SSLEngine engine, List<String> supportedProtocols) {
        return new JdkBaseApplicationProtocolNegotiator.NoFailProtocolSelectionListener((JdkSslEngine)engine, supportedProtocols);
      }
    };
  
  static class NoFailProtocolSelector implements JdkApplicationProtocolNegotiator.ProtocolSelector {
    private final JdkSslEngine engineWrapper;
    
    private final Set<String> supportedProtocols;
    
    NoFailProtocolSelector(JdkSslEngine engineWrapper, Set<String> supportedProtocols) {
      this.engineWrapper = engineWrapper;
      this.supportedProtocols = supportedProtocols;
    }
    
    public void unsupported() {
      this.engineWrapper.setNegotiatedApplicationProtocol(null);
    }
    
    public String select(List<String> protocols) throws Exception {
      for (String p : this.supportedProtocols) {
        if (protocols.contains(p)) {
          this.engineWrapper.setNegotiatedApplicationProtocol(p);
          return p;
        } 
      } 
      return noSelectMatchFound();
    }
    
    public String noSelectMatchFound() throws Exception {
      this.engineWrapper.setNegotiatedApplicationProtocol(null);
      return null;
    }
  }
  
  private static final class FailProtocolSelector extends NoFailProtocolSelector {
    FailProtocolSelector(JdkSslEngine engineWrapper, Set<String> supportedProtocols) {
      super(engineWrapper, supportedProtocols);
    }
    
    public String noSelectMatchFound() throws Exception {
      throw new SSLHandshakeException("Selected protocol is not supported");
    }
  }
  
  private static class NoFailProtocolSelectionListener implements JdkApplicationProtocolNegotiator.ProtocolSelectionListener {
    private final JdkSslEngine engineWrapper;
    
    private final List<String> supportedProtocols;
    
    NoFailProtocolSelectionListener(JdkSslEngine engineWrapper, List<String> supportedProtocols) {
      this.engineWrapper = engineWrapper;
      this.supportedProtocols = supportedProtocols;
    }
    
    public void unsupported() {
      this.engineWrapper.setNegotiatedApplicationProtocol(null);
    }
    
    public void selected(String protocol) throws Exception {
      if (this.supportedProtocols.contains(protocol)) {
        this.engineWrapper.setNegotiatedApplicationProtocol(protocol);
      } else {
        noSelectedMatchFound(protocol);
      } 
    }
    
    protected void noSelectedMatchFound(String protocol) throws Exception {}
  }
  
  private static final class FailProtocolSelectionListener extends NoFailProtocolSelectionListener {
    FailProtocolSelectionListener(JdkSslEngine engineWrapper, List<String> supportedProtocols) {
      super(engineWrapper, supportedProtocols);
    }
    
    protected void noSelectedMatchFound(String protocol) throws Exception {
      throw new SSLHandshakeException("No compatible protocols found");
    }
  }
}

package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLEngine;

@Deprecated
public interface JdkApplicationProtocolNegotiator extends ApplicationProtocolNegotiator {
  SslEngineWrapperFactory wrapperFactory();
  
  ProtocolSelectorFactory protocolSelectorFactory();
  
  ProtocolSelectionListenerFactory protocolListenerFactory();
  
  public static interface ProtocolSelectionListenerFactory {
    JdkApplicationProtocolNegotiator.ProtocolSelectionListener newListener(SSLEngine param1SSLEngine, List<String> param1List);
  }
  
  public static interface ProtocolSelectorFactory {
    JdkApplicationProtocolNegotiator.ProtocolSelector newSelector(SSLEngine param1SSLEngine, Set<String> param1Set);
  }
  
  public static interface ProtocolSelectionListener {
    void unsupported();
    
    void selected(String param1String) throws Exception;
  }
  
  public static interface ProtocolSelector {
    void unsupported();
    
    String select(List<String> param1List) throws Exception;
  }
  
  public static abstract class AllocatorAwareSslEngineWrapperFactory implements SslEngineWrapperFactory {
    public final SSLEngine wrapSslEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer) {
      return wrapSslEngine(engine, ByteBufAllocator.DEFAULT, applicationNegotiator, isServer);
    }
    
    abstract SSLEngine wrapSslEngine(SSLEngine param1SSLEngine, ByteBufAllocator param1ByteBufAllocator, JdkApplicationProtocolNegotiator param1JdkApplicationProtocolNegotiator, boolean param1Boolean);
  }
  
  public static interface SslEngineWrapperFactory {
    SSLEngine wrapSslEngine(SSLEngine param1SSLEngine, JdkApplicationProtocolNegotiator param1JdkApplicationProtocolNegotiator, boolean param1Boolean);
  }
}

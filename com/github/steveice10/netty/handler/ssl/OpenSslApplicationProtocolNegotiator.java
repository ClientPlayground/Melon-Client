package com.github.steveice10.netty.handler.ssl;

@Deprecated
public interface OpenSslApplicationProtocolNegotiator extends ApplicationProtocolNegotiator {
  ApplicationProtocolConfig.Protocol protocol();
  
  ApplicationProtocolConfig.SelectorFailureBehavior selectorFailureBehavior();
  
  ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedListenerFailureBehavior();
}

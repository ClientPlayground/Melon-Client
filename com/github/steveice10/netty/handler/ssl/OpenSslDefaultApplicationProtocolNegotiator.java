package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.List;

@Deprecated
public final class OpenSslDefaultApplicationProtocolNegotiator implements OpenSslApplicationProtocolNegotiator {
  private final ApplicationProtocolConfig config;
  
  public OpenSslDefaultApplicationProtocolNegotiator(ApplicationProtocolConfig config) {
    this.config = (ApplicationProtocolConfig)ObjectUtil.checkNotNull(config, "config");
  }
  
  public List<String> protocols() {
    return this.config.supportedProtocols();
  }
  
  public ApplicationProtocolConfig.Protocol protocol() {
    return this.config.protocol();
  }
  
  public ApplicationProtocolConfig.SelectorFailureBehavior selectorFailureBehavior() {
    return this.config.selectorFailureBehavior();
  }
  
  public ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedListenerFailureBehavior() {
    return this.config.selectedListenerFailureBehavior();
  }
}

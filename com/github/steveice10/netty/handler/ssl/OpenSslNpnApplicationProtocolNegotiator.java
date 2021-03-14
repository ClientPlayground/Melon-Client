package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.List;

@Deprecated
public final class OpenSslNpnApplicationProtocolNegotiator implements OpenSslApplicationProtocolNegotiator {
  private final List<String> protocols;
  
  public OpenSslNpnApplicationProtocolNegotiator(Iterable<String> protocols) {
    this.protocols = (List<String>)ObjectUtil.checkNotNull(ApplicationProtocolUtil.toList(protocols), "protocols");
  }
  
  public OpenSslNpnApplicationProtocolNegotiator(String... protocols) {
    this.protocols = (List<String>)ObjectUtil.checkNotNull(ApplicationProtocolUtil.toList(protocols), "protocols");
  }
  
  public ApplicationProtocolConfig.Protocol protocol() {
    return ApplicationProtocolConfig.Protocol.NPN;
  }
  
  public List<String> protocols() {
    return this.protocols;
  }
  
  public ApplicationProtocolConfig.SelectorFailureBehavior selectorFailureBehavior() {
    return ApplicationProtocolConfig.SelectorFailureBehavior.CHOOSE_MY_LAST_PROTOCOL;
  }
  
  public ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedListenerFailureBehavior() {
    return ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT;
  }
}

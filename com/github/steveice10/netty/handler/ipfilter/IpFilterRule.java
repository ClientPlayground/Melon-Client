package com.github.steveice10.netty.handler.ipfilter;

import java.net.InetSocketAddress;

public interface IpFilterRule {
  boolean matches(InetSocketAddress paramInetSocketAddress);
  
  IpFilterRuleType ruleType();
}

package com.github.steveice10.netty.handler.ipfilter;

import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Sharable
public class RuleBasedIpFilter extends AbstractRemoteAddressFilter<InetSocketAddress> {
  private final IpFilterRule[] rules;
  
  public RuleBasedIpFilter(IpFilterRule... rules) {
    if (rules == null)
      throw new NullPointerException("rules"); 
    this.rules = rules;
  }
  
  protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
    for (IpFilterRule rule : this.rules) {
      if (rule == null)
        break; 
      if (rule.matches(remoteAddress))
        return (rule.ruleType() == IpFilterRuleType.ACCEPT); 
    } 
    return true;
  }
}

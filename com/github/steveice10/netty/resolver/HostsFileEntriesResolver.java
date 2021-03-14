package com.github.steveice10.netty.resolver;

import java.net.InetAddress;

public interface HostsFileEntriesResolver {
  public static final HostsFileEntriesResolver DEFAULT = new DefaultHostsFileEntriesResolver();
  
  InetAddress address(String paramString, ResolvedAddressTypes paramResolvedAddressTypes);
}

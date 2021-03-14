package com.github.steveice10.netty.resolver.dns;

import java.net.InetAddress;

public interface DnsCacheEntry {
  InetAddress address();
  
  Throwable cause();
}

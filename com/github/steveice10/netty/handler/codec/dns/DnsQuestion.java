package com.github.steveice10.netty.handler.codec.dns;

public interface DnsQuestion extends DnsRecord {
  long timeToLive();
}

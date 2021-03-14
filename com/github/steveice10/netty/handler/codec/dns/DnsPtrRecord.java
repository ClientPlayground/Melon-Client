package com.github.steveice10.netty.handler.codec.dns;

public interface DnsPtrRecord extends DnsRecord {
  String hostname();
}

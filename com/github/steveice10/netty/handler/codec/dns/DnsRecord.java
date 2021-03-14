package com.github.steveice10.netty.handler.codec.dns;

public interface DnsRecord {
  public static final int CLASS_IN = 1;
  
  public static final int CLASS_CSNET = 2;
  
  public static final int CLASS_CHAOS = 3;
  
  public static final int CLASS_HESIOD = 4;
  
  public static final int CLASS_NONE = 254;
  
  public static final int CLASS_ANY = 255;
  
  String name();
  
  DnsRecordType type();
  
  int dnsClass();
  
  long timeToLive();
}

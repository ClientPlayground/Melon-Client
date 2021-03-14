package com.github.steveice10.netty.handler.codec.haproxy;

public enum HAProxyProxiedProtocol {
  UNKNOWN((byte)0, AddressFamily.AF_UNSPEC, TransportProtocol.UNSPEC),
  TCP4((byte)17, AddressFamily.AF_IPv4, TransportProtocol.STREAM),
  TCP6((byte)33, AddressFamily.AF_IPv6, TransportProtocol.STREAM),
  UDP4((byte)18, AddressFamily.AF_IPv4, TransportProtocol.DGRAM),
  UDP6((byte)34, AddressFamily.AF_IPv6, TransportProtocol.DGRAM),
  UNIX_STREAM((byte)49, AddressFamily.AF_UNIX, TransportProtocol.STREAM),
  UNIX_DGRAM((byte)50, AddressFamily.AF_UNIX, TransportProtocol.DGRAM);
  
  private final byte byteValue;
  
  private final AddressFamily addressFamily;
  
  private final TransportProtocol transportProtocol;
  
  HAProxyProxiedProtocol(byte byteValue, AddressFamily addressFamily, TransportProtocol transportProtocol) {
    this.byteValue = byteValue;
    this.addressFamily = addressFamily;
    this.transportProtocol = transportProtocol;
  }
  
  public byte byteValue() {
    return this.byteValue;
  }
  
  public AddressFamily addressFamily() {
    return this.addressFamily;
  }
  
  public TransportProtocol transportProtocol() {
    return this.transportProtocol;
  }
  
  public enum AddressFamily {
    AF_UNSPEC((byte)0),
    AF_IPv4((byte)16),
    AF_IPv6((byte)32),
    AF_UNIX((byte)48);
    
    private static final byte FAMILY_MASK = -16;
    
    private final byte byteValue;
    
    AddressFamily(byte byteValue) {
      this.byteValue = byteValue;
    }
    
    public byte byteValue() {
      return this.byteValue;
    }
  }
  
  public enum TransportProtocol {
    UNSPEC((byte)0),
    STREAM((byte)1),
    DGRAM((byte)2);
    
    private static final byte TRANSPORT_MASK = 15;
    
    private final byte transportByte;
    
    TransportProtocol(byte transportByte) {
      this.transportByte = transportByte;
    }
    
    public byte byteValue() {
      return this.transportByte;
    }
  }
}

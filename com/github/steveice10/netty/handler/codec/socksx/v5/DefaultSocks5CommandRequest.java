package com.github.steveice10.netty.handler.codec.socksx.v5;

import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.util.NetUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.net.IDN;

public final class DefaultSocks5CommandRequest extends AbstractSocks5Message implements Socks5CommandRequest {
  private final Socks5CommandType type;
  
  private final Socks5AddressType dstAddrType;
  
  private final String dstAddr;
  
  private final int dstPort;
  
  public DefaultSocks5CommandRequest(Socks5CommandType type, Socks5AddressType dstAddrType, String dstAddr, int dstPort) {
    if (type == null)
      throw new NullPointerException("type"); 
    if (dstAddrType == null)
      throw new NullPointerException("dstAddrType"); 
    if (dstAddr == null)
      throw new NullPointerException("dstAddr"); 
    if (dstAddrType == Socks5AddressType.IPv4) {
      if (!NetUtil.isValidIpV4Address(dstAddr))
        throw new IllegalArgumentException("dstAddr: " + dstAddr + " (expected: a valid IPv4 address)"); 
    } else if (dstAddrType == Socks5AddressType.DOMAIN) {
      dstAddr = IDN.toASCII(dstAddr);
      if (dstAddr.length() > 255)
        throw new IllegalArgumentException("dstAddr: " + dstAddr + " (expected: less than 256 chars)"); 
    } else if (dstAddrType == Socks5AddressType.IPv6 && 
      !NetUtil.isValidIpV6Address(dstAddr)) {
      throw new IllegalArgumentException("dstAddr: " + dstAddr + " (expected: a valid IPv6 address");
    } 
    if (dstPort < 0 || dstPort > 65535)
      throw new IllegalArgumentException("dstPort: " + dstPort + " (expected: 0~65535)"); 
    this.type = type;
    this.dstAddrType = dstAddrType;
    this.dstAddr = dstAddr;
    this.dstPort = dstPort;
  }
  
  public Socks5CommandType type() {
    return this.type;
  }
  
  public Socks5AddressType dstAddrType() {
    return this.dstAddrType;
  }
  
  public String dstAddr() {
    return this.dstAddr;
  }
  
  public int dstPort() {
    return this.dstPort;
  }
  
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append(StringUtil.simpleClassName(this));
    DecoderResult decoderResult = decoderResult();
    if (!decoderResult.isSuccess()) {
      buf.append("(decoderResult: ");
      buf.append(decoderResult);
      buf.append(", type: ");
    } else {
      buf.append("(type: ");
    } 
    buf.append(type());
    buf.append(", dstAddrType: ");
    buf.append(dstAddrType());
    buf.append(", dstAddr: ");
    buf.append(dstAddr());
    buf.append(", dstPort: ");
    buf.append(dstPort());
    buf.append(')');
    return buf.toString();
  }
}

package com.github.steveice10.netty.handler.codec.socksx.v4;

import com.github.steveice10.netty.handler.codec.DecoderResult;
import com.github.steveice10.netty.util.NetUtil;
import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultSocks4CommandResponse extends AbstractSocks4Message implements Socks4CommandResponse {
  private final Socks4CommandStatus status;
  
  private final String dstAddr;
  
  private final int dstPort;
  
  public DefaultSocks4CommandResponse(Socks4CommandStatus status) {
    this(status, null, 0);
  }
  
  public DefaultSocks4CommandResponse(Socks4CommandStatus status, String dstAddr, int dstPort) {
    if (status == null)
      throw new NullPointerException("cmdStatus"); 
    if (dstAddr != null && 
      !NetUtil.isValidIpV4Address(dstAddr))
      throw new IllegalArgumentException("dstAddr: " + dstAddr + " (expected: a valid IPv4 address)"); 
    if (dstPort < 0 || dstPort > 65535)
      throw new IllegalArgumentException("dstPort: " + dstPort + " (expected: 0~65535)"); 
    this.status = status;
    this.dstAddr = dstAddr;
    this.dstPort = dstPort;
  }
  
  public Socks4CommandStatus status() {
    return this.status;
  }
  
  public String dstAddr() {
    return this.dstAddr;
  }
  
  public int dstPort() {
    return this.dstPort;
  }
  
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append(StringUtil.simpleClassName(this));
    DecoderResult decoderResult = decoderResult();
    if (!decoderResult.isSuccess()) {
      buf.append("(decoderResult: ");
      buf.append(decoderResult);
      buf.append(", dstAddr: ");
    } else {
      buf.append("(dstAddr: ");
    } 
    buf.append(dstAddr());
    buf.append(", dstPort: ");
    buf.append(dstPort());
    buf.append(')');
    return buf.toString();
  }
}
